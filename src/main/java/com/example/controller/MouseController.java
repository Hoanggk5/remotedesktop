package com.example.controller;

import java.awt.*;
import java.awt.event.InputEvent;

public class MouseController {

    private final Robot robot;

    public MouseController() throws AWTException {
        robot = new Robot();
    }

    public void move(int x, int y) {
        robot.mouseMove(x, y);
    }

    public void clickLeft() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void clickRight() {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    public void scroll(int amount) {
        robot.mouseWheel(amount);
    }

    public void mousePressLeft() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void mouseReleaseLeft() {
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void mousePressRight() {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
    }

    public void mouseReleaseRight() {
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }
}