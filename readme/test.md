Server: dùng PrintWriter gửi header text (IMG_START <size>) báo kích thước ảnh, rồi dùng DataOutputStream (luồng nhị phân) gửi dữ liệu ảnh thực tế (mảng byte).

Client: đọc header bằng BufferedReader để biết kích thước ảnh, sau đó đọc đúng số byte bằng DataInputStream và tạo Image để hiển thị.