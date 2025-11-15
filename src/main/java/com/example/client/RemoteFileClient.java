package com.example.client;

import java.io.*;
import java.net.Socket;

public class RemoteFileClient {

    private final String serverIp; // Chỉ cần IP

    // SỬA LỖI: Nhận IP, không phải Socket
    public RemoteFileClient(String serverIp) {
        this.serverIp = serverIp;
    }

    // SỬA LỖI: Nhận 'filePort' từ ClientService
    public void sendFile(File file, int filePort) {
        if (file == null || !file.exists())
            return;

        System.out.println("Connecting to file port: " + serverIp + ":" + filePort);

        // Kênh 2: Mở kết nối tạm thời
        try (Socket fileSocket = new Socket(serverIp, filePort);
                DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
                FileInputStream fis = new FileInputStream(file)) {

            long size = file.length();

            // 1. Gửi metadata qua Kênh 2
            dos.writeUTF(file.getName());
            dos.writeLong(size);

            // 2. Gửi dữ liệu file
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }
            dos.flush();
            System.out.println("Uploaded file: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}