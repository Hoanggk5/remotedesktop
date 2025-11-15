package com.example.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Server IP: "); // Nhập IP của Server
        String serverIP = scanner.nextLine();
        final int PORT = 5000;

        try (Socket socket = new Socket(serverIP, PORT)) {
            System.out.println("Connected successfully to server!"); // Kết nối thành công đến server

            // Gửi yêu cầu kết nố34i
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println("Hello Server, I am Client!");

            // Nhận phản hồi từ server
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = input.readLine();
            System.out.println(" Response from server: " + response);

            // socket.close();
            System.out.println("Disconnected from server."); // Ngắt kết nối với server
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage()); // Xử lý lỗi kết nối
        }
    }
}
