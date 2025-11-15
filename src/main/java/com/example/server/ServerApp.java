
package com.example.server;

import java.awt.AWTException;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class ServerApp {

    // Cổng (PORT) mà server sẽ lắng nghe
    private final static int PORT = 5000;

    /**
     * Finds the local IPv4 address, excluding loopback interfaces.
     * (Tìm địa chỉ IPv4 cục bộ, loại trừ giao diện loopback. Đây là IP Client
     * Windows sẽ kết nối tới.)
     */

    private static String getLocalIpAddress() {
        try {
            // Duyệt qua tất cả các giao diện mạng (network interfaces)
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();

                // Bỏ qua các giao diện ảo, loopback, hoặc không hoạt động
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }

                // Lấy danh sách địa chỉ IP từ giao diện này
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // Chỉ lấy địa chỉ IPv4
                    if (addr instanceof Inet4Address) {
                        // Trả về IP hợp lệ đầu tiên được tìm thấy
                        return addr.getHostAddress();
                    }
                }
            }
            // Nếu không tìm thấy, trả về loopback (cho trường hợp lỗi)
            return "127.0.0.1";
        } catch (SocketException e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            // Tự động lấy và in IP khi server khởi động
            String serverIP = getLocalIpAddress();
            System.out.println("=============================================");
            System.out.println("SERVER IS RUNNING!");
            System.out.println("Client should connect to IP: " + serverIP);
            System.out.println("Server is listening on port: " + PORT);
            System.out.println("=============================================");

            // Vòng lặp vĩnh cửu để Server luôn sẵn sàng chấp nhận Client mới
            while (true) {
                System.out.println("\nWaiting for a new client to connect...");

                // Chờ client kết nối (lệnh này sẽ blocking)
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " +
                        clientSocket.getInetAddress().getHostAddress());

                // Sử dụng Đa luồng: Tạo một luồng (Thread) mới để xử lý Client này,
                // sau đó Server chính quay lại chờ Client tiếp theo ngay lập tức.
                // ClientHandler clientHandler = new ClientHandler(clientSocket);
                // Thread thread = new Thread(clientHandler);
                // thread.start();

                try {
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT + ". Error: " +
                    e.getMessage());
            e.printStackTrace();
        }
    }
}