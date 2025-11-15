package com.example.client;

import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RemoteMouseClient {

    private final PrintWriter writer;

    /**
     * Khởi tạo RemoteMouseClient với Socket đã kết nối.
     * Tự động tạo PrintWriter với autoFlush=true để gửi lệnh ngay lập tức.
     */
    // public RemoteMouseClient(Socket socket) throws Exception {
    // this.writer = new PrintWriter(socket.getOutputStream(), true); //
    // autoFlush=true
    // }

    /**
     * SỬA LỖI: Khởi tạo RemoteMouseClient với PrintWriter đã có (dùng chung).
     * Bỏ logic tạo PrintWriter ở đây.
     */
    public RemoteMouseClient(PrintWriter writer) {
        this.writer = writer;
    }

    /** Di chuyển chuột tới (x, y) trên server */
    public void moveMouse(int x, int y) {
        sendCommand("MOUSE_MOVE " + x + " " + y);
    }

    /** Click chuột trái */
    public void clickLeft() {
        sendCommand("CLICK_LEFT");
    }

    /** Click chuột phải */
    public void clickRight() {
        sendCommand("CLICK_RIGHT");
    }

    /** Scroll chuột, positive = lên, negative = xuống */
    public void scroll(int amount) {
        sendCommand("SCROLL " + amount);
    }

    public void mousePressLeft() {
        sendCommand("MOUSE_PRESS LEFT");
    }

    public void mouseReleaseLeft() {
        sendCommand("MOUSE_RELEASE LEFT");
    }

    public void mousePressRight() {
        sendCommand("MOUSE_PRESS RIGHT");
    }

    public void mouseReleaseRight() {
        sendCommand("MOUSE_RELEASE RIGHT");
    }

    /** Gửi lệnh tới server */
    private void sendCommand(String cmd) {
        if (writer != null) {
            writer.println(cmd); // autoFlush=true đảm bảo gửi ngay
        }
    }
}
