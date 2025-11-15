package com.example.server;

import java.io.*;
import java.net.Socket;

// Thay thế FileReceiver.java
public class RemoteFileHandler {

    private final Socket fileSocket; // Socket thứ 2
    private final File saveDir;

    public RemoteFileHandler(Socket fileSocket, File saveDir) throws IOException {
        this.fileSocket = fileSocket;
        this.saveDir = saveDir;
    }

    public void receiveFile() throws IOException {
        // Dùng try-with-resources để tự động đóng Socket 2
        try (Socket tempSocket = fileSocket;
                DataInputStream dataIn = new DataInputStream(fileSocket.getInputStream())) {

            // 1. Đọc metadata từ Kênh 2
            String fileName = dataIn.readUTF();
            long fileSize = dataIn.readLong();

            File outFile = new File(saveDir, fileName);
            System.out.println("Receiving file: " + outFile.getAbsolutePath());

            // 2. Đọc dữ liệu file
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[4096];
                long remaining = fileSize;
                int read;
                while (remaining > 0 &&
                        (read = dataIn.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }
            }
            System.out.println("File saved successfully!");
        }
    }
}