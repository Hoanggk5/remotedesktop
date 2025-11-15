package com.example.server;

import com.example.controller.KeyboardController;
import com.example.controller.MouseController;
import com.example.server.command.Command;
import com.example.server.command.CommandParser;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.awt.Robot;
import java.awt.AWTException;

/**
 * ClientHandler (Refactored)
 * Chỉ xử lý vòng lặp đọc lệnh và ủy quyền.
 * Logic stream ảnh và phân tích lệnh đã được tách ra.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final MouseController mouseController;
    private final String clientAddress;

    private ScreenStreamer screenStreamer;
    private Thread streamerThread;

    // Khóa chuyên dụng để đồng bộ hóa việc ghi ra output stream
    private final Object outputLock = new Object();

    // Cờ để luồng chính biết khi nào nên dừng
    private volatile boolean clientConnected = true;

    // Streams được khai báo là thành viên của lớp
    private BufferedReader reader;
    private PrintWriter writer;
    private DataOutputStream dataOut;

    private KeyboardController keyboardController;
    private final Robot robot;

    public KeyboardController getKeyboardController() {
        return keyboardController;
    }

    public ClientHandler(Socket socket) throws Exception {
        this.clientSocket = socket;
        this.mouseController = new MouseController();
        try {
            this.robot = new Robot(); // Khởi tạo Robot một lần
        } catch (AWTException e) {
            System.err.println("Failed to create Robot: " + e.getMessage());
            throw new Exception("Failed to initialize server handler", e);
        }

        this.keyboardController = new KeyboardController(this.robot); // Truyền Robot vào
        this.clientAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            // Khởi tạo streams
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            this.dataOut = new DataOutputStream(out);

            // Gửi thông tin chào mừng và kích thước màn hình
            sendWelcomeMessage();
            sendScreenSize();

            String clientMessage;
            // Vòng lặp đọc lệnh chính
            while (clientConnected && (clientMessage = reader.readLine()) != null) {

                System.out.println("[" + clientAddress + "] Command: " + clientMessage);

                // Ủy quyền cho Parser
                Command command = CommandParser.parse(clientMessage.trim());

                if (command != null) {
                    // Log mới để xác nhận thành công
                    System.out.println("[Parser] Success: " + command.getClass().getSimpleName());

                    // Thực thi lệnh (truyền 'this' làm context)
                    command.execute(this);
                } else {
                    // Log lỗi QUAN TRỌNG
                    System.err.println("[Parser] FAILED to parse: " + clientMessage);

                    // Gửi lỗi (dùng khóa)
                    synchronized (outputLock) {
                        writer.println("Unknown command: " + clientMessage);
                    }
                }
            }

        } catch (IOException e) {
            if (clientConnected) {
                System.err.println("[" + clientAddress + "] Connection lost: " + e.getMessage());
            }
        } finally {
            // Dọn dẹp
            stopStreaming();
            closeSocket();
            System.out.println("[" + clientAddress + "] Handler finished.");
        }
    }

    // --- Các phương thức được gọi bởi Command objects ---

    public void startStreaming() {
        if (streamerThread != null && streamerThread.isAlive()) {
            System.out.println("[" + clientAddress + "] Stream is already running.");
            return;
        }

        // Khởi tạo và chạy luồng streamer
        // Sử dụng streams và khóa của ClientHandler
        screenStreamer = new ScreenStreamer(writer, dataOut, outputLock);
        streamerThread = new Thread(screenStreamer, "Streamer-" + clientAddress);
        streamerThread.setDaemon(true);
        streamerThread.start();

        // Gửi thông báo (dùng khóa)
        synchronized (outputLock) {
            writer.println("STREAM_STARTED");
        }
    }

    public void stopStreaming() {
        if (screenStreamer != null) {
            screenStreamer.stop();
        }
        if (streamerThread != null) {
            try {
                streamerThread.join(200);
            } catch (InterruptedException ignored) {
            }
        }
        screenStreamer = null;
        streamerThread = null;
    }

    public void requestDisconnect() {
        this.clientConnected = false; // Báo cho vòng lặp while dừng lại
    }

    public MouseController getMouseController() {
        return mouseController;
    }

    // --- Các hàm tiện ích private ---

    private void sendWelcomeMessage() {
        synchronized (outputLock) {
            writer.println("Successfully connected to Remote Desktop Server.");
        }
        System.out.println("[" + clientAddress + "] Client connected.");
    }

    private void sendScreenSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // Gửi kích thước thật (double) để client tính toán chính xác
        synchronized (outputLock) {
            writer.println("SCREEN_SIZE " + (int) screenSize.getWidth() + " " + (int) screenSize.getHeight());
        }
    }

    private void closeSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                // Đóng socket sẽ tự động đóng tất cả streams của nó
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}
