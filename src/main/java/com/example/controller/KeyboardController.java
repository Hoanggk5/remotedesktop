package com.example.controller;

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class KeyboardController {

    private final Robot robot;

    public KeyboardController(Robot robot) {
        this.robot = robot;
        System.out.println("KeyboardController (Real Logic) initialized.");
    }

    /**
     * Nhấn một phím (dựa trên mã phím ảo của Java AWT).
     * * @param keyCode (VD: KeyEvent.VK_A, KeyEvent.VK_SHIFT)
     */
    public void keyPress(int keyCode) {
        try {
            robot.keyPress(keyCode);
        } catch (Exception e) {
            System.err.println("Error pressing key code: " + keyCode + " - " + e.getMessage());
        }
    }

    /**
     * Nhả một phím (dựa trên mã phím ảo của Java AWT).
     * * @param keyCode (VD: KeyEvent.VK_A, KeyEvent.VK_SHIFT)
     */
    public void keyRelease(int keyCode) {
        try {
            robot.keyRelease(keyCode);
        } catch (Exception e) {
            System.err.println("Error releasing key code: " + keyCode + " - " + e.getMessage());
        }
    }

    // --- PHẦN THÊM VÀO ---
    /**
     * Gõ một phím (nhấn và thả ngay lập tức).
     * * @param keyCode (VD: KeyEvent.VK_A)
     */
    public void typeKey(int keyCode) {
        try {
            robot.keyPress(keyCode);
            // Bạn có thể thêm một độ trễ rất nhỏ ở đây nếu cần
            // robot.delay(10);
            robot.keyRelease(keyCode);
        } catch (Exception e) {
            System.err.println("Error typing key code: " + keyCode + " - " + e.getMessage());
        }
    }
    // --- KẾT THÚC PHẦN THÊM VÀO ---
}