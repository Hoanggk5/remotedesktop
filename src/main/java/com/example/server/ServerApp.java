
package com.example.server;

import java.awt.AWTException;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

public class ServerApp {

    public static void main(String[] args) {
        int port = 5000;

        // --- IN THÔNG TIN IP CỦA SERVER ---
        String localIp = getLocalIpAddress();
        System.out.println("Local Server IP: " + localIp);
        // ----------------------------------

        // --- LUỒNG 1: LẮNG NGHE KẾT NỐI ---
        Thread connectionThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server started on port " + port);
                System.out.println("Type 'help' for commands.");

                while (true) {
                    Socket socket = serverSocket.accept();

                    ClientHandler handler = new ClientHandler(socket);
                    ClientManager.getInstance().addClient(handler);
                    new Thread(handler).start();
                }
            } catch (Exception e) {
                // Chỉ in lỗi nếu nó không phải là lỗi đóng socket thông thường
                if (!(e instanceof IOException && e.getMessage().contains("socket closed"))) {
                    e.printStackTrace();
                }
            }
        });
        connectionThread.setDaemon(true);
        connectionThread.start();

        // --- LUỒNG 2 (MAIN): XỬ LÝ LỆNH CỦA ADMIN ---
        handleAdminCommands();
    }

    /**
     * Lấy địa chỉ IP cục bộ của Server (thường là IP trong mạng LAN).
     * 
     * @return Địa chỉ IP dưới dạng chuỗi hoặc "Unknown" nếu gặp lỗi.
     */
    private static String getLocalIpAddress() {
        try {
            // Lấy địa chỉ cục bộ. getHostAddress() trả về IP.
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static void handleAdminCommands() {
        // ... (phần handleAdminCommands() giữ nguyên như code của bạn)
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Server> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "list":
                    printClientList();
                    break;

                case "grant":
                    if (parts.length < 2) {
                        System.out.println("Usage: grant <id>");
                    } else {
                        try {
                            int id = Integer.parseInt(parts[1]);
                            ClientManager.getInstance().grantControl(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid ID format.");
                        }
                    }
                    break;

                case "revoke":
                    ClientManager.getInstance().revokeAllControl();
                    break;

                case "help":
                    System.out.println("Commands: list, grant <id>, revoke, exit");
                    break;

                case "exit":
                    System.out.println("Shutting down server...");
                    System.exit(0);
                    break;

                default:
                    if (!cmd.isEmpty())
                        System.out.println("Unknown command.");
            }
        }
    }

    private static void printClientList() {
        List<ClientHandler> list = ClientManager.getInstance().getClients();
        System.out.println("--- Connected Clients ---");
        for (int i = 0; i < list.size(); i++) {
            ClientHandler c = list.get(i);
            boolean isAdmin = ClientManager.getInstance().isController(c);
            System.out.printf("ID: %d | IP: %s | Role: %s%n",
                    i, c.getClientAddress(), (isAdmin ? "CONTROLLER" : "VIEWER"));
        }
        System.out.println("-------------------------");
    }
}

// // Cổng (PORT) mà server sẽ lắng nghe
// private final static int PORT = 5000;

// /**
// * Finds the local IPv4 address, excluding loopback interfaces.
// * (Tìm địa chỉ IPv4 cục bộ, loại trừ giao diện loopback. Đây là IP Client
// * Windows sẽ kết nối tới.)
// */

// private static String getLocalIpAddress() {
// try {
// // Duyệt qua tất cả các giao diện mạng (network interfaces)
// Enumeration<NetworkInterface> networkInterfaces =
// NetworkInterface.getNetworkInterfaces();
// while (networkInterfaces.hasMoreElements()) {
// NetworkInterface ni = networkInterfaces.nextElement();

// // Bỏ qua các giao diện ảo, loopback, hoặc không hoạt động
// if (ni.isLoopback() || !ni.isUp()) {
// continue;
// }

// // Lấy danh sách địa chỉ IP từ giao diện này
// Enumeration<InetAddress> addresses = ni.getInetAddresses();
// while (addresses.hasMoreElements()) {
// InetAddress addr = addresses.nextElement();

// // Chỉ lấy địa chỉ IPv4
// if (addr instanceof Inet4Address) {
// // Trả về IP hợp lệ đầu tiên được tìm thấy
// return addr.getHostAddress();
// }
// }
// }
// // Nếu không tìm thấy, trả về loopback (cho trường hợp lỗi)
// return "127.0.0.1";
// } catch (SocketException e) {
// e.printStackTrace();
// return "127.0.0.1";
// }
// }

// public static void main(String[] args) {
// try (ServerSocket serverSocket = new ServerSocket(PORT)) {

// // Tự động lấy và in IP khi server khởi động
// String serverIP = getLocalIpAddress();
// System.out.println("=============================================");
// System.out.println("SERVER IS RUNNING!");
// System.out.println("Client should connect to IP: " + serverIP);
// System.out.println("Server is listening on port: " + PORT);
// System.out.println("=============================================");

// // Vòng lặp vĩnh cửu để Server luôn sẵn sàng chấp nhận Client mới
// while (true) {
// System.out.println("\nWaiting for a new client to connect...");

// // Chờ client kết nối (lệnh này sẽ blocking)
// Socket clientSocket = serverSocket.accept();
// System.out.println("Client connected: " +
// clientSocket.getInetAddress().getHostAddress());

// // Sử dụng Đa luồng: Tạo một luồng (Thread) mới để xử lý Client này,
// // sau đó Server chính quay lại chờ Client tiếp theo ngay lập tức.
// // ClientHandler clientHandler = new ClientHandler(clientSocket);
// // Thread thread = new Thread(clientHandler);
// // thread.start();

// try {
// ClientHandler handler = new ClientHandler(clientSocket);
// new Thread(handler).start();
// } catch (Exception e) {
// e.printStackTrace();
// }
// }

// } catch (IOException e) {
// System.err.println("Could not listen on port " + PORT + ". Error: " +
// e.getMessage());
// e.printStackTrace();
// }
// }
// }
