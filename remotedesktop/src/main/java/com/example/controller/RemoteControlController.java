package com.example.controller;

import com.example.client.RemoteMouseClient;
import com.example.model.ConnectionInf;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.Socket;

import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class RemoteControlController {

    @FXML
    private Label connectionStatusLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Button disconnectButton;

    private ConnectionInf connectionInfo; // lưu thông tin kết nối hiện tại
    private volatile boolean running = true; // trạng thái luồng nhận ảnh
    private Thread imageReceiverThread; // luồng nhận ảnh từ server
    private PrintWriter writer; // để gửi lệnh tới server
    private RemoteMouseClient remoteMouseClient; // gửi lệnh chuột/phím
    private int serverWidth = 0, serverHeight = 0; // kích thước màn hình server

    // public void initializeConnection(ConnectionInf info) {
    // this.connectionInfo = info;
    // connectionStatusLabel.setText("Connected to: " + info.getIpAddress() + ":" +
    // info.getPort());

    // // startImageReceiver();
    // try {
    // Socket socket = connectionInfo.getSocket();

    // // Khởi tạo writer chung để dùng gửi lệnh
    // // luồng gửi lệnh dạng text
    // writer = new PrintWriter(socket.getOutputStream(), true);

    // // Khởi tạo RemoteMouseClient
    // // remoteMouseClient = new RemoteMouseClient(socket);
    // // Khởi tạo sau khi kết nối thành công
    // remoteMouseClient = new RemoteMouseClient(socket);

    // // Sự kiện chuột
    // imageView.setOnMouseMoved(event -> {
    // int x = (int) event.getX();
    // int y = (int) event.getY();
    // remoteMouseClient.moveMouse(x, y);
    // });

    // imageView.setOnMouseDragged(event -> {
    // int x = (int) event.getX();
    // int y = (int) event.getY();
    // remoteMouseClient.moveMouse(x, y);
    // });

    // imageView.setOnMouseClicked(event -> {
    // if (event.isPrimaryButtonDown())
    // remoteMouseClient.clickLeft();
    // if (event.isSecondaryButtonDown())
    // remoteMouseClient.clickRight();
    // });
    // // Chia 40 để giảm nhạy
    // imageView.setOnScroll(event -> remoteMouseClient.scroll((int)
    // event.getDeltaY() / 40));

    // // Bắt đầu luồng nhận ảnh từ Server
    // startImageReceiver(socket);

    // // Thiết lập sự kiện chuột cho điều khiển
    // setupMouseEvents();

    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    public void initializeConnection(ConnectionInf info) {
        this.connectionInfo = info;
        connectionStatusLabel.setText("Connected to: " + info.getIpAddress() + ":" + info.getPort());

        try {
            Socket socket = connectionInfo.getSocket();

            // Khởi tạo writer chung để gửi lệnh text
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Khởi tạo RemoteMouseClient để điều khiển chuột
            remoteMouseClient = new RemoteMouseClient(socket);

            // Thiết lập các sự kiện chuột & scroll
            setupMouseEvents();

            // Bắt đầu luồng nhận ảnh từ server
            startImageReceiver(socket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Luồng nhận hình ảnh từ server và hiển thị lên ImageView.
     */

    private void startImageReceiver(Socket socket) {
        imageReceiverThread = new Thread(() -> {
            try (InputStream in = socket.getInputStream()) {

                // Gửi lệnh bắt đầu stream ảnh
                writer.println("START_SCREEN");

                while (running) {
                    // Đọc header text (byte theo byte đến \n)
                    StringBuilder sb = new StringBuilder();
                    int c;
                    while ((c = in.read()) != -1) {
                        if (c == '\n')
                            break;
                        sb.append((char) c);
                    }
                    if (sb.length() == 0)
                        break; // socket đóng

                    String header = sb.toString().trim();

                    // Nhận screen size từ server
                    if (header.startsWith("SCREEN_SIZE")) {
                        String[] parts = header.split(" ");
                        serverWidth = Integer.parseInt(parts[1]);
                        serverHeight = Integer.parseInt(parts[2]);
                        continue;
                    }

                    if (header.startsWith("IMG_START")) {
                        int size = Integer.parseInt(header.split(" ")[1]);
                        byte[] buffer = new byte[size];

                        // đọc chính xác số byte ảnh
                        int read = 0;
                        while (read < size) {
                            int r = in.read(buffer, read, size - read);
                            if (r == -1)
                                break;
                            read += r;
                        }

                        // Cập nhật ảnh trên UI Thread
                        Image img = new Image(new java.io.ByteArrayInputStream(buffer));
                        Platform.runLater(() -> imageView.setImage(img));
                    }
                }

            } catch (Exception e) {
                if (running)
                    System.err.println("Lost connection: " + e.getMessage());
            } finally {
                running = false;
                Platform.runLater(() -> {
                    connectionStatusLabel.setText("Disconnected");
                    imageView.setImage(null);
                });
            }
        }, "ImageReceiverThread");

        imageReceiverThread.setDaemon(true);
        imageReceiverThread.start();
    }

    // private void setupMouseEvents() {
    // // di chuyển + drag
    // imageView.setOnMouseMoved(this::sendMouseMove);
    // imageView.setOnMouseDragged(this::sendMouseMove);

    // // NHẤN (gửi click ngay khi nhấn). Dùng getButton() thay vì
    // // isPrimaryButtonDown()
    // imageView.setOnMousePressed(event -> {
    // if (remoteMouseClient == null || !running)
    // return;

    // MouseButton btn = event.getButton();
    // System.out.println("Local mouse pressed: " + btn); // debug

    // if (btn == MouseButton.PRIMARY) {
    // remoteMouseClient.clickLeft();
    // System.out.println("Sent CLICK_LEFT");
    // } else if (btn == MouseButton.SECONDARY) {
    // remoteMouseClient.clickRight();
    // System.out.println("Sent CLICK_RIGHT");
    // }

    // // ngăn các handler khác (context menu...) xử lý sự kiện này
    // event.consume();
    // });
    // // Vô hiệu hóa context menu request để right-click không bị ăn bởi menu
    // imageView.setOnContextMenuRequested((ContextMenuEvent e) -> {
    // e.consume();
    // });

    // // Scroll
    // imageView.setOnScroll(event -> {
    // if (remoteMouseClient == null || !running)
    // return;
    // int amount = (int) (event.getDeltaY() / 40);
    // remoteMouseClient.scroll(amount);
    // System.out.println("Sent SCROLL " + amount);
    // event.consume();
    // });
    // }
    private void setupMouseEvents() {
        imageView.setOnMousePressed(event -> {
            if (remoteMouseClient == null || !running)
                return;
            MouseButton btn = event.getButton();

            if (btn == MouseButton.PRIMARY) {
                remoteMouseClient.mousePressLeft(); // giữ chuột trái
            } else if (btn == MouseButton.SECONDARY) {
                remoteMouseClient.mousePressRight(); // giữ chuột phải
            }
            event.consume();
        });

        imageView.setOnMouseReleased(event -> {
            if (remoteMouseClient == null || !running)
                return;
            MouseButton btn = event.getButton();

            if (btn == MouseButton.PRIMARY) {
                remoteMouseClient.mouseReleaseLeft(); // nhả chuột trái
            } else if (btn == MouseButton.SECONDARY) {
                remoteMouseClient.mouseReleaseRight(); // nhả chuột phải
            }
            event.consume();
        });

        imageView.setOnMouseDragged(this::sendMouseMove);
        imageView.setOnMouseMoved(this::sendMouseMove);

        imageView.setOnScroll(event -> {
            if (remoteMouseClient == null || !running)
                return;
            int amount = (int) (event.getDeltaY() / 40);
            remoteMouseClient.scroll(amount);
            event.consume();
        });
    }

    /**
     * Gửi lệnh di chuyển chuột tới server (mapping chính xác).
     */
    private void sendMouseMove(MouseEvent event) {
        if (remoteMouseClient == null || !running)
            return;
        if (serverWidth <= 0 || serverHeight <= 0)
            return;

        // Lấy ảnh hiện đang được hiển thị trên ImageView ở client.
        Image currentImage = imageView.getImage();
        if (currentImage == null)
            return;

        // kích thước hiển thị của ImageView trên client.
        double viewWidth = imageView.getBoundsInLocal().getWidth();
        double viewHeight = imageView.getBoundsInLocal().getHeight();
        if (viewWidth <= 0 || viewHeight <= 0)
            return;

        // kích thước thực của ảnh màn hình server được gửi về.
        double imgWidth = currentImage.getWidth();
        double imgHeight = currentImage.getHeight();
        if (imgWidth <= 0 || imgHeight <= 0)
            return;

        double viewRatio = viewWidth / viewHeight;// tỷ lệ ImageView trên client
        double imgRatio = imgWidth / imgHeight;// tỷ lệ ảnh gốc server

        double displayedWidth, displayedHeight;
        double offsetX = 0, offsetY = 0;

        if (viewRatio > imgRatio) {
            // letterbox ngang (ảnh cao đầy)
            displayedHeight = viewHeight;
            displayedWidth = imgRatio * displayedHeight;
            offsetX = (viewWidth - displayedWidth) / 2.0;
        } else {
            // letterbox dọc (ảnh rộng đầy)
            displayedWidth = viewWidth;
            displayedHeight = displayedWidth / imgRatio;
            offsetY = (viewHeight - displayedHeight) / 2.0;
        }

        double imageX = event.getX() - offsetX;
        double imageY = event.getY() - offsetY;

        if (imageX < 0 || imageY < 0 || imageX > displayedWidth || imageY > displayedHeight)
            return;

        // Mỗi pixel hiển thị tương ứng ? pixel gốc
        double scaleX = imgWidth / displayedWidth;
        double scaleY = imgHeight / displayedHeight;

        // Quy đổi ngược về pixel gốc trong ảnh
        double imgPixelX = imageX * scaleX;
        double imgPixelY = imageY * scaleY;

        // Quy đổi sang tọa độ thật của màn hình server
        /*
         * Nếu click ở x% vị trí ngang của ảnh
         * thì chuột cũng phải di chuyển đến x% vị trí ngang của màn hình server thật.
         */
        double screenX = (imgPixelX / imgWidth) * serverWidth;
        double screenY = (imgPixelY / imgHeight) * serverHeight;

        int serverX = (int) Math.round(screenX);
        int serverY = (int) Math.round(screenY);

        remoteMouseClient.moveMouse(serverX, serverY);
    }

    /**
     * Ngắt kết nối an toàn:
     * - Dừng luồng nhận ảnh
     * - Gửi lệnh DISCONNECT tới server
     * - Đóng socket
     * - Dọn dẹp UI
     */
    @FXML
    private void disconnectFromServer() {
        running = false;

        try {
            if (writer != null)
                writer.println("DISCONNECT");
        } catch (Exception ignored) {
        }

        try {
            if (connectionInfo.getSocket() != null && !connectionInfo.getSocket().isClosed())
                connectionInfo.getSocket().close();
        } catch (Exception ignored) {
        }

        Platform.runLater(() -> {
            connectionStatusLabel.setText("Disconnected.");
            imageView.setImage(null);
        });
    }
}