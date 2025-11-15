package com.example.model;

import java.net.Socket;

public class ConnectionInf {
    private String ipAddress;
    private int port;
    private Socket socket;
    private boolean connected;

    public ConnectionInf(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.connected = false;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        this.connected = (socket != null && socket.isConnected());
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                // socket.close();
            }
        } catch (Exception e) {
            System.err.println("[Connection] Error closing socket: " + e.getMessage());
        }
        connected = false;
    }
}
