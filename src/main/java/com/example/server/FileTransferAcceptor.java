package com.example.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class FileTransferAcceptor implements Runnable {

    private final File saveDir;
    private final PrintWriter commandWriter; // Writer của Kênh 1
    private final Object outputLock;

    public FileTransferAcceptor(File saveDir, PrintWriter commandWriter, Object outputLock) {
        this.saveDir = saveDir;
        this.commandWriter = commandWriter;
        this.outputLock = outputLock;
        if (!saveDir.exists())
            saveDir.mkdirs();
    }

    @Override
    public void run() {
        // Mở ServerSocket trên port 0 (OS tự chọn port)
        try (ServerSocket fileServerSocket = new ServerSocket(0)) {

            int filePort = fileServerSocket.getLocalPort();
            System.out.println("[FileAcceptor] Listening for file on port: " + filePort);

            // 1. Gửi port mới này cho Client qua Kênh 1
            synchronized (outputLock) {
                commandWriter.println("ACCEPT_FILE_TRANSFER " + filePort);
            }

            // 2. Chờ Client kết nối vào Kênh 2
            fileServerSocket.setSoTimeout(30000);
            Socket fileSocket = fileServerSocket.accept(); // Chờ kết nối mới

            System.out.println("[FileAcceptor] Client connected for file transfer.");

            // 3. Giao Kênh 2 cho RemoteFileHandler
            RemoteFileHandler fileHandler = new RemoteFileHandler(fileSocket, saveDir);
            fileHandler.receiveFile(); // Bắt đầu nhận

        } catch (SocketTimeoutException e) {
            System.err.println("[FileAcceptor] Client failed to connect in 30s.");
        } catch (IOException e) {
            System.err.println("[FileAcceptor] File transfer failed: " + e.getMessage());
        }
    }
}