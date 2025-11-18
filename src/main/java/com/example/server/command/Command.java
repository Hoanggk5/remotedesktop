package com.example.server.command;

import com.example.server.ClientHandler;
import com.example.server.ClientManager;

/**
 * Interface chung cho tất cả các lệnh.
 * Các class implement có thể để chung trong file này (package-private) cho gọn.
 */
public interface Command {
    void execute(ClientHandler context);
}

// --- CLASS HỖ TRỢ CHECK QUYỀN ---
class SecurityCheck {
    static boolean allow(ClientHandler context) {
        // Chỉ cho phép thực thi nếu Client này đang giữ quyền Controller trong
        // ClientManager
        return ClientManager.getInstance().isController(context);
    }
}

// ========================================================
// NHÓM LỆNH HỆ THỐNG (Ai cũng có quyền thực thi)
// ========================================================

class DisconnectCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.requestDisconnect();
    }
}

class StartStreamCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        // Gọi hàm bắt đầu gửi ảnh màn hình
        // Lưu ý: Hãy chắc chắn method startStreaming() trong ClientHandler là public
        context.startStreaming();
    }
}

// ========================================================
// NHÓM LỆNH ĐIỀU KHIỂN (Chỉ Admin mới được thực thi)
// ========================================================

// --- Di chuyển chuột ---
class MouseMoveCommand implements Command {
    private final int x, y;

    public MouseMoveCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) {
            context.getMouseController().move(x, y);
        }
    }
}

// --- Cuộn chuột ---
class ScrollCommand implements Command {
    private final int amount;

    public ScrollCommand(int amount) {
        this.amount = amount;
    }

    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) {
            context.getMouseController().scroll(amount);
        }
    }
}

// --- Click Chuột (Nhấn + Nhả nhanh) ---
class ClickLeftCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) {
            context.getMouseController().clickLeft();
        }
    }
}

class ClickRightCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) {
            context.getMouseController().clickRight();
        }
    }
}

// --- Giữ chuột (Press) ---
class MousePressLeftCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) {
            context.getMouseController().mousePressLeft();
        }
    }
}

class MousePressRightCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) {
            context.getMouseController().mousePressRight();
        }
    }
}

// --- Nhả chuột (Release) ---
class MouseReleaseLeftCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) {
            context.getMouseController().mouseReleaseLeft();
        }
    }
}

class MouseReleaseRightCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) {
            context.getMouseController().mouseReleaseRight();
        }
    }
}

// --- Bàn phím ---
// class KeyPressCommand implements Command {
// private final int keyCode;

// public KeyPressCommand(int keyCode) {
// this.keyCode = keyCode;
// }

// @Override
// public void execute(ClientHandler context) {
// if (SecurityCheck.allow(context)) {
// context.getKeyboardController().keyPress(keyCode);
// }
// }
// }

// class KeyReleaseCommand implements Command {
// private final int keyCode;

// public KeyReleaseCommand(int keyCode) {
// this.keyCode = keyCode;
// }

// @Override
// public void execute(ClientHandler context) {
// if (SecurityCheck.allow(context)) {
// context.getKeyboardController().keyRelease(keyCode);
// }
// }
// }
// package com.example.server.command;

// import com.example.server.ClientHandler;

// /**
// * Interface chung cho tất cả các lệnh
// */
// public interface Command {
// void execute(ClientHandler context);
// }

// --- Lệnh Hệ thống ---

// class DisconnectCommand implements Command {
// @Override
// public void execute(ClientHandler context) {
// context.requestDisconnect(); // Báo ClientHandler tự ngắt
// }
// }

// class StartStreamCommand implements Command {
// @Override
// public void execute(ClientHandler context) {
// context.startStreaming(); // Báo ClientHandler tự start
// }
// }

// --- Lệnh Di chuyển/Cuộn ---

// class MouseMoveCommand implements Command {
// private final int x, y;

// public MouseMoveCommand(int x, int y) {
// this.x = x;
// this.y = y;
// }

// @Override
// public void execute(ClientHandler context) {
// context.getMouseController().move(x, y);
// }
// }

// class ScrollCommand implements Command {
// private final int amount;

// public ScrollCommand(int amount) {
// this.amount = amount;
// }

// @Override
// public void execute(ClientHandler context) {
// context.getMouseController().scroll(amount);
// }
// }

// // --- Lệnh Click (Press + Release) ---

// class ClickLeftCommand implements Command {
// @Override
// public void execute(ClientHandler context) {
// context.getMouseController().clickLeft();
// }
// }

// class ClickRightCommand implements Command {
// @Override
// public void execute(ClientHandler context) {
// context.getMouseController().clickRight();
// }
// }

// --- Lệnh Giữ Chuột (Press) ---

// class MousePressLeftCommand implements Command {
// @Override
// public void execute(ClientHandler context) {
// context.getMouseController().mousePressLeft();
// }
// }

// class MousePressRightCommand implements Command {
// @Override
// public void execute(ClientHandler context) {
// context.getMouseController().mousePressRight();
// }
// }

// // --- Lệnh Nhả Chuột (Release) ---

// class MouseReleaseLeftCommand implements Command {
// @Override
// public void execute(ClientHandler context) {
// context.getMouseController().mouseReleaseLeft();
// }
// }

// class MouseReleaseRightCommand implements Command {
// @Override
// public void execute(ClientHandler context) {
// context.getMouseController().mouseReleaseRight();
// }
// }