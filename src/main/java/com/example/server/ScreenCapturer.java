package com.example.server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScreenCapturer {

    public static byte[] captureScreen() throws AWTException, IOException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenImage = robot.createScreenCapture(screenRect);

        // Nén thành JPEG để tiết kiệm dung lượng
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(screenImage, "jpg", baos);
        return baos.toByteArray();
    }
}