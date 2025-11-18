package com.example.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientManager {
    private static final ClientManager instance = new ClientManager();

    // Dùng CopyOnWriteArrayList để an toàn khi vừa duyệt vừa xóa (Thread-safe)
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // Client nào đang nắm quyền? (null = chưa ai)
    private ClientHandler currentController = null;

    private ClientManager() {
    }

    public static ClientManager getInstance() {
        return instance;
    }

    public void addClient(ClientHandler client) {
        clients.add(client);
        System.out
                .println("[Manager] Client added. ID: " + (clients.size() - 1) + " | IP: " + client.getClientAddress());
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        if (currentController == client) {
            currentController = null;
            System.out.println("[Manager] Controller disconnected. Control is now FREE.");
        }
    }

    /**
     * Lấy danh sách để in ra màn hình console
     */
    public List<ClientHandler> getClients() {
        return clients;
    }

    /**
     * Cấp quyền cho client theo ID (index trong list)
     */
    public synchronized void grantControl(int clientId) {
        if (clientId < 0 || clientId >= clients.size()) {
            System.out.println("[Error] Client ID not found: " + clientId);
            return;
        }

        ClientHandler newController = clients.get(clientId);

        // 1. Thông báo cho người cũ biết họ bị mất quyền (nếu có)
        if (currentController != null && currentController != newController) {
            currentController.sendMessage("ROLE VIEWER");
        }

        // 2. Gán quyền mới
        currentController = newController;

        // 3. Thông báo cho người mới
        currentController.sendMessage("ROLE CONTROLLER");
        System.out.println("[Manager] Permission GRANTED to Client ID: " + clientId);
    }

    /**
     * Thu hồi quyền điều khiển từ client hiện tại (tất cả trở thành Viewer).
     */
    public synchronized void revokeAllControl() {
        if (currentController != null) {
            // 1. Thông báo cho người điều khiển hiện tại biết rằng họ bị tước quyền
            currentController.sendMessage("ROLE VIEWER");

            System.out.println("[Manager] Control revoked from: " + currentController.getClientAddress());

            // 2. Giải phóng quyền điều khiển
            currentController = null;
        } else {
            System.out.println("[Manager] Control is already free (No active controller).");
        }
    }

    /**
     * Kiểm tra xem client có phải là người được cấp quyền không
     */
    public boolean isController(ClientHandler client) {
        return currentController == client;
    }
}