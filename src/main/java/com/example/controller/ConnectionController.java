package com.example.controller;

import java.io.IOException;

import com.example.model.ConnectionInf;
import com.example.service.ConnectionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ConnectionController {

    @FXML
    private TextField ipTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private Button connectButton;
    @FXML
    private Label statusLabel;

    private static final int DEFAULT_PORT = 5000;
    private final ConnectionService connectionService = new ConnectionService();

    @FXML
    private void connectToServer() {
        connectButton.setDisable(true);
        updateStatus("Connecting...", "orange");

        String ip = ipTextField.getText().trim();
        String portText = portTextField.getText().trim();
        int port = portText.isEmpty() ? DEFAULT_PORT : parsePort(portText);
        if (port == -1 || ip.isEmpty()) {
            updateStatus("Invalid input. Check IP or Port.", "red");
            connectButton.setDisable(false);
            return;
        }

        new Thread(() -> {
            try {
                ConnectionInf info = connectionService.connect(ip, port);
                updateStatus("Connected to " + ip + ":" + port, "green");

                connectionService.performHandshake(info);
                // info.disconnect();
                // updateStatus("Session closed.", "black");
                Platform.runLater(() -> openRemoteControlWindow(info));

            } catch (Exception e) {
                updateStatus("Connection failed: " + e.getMessage(), "red");
            } finally {
                Platform.runLater(() -> connectButton.setDisable(false));
            }
        }).start();
    }

    private int parsePort(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updateStatus(String message, String color) {
        Platform.runLater(() -> {
            statusLabel.setText("STATUS: " + message);
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        });
    }

    /**
     * Mở cửa sổ điều khiển chính sau khi kết nối thành công.
     */
    private void openRemoteControlWindow(ConnectionInf info) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/remote_control.fxml"));
            // Tải FXML và tạo Scene
            Scene scene = new Scene(loader.load());

            // Lấy controller mới và truyền thông tin kết nối
            RemoteControlController controller = loader.getController();
            controller.initializeConnection(info);

            Stage stage = (Stage) connectButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Remote Desktop Control");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            updateStatus("ERROR: Failed to load remote control view.", "red");
        }
    }
}