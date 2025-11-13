package com.example.server;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.example.controller.MouseController;

/**
 * ClientHandler xử lý giao tiếp cho một client duy nhất.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final MouseController mouseController;

    private volatile boolean streaming = false;
    private Thread streamerThread;

    public ClientHandler(Socket socket) throws AWTException {
        this.clientSocket = socket;
        this.mouseController = new MouseController();
    }
    // Logger ghi vào file server.log

    @Override
    public void run() {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();

        try (
                // Đọc lệnh client gửi từ server
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(),
                                StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream(),
                                StandardCharsets.UTF_8),
                        true)) {
            writer.println("Successfully connected to Remote Desktop Server.");
            System.out.println("[" + clientAddress + "] Client connected.");

            // // Lấy độ phân giải màn hình server
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = (int) screenSize.getWidth();
            int screenHeight = (int) screenSize.getHeight();

            // // Gửi thông tin screen size cho client
            writer.println("SCREEN_SIZE " + screenWidth + " " + screenHeight);
            writer.flush();

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                clientMessage = clientMessage.trim();
                System.out.println("[" + clientAddress + "] Command: " + clientMessage);

                // --- LỆNH NGẮT KẾT NỐI ---
                if (clientMessage.equalsIgnoreCase("DISCONNECT")) {
                    System.out.println("[" + clientAddress + "] Disconnect requested.");
                    writer.println("DISCONNECT_ACK");
                    break;
                }
                // --- LỆNH CHỤP MÀN HÌNH: khởi streamer thread ---
                else if (clientMessage.equalsIgnoreCase("START_SCREEN")) {
                    if (!streaming) {
                        startStreaming(writer);
                        writer.println("STREAM_STARTED");
                    } else {
                        writer.println("ALREADY_STREAMING");
                    }
                }

                // // --- Điều khiển chuột ---
                else if (clientMessage.startsWith("MOUSE_MOVE")) {
                    String[] parts = clientMessage.split(" ");
                    if (parts.length >= 3) {
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            mouseController.move(x, y);
                        } catch (NumberFormatException ignore) {
                        }
                    }
                } else if (clientMessage.equals("CLICK_LEFT")) {
                    mouseController.clickLeft();
                } else if (clientMessage.equals("CLICK_RIGHT")) {
                    mouseController.clickRight();
                } else if (clientMessage.startsWith("SCROLL")) {
                    String[] parts = clientMessage.split(" ");
                    if (parts.length >= 2) {
                        try {
                            int amount = Integer.parseInt(parts[1]);
                            mouseController.scroll(amount);
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }

                else if (clientMessage.equals("MOUSE_PRESS LEFT")) {
                    mouseController.mousePressLeft();
                } else if (clientMessage.equals("MOUSE_RELEASE LEFT")) {
                    mouseController.mouseReleaseLeft();
                } else if (clientMessage.equals("MOUSE_PRESS RIGHT")) {
                    mouseController.mousePressRight();
                } else if (clientMessage.equals("MOUSE_RELEASE RIGHT")) {
                    mouseController.mouseReleaseRight();
                }

                else {
                    writer.println("Unknown command: " + clientMessage);
                }
            }

        } catch (IOException e) {
            System.err.println("[" + clientAddress + "] Connection lost: " +
                    e.getMessage());
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
                System.out.println("[" + clientAddress + "] Socket closed.");
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    /**
     * Bắt đầu streamer thread (gửi ảnh). Đồng bộ hóa khi ghi header text + payload.
     */

    private synchronized void startStreaming(PrintWriter writer) {
        streaming = true;
        final OutputStream rawOut;
        try {
            rawOut = clientSocket.getOutputStream();// gửi dữ liệu nhị phân về client
        } catch (IOException e) {
            streaming = false;
            return;
        }

        DataOutputStream dataOut = new DataOutputStream(rawOut);

        streamerThread = new Thread(() -> {
            try {
                while (streaming && !clientSocket.isClosed()) {
                    byte[] imageBytes = ScreenCapturer.captureScreen();

                    // Ghi header (text) rồi payload nhị phân — giữ atomic bằng synchronized trên
                    // dataOut
                    // Chỉ cho phép một luồng tại một thời điểm được thực thi bên trong block
                    synchronized (dataOut) {
                        writer.println("IMG_START " + imageBytes.length);
                        writer.flush();
                        dataOut.write(imageBytes);
                        dataOut.flush();
                    }

                    // // Sleep để giới hạn FPS (adjust nếu cần)
                    Thread.sleep(33); // ~30 FPS
                }
            } catch (Exception e) {
                System.err.println(
                        "[" + clientSocket.getInetAddress().getHostAddress() + "] Streamer error: " +
                                e.getMessage());
            } finally {
                streaming = false;
            }
        }, "Streamer-" + clientSocket.getInetAddress().getHostAddress());

        streamerThread.setDaemon(true);
        streamerThread.start();
    }

    private synchronized void stopStreaming() {
        streaming = false;
        if (streamerThread != null && streamerThread.isAlive()) {
            try {
                streamerThread.join(200); // đợi thread tắt nhanh
            } catch (InterruptedException ignored) {
            }
        }
    }
}
