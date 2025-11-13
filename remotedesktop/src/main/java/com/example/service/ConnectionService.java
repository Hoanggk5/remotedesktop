package com.example.service;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import com.example.model.ConnectionInf;

public class ConnectionService {

    /**
     * Kết nối đến server và trả về ConnectionInfo (đã gắn socket).
     */
    public ConnectionInf connect(String ipAddress, int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, port), 5000);

        ConnectionInf info = new ConnectionInf(ipAddress, port);
        info.setSocket(socket);
        return info;
    }

    /**
     * Giao tiếp ban đầu: nhận lời chào, gửi lệnh kiểm tra kết nối.
     */
    public void performHandshake(ConnectionInf info) throws IOException {
        BufferedReader input = new BufferedReader(
                new InputStreamReader(info.getSocket().getInputStream(), StandardCharsets.UTF_8));

        PrintWriter output = new PrintWriter(
                new OutputStreamWriter(info.getSocket().getOutputStream(), StandardCharsets.UTF_8), true);

        // Đọc dòng đầu tiên từ server (thường là thông điệp chào)
        String greeting = input.readLine();
        if (greeting != null) {
            System.out.println("Server Greeting: " + greeting);
        }

        // Không đóng input/output — giữ nguyên cho các thread khác dùng (như
        // ImageReceiver)
    }

}