remotedesktop/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/
│       │       ├── App.java                     # JavaFX main app
│       │       ├── controller/
│       │       │   ├── PrimaryController.java
│       │       │   └── SecondaryController.java
│       │       ├── client/
│       │       │   ├── ClientApp.java           # Khởi động client
│       │       │   ├── ClientHandler.java       # Nhận & hiển thị hình ảnh
│       │       │   ├── InputSender.java         # Gửi sự kiện chuột/phím
│       │       │   └── ScreenViewer.java        # Render màn hình server
│       │       ├── server/
│       │       │   ├── ServerApp.java           # Khởi động server
│       │       │   ├── ClientSession.java       # Xử lý 1 kết nối client
│       │       │   ├── ScreenCapturer.java      # Chụp ảnh màn hình
│       │       │   └── CommandExecutor.java     # Thực thi lệnh điều khiển
│       │       ├── utils/
│       │       │   ├── ImageUtils.java          # Chuyển đổi BufferedImage <-> byte[]
│       │       │   └── NetworkUtils.java        # Các hàm tiện ích Socket
│       │       └── module-info.java
│       └── resources/
│           └── com/example/
│               ├── primary.fxml
│               ├── secondary.fxml
│               └── icons/                       # (tuỳ chọn) chứa hình ảnh icon
│
└── target/
