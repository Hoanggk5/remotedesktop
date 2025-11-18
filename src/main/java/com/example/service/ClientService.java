package com.example.service;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.client.RemoteFileClient; // <-- Thêm import
import com.example.client.RemoteKeyboardClient;
import com.example.client.RemoteMouseClient;
import com.example.model.ConnectionInf;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class ClientService {
    private ConnectionInf connectionInfo;
    private volatile boolean running = true;
    private Thread imageReceiverThread;

    private PrintWriter writer; // Sẽ được dùng chung
    private RemoteMouseClient remoteMouseClient;
    private RemoteKeyboardClient remoteKeyboardClient;
    private RemoteFileClient remoteFileClient; // <-- 1. KHAI BÁO BỊ THIẾU

    private File fileUserSelected; // File đang chờ gửi

    // (Các property... giữ nguyên)
    private final StringProperty connectionStatus = new SimpleStringProperty("Disconnected");
    private final ObjectProperty<Image> currentImage = new SimpleObjectProperty<>(null);
    private int serverWidth = 0;
    private int serverHeight = 0;

    public void connect(ConnectionInf info) {
        this.connectionInfo = info;
        try {
            Socket socket = info.getSocket();

            // Tạo MỘT writer chung
            this.writer = new PrintWriter(socket.getOutputStream(), true);

            // Dùng chung 'writer' cho TẤT CẢ
            this.remoteMouseClient = new RemoteMouseClient(this.writer);
            this.remoteKeyboardClient = new RemoteKeyboardClient(this.writer);

            // 2. KHỞI TẠO BỊ THIẾU (dùng IP)
            this.remoteFileClient = new RemoteFileClient(info.getIpAddress());

            updateStatus("Connected to: " + info.getIpAddress() + ":" + info.getPort());
            startImageReceiver(socket);

        } catch (Exception e) {
            e.printStackTrace();
            updateStatus("Connection failed: " + e.getMessage());
        }
    }

    private void startImageReceiver(Socket socket) {
        imageReceiverThread = new Thread(() -> {
            try (InputStream in = socket.getInputStream()) {
                writer.println("START_SCREEN");

                while (running) {
                    // (Logic đọc header bằng sb.append((char) c) giữ nguyên...)
                    StringBuilder sb = new StringBuilder();
                    int c;
                    while ((c = in.read()) != -1) {
                        if (c == '\n')
                            break;
                        sb.append((char) c);
                    }
                    if (sb.length() == 0)
                        break;
                    String header = sb.toString().trim();

                    if (header.startsWith("SCREEN_SIZE")) {
                        String[] parts = header.split(" ");
                        serverWidth = Integer.parseInt(parts[1]);
                        serverHeight = Integer.parseInt(parts[2]);
                        continue;

                        // --- ĐÂY LÀ LOGIC BẠN ĐANG THIẾU ---
                    } else if (header.startsWith("ACCEPT_FILE_TRANSFER ")) {
                        try {
                            // 1. Đọc port mà server gửi
                            int filePort = Integer.parseInt(header.split(" ")[1]);
                            System.out.println("Server accepted transfer on port: " + filePort);

                            // 2. Chạy việc gửi file trên một luồng mới
                            if (this.fileUserSelected != null) {
                                new Thread(() -> {
                                    // 3. Gọi RemoteFileClient để kết nối Kênh 2
                                    remoteFileClient.sendFile(this.fileUserSelected, filePort);
                                    this.fileUserSelected = null;
                                }).start();
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to parse file port: " + e.getMessage());
                        }
                        // --- KẾT THÚC LOGIC BỊ THIẾU ---

                    } else if (header.startsWith("IMG_START")) {
                        // (Logic đọc buffer ảnh giữ nguyên)
                        int size = Integer.parseInt(header.split(" ")[1]);
                        byte[] buffer = new byte[size];
                        int read = 0;
                        while (read < size) {
                            int r = in.read(buffer, read, size - read);
                            if (r == -1)
                                break;
                            read += r;
                        }
                        Image img = new Image(new java.io.ByteArrayInputStream(buffer));
                        Platform.runLater(() -> currentImage.set(img));
                    }
                }
            } catch (Exception e) {
                if (running)
                    System.err.println("Lost connection: " + e.getMessage());
            } finally {
                running = false;
                updateStatus("Disconnected");
                Platform.runLater(() -> currentImage.set(null));
            }
        }, "ImageReceiverThread");
        imageReceiverThread.setDaemon(true);
        imageReceiverThread.start();
    }

    // package com.example.service;

    // import java.io.File; // <-- Thêm import
    // import java.io.InputStream;
    // import java.io.PrintWriter;
    // import java.net.Socket;

    // import com.example.client.RemoteFileClient;
    // import com.example.client.RemoteKeyboardClient;
    // import com.example.client.RemoteMouseClient;
    // import com.example.model.ConnectionInf;

    // import javafx.application.Platform;
    // import javafx.beans.property.ObjectProperty;
    // import javafx.beans.property.SimpleObjectProperty;
    // import javafx.beans.property.SimpleStringProperty;
    // import javafx.beans.property.StringProperty;
    // import javafx.scene.image.Image;

    // public class ClientService {
    // private ConnectionInf connectionInfo;
    // private volatile boolean running = true;
    // private Thread imageReceiverThread;
    // private PrintWriter writer; // Sẽ được dùng chung
    // private RemoteMouseClient remoteMouseClient;
    // private RemoteKeyboardClient remoteKeyboardClient;

    // private RemoteFileClient remoteFileClient; // <-- 1. THÊM KHAI BÁO BỊ THIẾU

    // private File fileUserSelected; // File đang chờ gửi

    // // (Các property... giữ nguyên)
    // private final StringProperty connectionStatus = new
    // SimpleStringProperty("Disconnected");
    // private final ObjectProperty<Image> currentImage = new
    // SimpleObjectProperty<>(null);
    // private int serverWidth = 0;
    // private int serverHeight = 0;

    // public void connect(ConnectionInf info) {
    // this.connectionInfo = info;
    // try {
    // Socket socket = info.getSocket();
    // // 1. Tạo MỘT writer duy nhất cho TẤT CẢ lệnh text
    // writer = new PrintWriter(socket.getOutputStream(), true);

    // // 3. Dùng chung 'writer' cho cả chuột và phím
    // remoteMouseClient = new RemoteMouseClient(socket); // <-- SỬA LỖI
    // remoteKeyboardClient = new RemoteKeyboardClient(writer); // (Của bạn đã đúng)

    // updateStatus("Connected to: " + info.getIpAddress() + ":" + info.getPort());

    // // Bắt đầu luồng nhận ảnh/lệnh
    // startImageReceiver(socket);

    // } catch (Exception e) {
    // e.printStackTrace();
    // updateStatus("Connection failed: " + e.getMessage());
    // }
    // }

    // private void startImageReceiver(Socket socket) {
    // imageReceiverThread = new Thread(() -> {
    // try (InputStream in = socket.getInputStream()) {
    // // Gửi lệnh bắt đầu stream (qua writer dùng chung)
    // writer.println("START_SCREEN");

    // while (running) {
    // // (Logic đọc header bằng sb.append((char) c) giữ nguyên...)
    // StringBuilder sb = new StringBuilder();
    // int c;
    // while ((c = in.read()) != -1) {
    // if (c == '\n')
    // break;
    // sb.append((char) c);
    // }
    // if (sb.length() == 0)
    // break;

    // String header = sb.toString().trim();

    // if (header.startsWith("SCREEN_SIZE")) {
    // // (Logic xử lý SCREEN_SIZE giữ nguyên)
    // String[] parts = header.split(" ");
    // serverWidth = Integer.parseInt(parts[1]);
    // serverHeight = Integer.parseInt(parts[2]);
    // continue;

    // // --- LOGIC BỊ THIẾU CỦA BẠN NẰM Ở ĐÂY ---
    // } else if (header.startsWith("ACCEPT_FILE_TRANSFER ")) {
    // try {
    // // 1. Đọc port mà server gửi
    // int filePort = Integer.parseInt(header.split(" ")[1]);
    // System.out.println("Server accepted transfer on port: " + filePort);

    // // 2. Chạy việc gửi file trên một luồng mới (để không chặn luồng ảnh)
    // if (this.fileUserSelected != null) {
    // new Thread(() -> {
    // // 3. Gọi RemoteFileClient để kết nối Kênh 2
    // remoteFileClient.sendFile(this.fileUserSelected, filePort);
    // this.fileUserSelected = null; // Xóa file đã chọn
    // }).start();
    // }
    // } catch (Exception e) {
    // System.err.println("Failed to parse file port: " + e.getMessage());
    // }
    // // --- KẾT THÚC LOGIC BỊ THIẾU ---

    // } else if (header.startsWith("IMG_START")) {
    // // (Logic đọc buffer ảnh giữ nguyên)
    // int size = Integer.parseInt(header.split(" ")[1]);
    // byte[] buffer = new byte[size];
    // int read = 0;
    // while (read < size) {
    // int r = in.read(buffer, read, size - read);
    // if (r == -1)
    // break;
    // read += r;
    // }
    // Image img = new Image(new java.io.ByteArrayInputStream(buffer));
    // Platform.runLater(() -> currentImage.set(img));
    // }
    // }
    // } catch (Exception e) {
    // if (running)
    // System.err.println("Lost connection: " + e.getMessage());
    // } finally {
    // running = false;
    // updateStatus("Disconnected");
    // Platform.runLater(() -> currentImage.set(null));
    // }
    // }, "ImageReceiverThread");

    // imageReceiverThread.setDaemon(true);
    // imageReceiverThread.start();
    // }
    // private void startImageReceiver(Socket socket) {
    // imageReceiverThread = new Thread(() -> {
    // try (InputStream in = socket.getInputStream()) {
    // // Gửi lệnh bắt đầu stream (qua writer dùng chung)
    // writer.println("START_SCREEN");

    // while (running) {
    // // (Logic đọc header bằng sb.append((char) c) giữ nguyên...)
    // StringBuilder sb = new StringBuilder();
    // int c;
    // while ((c = in.read()) != -1) {
    // if (c == '\n')
    // break;
    // sb.append((char) c);
    // }
    // if (sb.length() == 0)
    // break;

    // String header = sb.toString().trim();

    // if (header.startsWith("SCREEN_SIZE")) {
    // // (Logic xử lý SCREEN_SIZE giữ nguyên)
    // String[] parts = header.split(" ");
    // serverWidth = Integer.parseInt(parts[1]);
    // serverHeight = Integer.parseInt(parts[2]);
    // continue;

    // // --- LOGIC MỚI: Nhận cổng truyền file ---
    // } else if (header.startsWith("IMG_START")) {
    // // (Logic đọc buffer ảnh giữ nguyên)
    // int size = Integer.parseInt(header.split(" ")[1]);
    // byte[] buffer = new byte[size];
    // int read = 0;
    // while (read < size) {
    // int r = in.read(buffer, read, size - read);
    // if (r == -1)
    // break;
    // read += r;
    // }
    // Image img = new Image(new java.io.ByteArrayInputStream(buffer));
    // Platform.runLater(() -> currentImage.set(img));
    // }
    // }
    // } catch (Exception e) {
    // // (catch... finally... giữ nguyên)
    // if (running)
    // System.err.println("Lost connection: " + e.getMessage());
    // } finally {
    // running = false;
    // updateStatus("Disconnected");
    // Platform.runLater(() -> currentImage.set(null));
    // }
    // }, "ImageReceiverThread");

    // imageReceiverThread.setDaemon(true);
    // imageReceiverThread.start();
    // }

    /**
     * Phương thức này được gọi từ Controller (ví dụ: khi nhấn nút Send File)
     * để bắt đầu quá trình truyền file.
     */

    public void startFileTransfer(File file) { // Đổi tên
        if (file == null || !file.exists())
            return;
        if (writer == null || !running)
            return;

        this.fileUserSelected = file;
        // Bước 1: Gửi yêu cầu qua Kênh 1
        writer.println("REQUEST_FILE_TRANSFER");
    }

    public void disconnect() {
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
    }

    // Phương thức để Controller gọi
    public RemoteMouseClient getRemoteMouseClient() {
        return remoteMouseClient;
    }

    public RemoteKeyboardClient getRemoteKeyboardClient() {
        return remoteKeyboardClient;
    }

    public int getServerWidth() {
        return serverWidth;
    }

    public int getServerHeight() {
        return serverHeight;
    }

    public boolean isRunning() {
        return running;
    }

    // Các Property để UI "lắng nghe"
    public StringProperty connectionStatusProperty() {
        return connectionStatus;
    }

    public ObjectProperty<Image> currentImageProperty() {
        return currentImage;
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> connectionStatus.set(status));
    }

}