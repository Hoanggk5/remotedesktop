package com.example.controller;

import com.example.model.ConnectionInf;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import com.example.service.ClientService; // Mới
import com.example.utils.CoordinateMapper;

import javafx.geometry.Point2D;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class RemoteControlController {

    @FXML
    private Label connectionStatusLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Button disconnectButton;

    // Controller chỉ cần giữ tham chiếu tới Service
    private ClientService clientService;

    public void initializeConnection(ConnectionInf info) {
        this.clientService = new ClientService();

        // 1. Gán (Bind) các thuộc tính của UI vào thuộc tính của Service
        // Khi Service cập nhật 'currentImage', ImageView sẽ tự thay đổi
        imageView.imageProperty().bind(clientService.currentImageProperty());

        // Khi Service cập nhật 'connectionStatus', Label sẽ tự thay đổi
        connectionStatusLabel.textProperty().bind(clientService.connectionStatusProperty());

        // 2. Thiết lập sự kiện
        setupMouseEvents();
        setupKeyboardEvents();
        // 3. Bắt đầu kết nối
        clientService.connect(info);
    }

    private void setupMouseEvents() {
        imageView.setOnMousePressed(event -> {
            if (clientService == null || !clientService.isRunning())
                return;

            // imageView.requestFocus();

            // if (event.getButton() == MouseButton.PRIMARY) {
            // clientService.getRemoteMouseClient().mousePressLeft();
            // } else if (event.getButton() == MouseButton.SECONDARY) {
            // clientService.getRemoteMouseClient().mousePressRight();
            // }
            // --- SỬA ĐỔI ---
            // Gọi 'map' để kiểm tra giới hạn.
            Point2D serverCoords = CoordinateMapper.map(
                    event, imageView.getImage(),
                    imageView.getBoundsInLocal().getWidth(),
                    imageView.getBoundsInLocal().getHeight(),
                    clientService.getServerWidth(),
                    clientService.getServerHeight());

            // Chỉ xử lý click (và cấp focus) nếu serverCoords != null (tức là click hợp lệ)
            if (serverCoords != null) {
                imageView.requestFocus();

                if (event.getButton() == MouseButton.PRIMARY) {
                    clientService.getRemoteMouseClient().mousePressLeft();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    clientService.getRemoteMouseClient().mousePressRight();
                }
            }
            event.consume();
        });

        imageView.setOnMouseReleased(event -> {
            if (clientService == null || !clientService.isRunning())
                return;

            // if (event.getButton() == MouseButton.PRIMARY) {
            // clientService.getRemoteMouseClient().mouseReleaseLeft();
            // } else if (event.getButton() == MouseButton.SECONDARY) {
            // clientService.getRemoteMouseClient().mouseReleaseRight();
            // }
            // Chỉ gửi lệnh 'release' nếu vị trí hợp lệ
            Point2D serverCoords = CoordinateMapper.map(
                    event, imageView.getImage(),
                    imageView.getBoundsInLocal().getWidth(),
                    imageView.getBoundsInLocal().getHeight(),
                    clientService.getServerWidth(),
                    clientService.getServerHeight());

            if (serverCoords != null) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    clientService.getRemoteMouseClient().mouseReleaseLeft();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    clientService.getRemoteMouseClient().mouseReleaseRight();
                }
            }
            event.consume();
        });

        imageView.setOnMouseDragged(this::sendMouseMove);
        imageView.setOnMouseMoved(this::sendMouseMove);

        imageView.setOnScroll(event -> {
            if (clientService == null || !clientService.isRunning())
                return;

            // Chuyển deltaY thành số bước scroll
            double rawDelta = event.getDeltaY(); // có thể ±2, ±3, ±40
            int steps = (int) Math.signum(rawDelta); // ±1

            if (steps != 0) {
                clientService.getRemoteMouseClient().scroll(steps);
                System.out.println("[Client] Scroll: " + steps);
            }

            event.consume();
        });

    }

    private void sendMouseMove(MouseEvent event) {
        if (clientService == null || !clientService.isRunning())
            return;

        // Logic tính toán phức tạp đã được chuyển đi
        Point2D serverCoords = CoordinateMapper.map(
                event,
                imageView.getImage(),
                imageView.getBoundsInLocal().getWidth(),
                imageView.getBoundsInLocal().getHeight(),
                clientService.getServerWidth(),
                clientService.getServerHeight());

        if (serverCoords != null) {
            clientService.getRemoteMouseClient().moveMouse(
                    (int) serverCoords.getX(),
                    (int) serverCoords.getY());
        }
    }

    private void setupKeyboardEvents() {
        // Chúng ta cần lắng nghe sự kiện trên toàn bộ Scene,
        // nhưng gắn vào imageView cũng hoạt động miễn là nó được focus.

        imageView.setOnKeyPressed(event -> {
            if (clientService == null || !clientService.isRunning())
                return;

            // event.getCode().getCode() sẽ trả về mã phím ảo (virtual key code)
            // mà AWT Robot của server có thể hiểu (ví dụ: 65 cho phím 'A')
            int keyCode = event.getCode().getCode();
            clientService.getRemoteKeyboardClient().pressKey(keyCode);

            System.out.println("[Client] Key Pressed: " + keyCode); // Thêm log để debug
            event.consume();
        });

        imageView.setOnKeyReleased(event -> {
            if (clientService == null || !clientService.isRunning())
                return;

            int keyCode = event.getCode().getCode();
            clientService.getRemoteKeyboardClient().releaseKey(keyCode);

            System.out.println("[Client] Key Released: " + keyCode); // Thêm log để debug
            event.consume();
        });
    }

    @FXML
    private void disconnectFromServer() {
        if (clientService != null) {
            clientService.disconnect();
        }
        // UI (Label, ImageView) sẽ tự động dọn dẹp nhờ cơ chế Binding
    }
}

// import com.example.client.RemoteMouseClient;
// import com.example.model.ConnectionInf;
// import javafx.application.Platform;
// import javafx.fxml.FXML;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;

// import java.io.BufferedReader;
// import java.io.DataInputStream;
// import java.io.FileWriter;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.PrintWriter;
// import java.nio.charset.StandardCharsets;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.net.Socket;

// import javafx.scene.input.ContextMenuEvent;
// import javafx.scene.input.MouseButton;
// import javafx.scene.input.MouseEvent;

// public class RemoteControlController {

// @FXML
// private Label connectionStatusLabel;
// @FXML
// private ImageView imageView;
// @FXML
// private Button disconnectButton;

// private ConnectionInf connectionInfo; // lưu thông tin kết nối hiện tại
// private volatile boolean running = true; // trạng thái luồng nhận ảnh
// private Thread imageReceiverThread; // luồng nhận ảnh từ server
// private PrintWriter writer; // để gửi lệnh tới server
// private RemoteMouseClient remoteMouseClient; // gửi lệnh chuột/phím
// private int serverWidth = 0, serverHeight = 0; // kích thước màn hình server

// public void initializeConnection(ConnectionInf info) {
// this.connectionInfo = info;
// connectionStatusLabel.setText("Connected to: " + info.getIpAddress() + ":" +
// info.getPort());

// try {
// Socket socket = connectionInfo.getSocket();

// // Khởi tạo writer chung để gửi lệnh text
// writer = new PrintWriter(socket.getOutputStream(), true);

// // Khởi tạo RemoteMouseClient để điều khiển chuột
// remoteMouseClient = new RemoteMouseClient(socket);

// // Thiết lập các sự kiện chuột & scroll
// setupMouseEvents();

// // Bắt đầu luồng nhận ảnh từ server
// startImageReceiver(socket);

// } catch (Exception e) {
// e.printStackTrace();
// }
// }

// /**
// * Luồng nhận hình ảnh từ server và hiển thị lên ImageView.
// */

// private void startImageReceiver(Socket socket) {
// imageReceiverThread = new Thread(() -> {
// try (InputStream in = socket.getInputStream()) {

// // Gửi lệnh bắt đầu stream ảnh
// writer.println("START_SCREEN");

// while (running) {
// // Đọc header text (byte theo byte đến \n)
// StringBuilder sb = new StringBuilder();
// int c;
// while ((c = in.read()) != -1) {
// if (c == '\n')
// break;
// sb.append((char) c);
// }
// if (sb.length() == 0)
// break; // socket đóng

// String header = sb.toString().trim();

// // Nhận screen size từ server
// if (header.startsWith("SCREEN_SIZE")) {
// String[] parts = header.split(" ");
// serverWidth = Integer.parseInt(parts[1]);
// serverHeight = Integer.parseInt(parts[2]);
// continue;
// }

// if (header.startsWith("IMG_START")) {
// int size = Integer.parseInt(header.split(" ")[1]);
// byte[] buffer = new byte[size];

// // đọc chính xác số byte ảnh
// int read = 0;
// while (read < size) {
// int r = in.read(buffer, read, size - read);
// if (r == -1)
// break;
// read += r;
// }

// // Cập nhật ảnh trên UI Thread
// Image img = new Image(new java.io.ByteArrayInputStream(buffer));
// Platform.runLater(() -> imageView.setImage(img));
// }
// }

// } catch (Exception e) {
// if (running)
// System.err.println("Lost connection: " + e.getMessage());
// } finally {
// running = false;
// Platform.runLater(() -> {
// connectionStatusLabel.setText("Disconnected");
// imageView.setImage(null);
// });
// }
// }, "ImageReceiverThread");

// imageReceiverThread.setDaemon(true);
// imageReceiverThread.start();
// }

// private void setupMouseEvents() {
// imageView.setOnMousePressed(event -> {
// if (remoteMouseClient == null || !running)
// return;
// MouseButton btn = event.getButton();

// if (btn == MouseButton.PRIMARY) {
// remoteMouseClient.mousePressLeft(); // giữ chuột trái
// } else if (btn == MouseButton.SECONDARY) {
// remoteMouseClient.mousePressRight(); // giữ chuột phải
// }
// event.consume();
// });

// imageView.setOnMouseReleased(event -> {
// if (remoteMouseClient == null || !running)
// return;
// MouseButton btn = event.getButton();

// if (btn == MouseButton.PRIMARY) {
// remoteMouseClient.mouseReleaseLeft(); // nhả chuột trái
// } else if (btn == MouseButton.SECONDARY) {
// remoteMouseClient.mouseReleaseRight(); // nhả chuột phải
// }
// event.consume();
// });

// imageView.setOnMouseDragged(this::sendMouseMove);
// imageView.setOnMouseMoved(this::sendMouseMove);

// imageView.setOnScroll(event -> {
// if (remoteMouseClient == null || !running)
// return;
// int amount = (int) (event.getDeltaY() / 40);
// remoteMouseClient.scroll(amount);
// event.consume();
// });
// }

// /**
// * Gửi lệnh di chuyển chuột tới server (mapping chính xác).
// */
// private void sendMouseMove(MouseEvent event) {
// if (remoteMouseClient == null || !running)
// return;
// if (serverWidth <= 0 || serverHeight <= 0)
// return;

// // Lấy ảnh hiện đang được hiển thị trên ImageView ở client.
// Image currentImage = imageView.getImage();
// if (currentImage == null)
// return;

// // kích thước hiển thị của ImageView trên client.
// double viewWidth = imageView.getBoundsInLocal().getWidth();
// double viewHeight = imageView.getBoundsInLocal().getHeight();
// if (viewWidth <= 0 || viewHeight <= 0)
// return;

// // kích thước thực của ảnh màn hình server được gửi về.
// double imgWidth = currentImage.getWidth();
// double imgHeight = currentImage.getHeight();
// if (imgWidth <= 0 || imgHeight <= 0)
// return;

// double viewRatio = viewWidth / viewHeight;// tỷ lệ ImageView trên client
// double imgRatio = imgWidth / imgHeight;// tỷ lệ ảnh gốc server

// double displayedWidth, displayedHeight;
// double offsetX = 0, offsetY = 0;

// if (viewRatio > imgRatio) {
// // letterbox ngang (ảnh cao đầy)
// displayedHeight = viewHeight;
// displayedWidth = imgRatio * displayedHeight;
// offsetX = (viewWidth - displayedWidth) / 2.0;
// } else {
// // letterbox dọc (ảnh rộng đầy)
// displayedWidth = viewWidth;
// displayedHeight = displayedWidth / imgRatio;
// offsetY = (viewHeight - displayedHeight) / 2.0;
// }

// double imageX = event.getX() - offsetX;
// double imageY = event.getY() - offsetY;

// if (imageX < 0 || imageY < 0 || imageX > displayedWidth || imageY >
// displayedHeight)
// return;

// // Mỗi pixel hiển thị tương ứng ? pixel gốc
// double scaleX = imgWidth / displayedWidth;
// double scaleY = imgHeight / displayedHeight;

// // Quy đổi ngược về pixel gốc trong ảnh
// double imgPixelX = imageX * scaleX;
// double imgPixelY = imageY * scaleY;

// // Quy đổi sang tọa độ thật của màn hình server
// /*
// * Nếu click ở x% vị trí ngang của ảnh
// * thì chuột cũng phải di chuyển đến x% vị trí ngang của màn hình server thật.
// */
// double screenX = (imgPixelX / imgWidth) * serverWidth;
// double screenY = (imgPixelY / imgHeight) * serverHeight;

// int serverX = (int) Math.round(screenX);
// int serverY = (int) Math.round(screenY);

// remoteMouseClient.moveMouse(serverX, serverY);
// }

// /**
// * Ngắt kết nối an toàn:
// * - Dừng luồng nhận ảnh
// * - Gửi lệnh DISCONNECT tới server
// * - Đóng socket
// * - Dọn dẹp UI
// */
// @FXML
// private void disconnectFromServer() {
// running = false;

// try {
// if (writer != null)
// writer.println("DISCONNECT");
// } catch (Exception ignored) {
// }

// try {
// if (connectionInfo.getSocket() != null &&
// !connectionInfo.getSocket().isClosed())
// connectionInfo.getSocket().close();
// } catch (Exception ignored) {
// }

// Platform.runLater(() -> {
// connectionStatusLabel.setText("Disconnected.");
// imageView.setImage(null);
// });
// }
// }