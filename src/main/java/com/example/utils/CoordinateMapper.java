package com.example.utils;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

public class CoordinateMapper {

    /**
     * Chuyển đổi tọa độ MouseEvent trên ImageView (có letterbox) sang tọa độ màn
     * hình server.
     * Trả về null nếu tọa độ nằm ngoài vùng ảnh.
     */
    public static Point2D map(MouseEvent event, Image currentImage,
            double viewWidth, double viewHeight,
            int serverWidth, int serverHeight) {

        if (currentImage == null || viewWidth <= 0 || viewHeight <= 0 || serverWidth <= 0) {
            return null;
        }

        double imgWidth = currentImage.getWidth();
        double imgHeight = currentImage.getHeight();
        if (imgWidth <= 0 || imgHeight <= 0) {
            return null;
        }

        double viewRatio = viewWidth / viewHeight;
        double imgRatio = imgWidth / imgHeight;

        double displayedWidth, displayedHeight;
        double offsetX = 0, offsetY = 0;

        if (viewRatio > imgRatio) {
            displayedHeight = viewHeight;
            displayedWidth = imgRatio * displayedHeight;
            offsetX = (viewWidth - displayedWidth) / 2.0;
        } else {
            displayedWidth = viewWidth;
            displayedHeight = displayedWidth / imgRatio;
            offsetY = (viewHeight - displayedHeight) / 2.0;
        }

        double imageX = event.getX() - offsetX;
        double imageY = event.getY() - offsetY;

        if (imageX < 0 || imageY < 0 || imageX > displayedWidth || imageY > displayedHeight) {
            return null; // Click ra ngoài lề (letterbox)
        }

        double scaleX = imgWidth / displayedWidth;
        double scaleY = imgHeight / displayedHeight;

        double imgPixelX = imageX * scaleX;
        double imgPixelY = imageY * scaleY;

        double screenX = (imgPixelX / imgWidth) * serverWidth;
        double screenY = (imgPixelY / imgHeight) * serverHeight;

        int serverX = (int) Math.round(screenX);
        int serverY = (int) Math.round(screenY);

        return new Point2D(serverX, serverY);
    }
}
