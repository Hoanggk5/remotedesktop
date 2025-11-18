package com.example.server;

import java.io.DataOutputStream;
import java.io.PrintWriter;

/**
 * Lớp Runnable này chạy trên một luồng riêng
 * chỉ để chụp và gửi hình ảnh.
 */
public class ScreenStreamer implements Runnable {

    private volatile boolean streaming = true;
    private final PrintWriter writer;
    private final DataOutputStream dataOut;
    private final Object outputLock; // Dùng chung khóa với ClientHandler

    public ScreenStreamer(PrintWriter writer, DataOutputStream dataOut, Object outputLock) {
        this.writer = writer;
        this.dataOut = dataOut;
        this.outputLock = outputLock;
    }

    @Override
    public void run() {
        try {
            while (streaming) {
                byte[] imageBytes = ScreenCapturer.captureScreen();

                // Phải khóa để đảm bảo header text và data nhị phân
                // được gửi đi liền mạch, không bị luồng khác xen vào.
                synchronized (outputLock) {
                    writer.println("IMG_START " + imageBytes.length);
                    writer.flush(); // Phải flush writer trước khi dùng dataOut
                    dataOut.write(imageBytes);
                    dataOut.flush();
                }

                Thread.sleep(33); // ~30 FPS
            }
        } catch (Exception e) {
            System.err.println("Streamer error: " + e.getMessage());
        } finally {
            streaming = false;
        }
    }

    public void stop() {
        this.streaming = false;
    }
}