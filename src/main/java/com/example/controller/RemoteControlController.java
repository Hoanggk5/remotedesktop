package com.example.controller;

import java.io.File;

import com.example.model.ConnectionInf;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import com.example.service.ClientService; // Mới
import com.example.utils.CoordinateMapper;

import javafx.geometry.Point2D;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

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

        // --- DRAG & DROP FILE (Code của bạn đã đúng) ---
        imageView.setOnDragOver(event -> {
            if (event.getGestureSource() != imageView &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        imageView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                System.out.println("Dropped file: " + file.getAbsolutePath());

                // SỬA LỖI: Gọi hàm 2-Socket
                clientService.startFileTransfer(file);

                success = true;
            }
            event.setDropCompleted(success);
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
