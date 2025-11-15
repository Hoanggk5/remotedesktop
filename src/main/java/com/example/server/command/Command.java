package com.example.server.command;

import com.example.server.ClientHandler;

/**
 * Interface chung cho tất cả các lệnh
 */
public interface Command {
    void execute(ClientHandler context);
}

// --- Lệnh Hệ thống ---

class DisconnectCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.requestDisconnect(); // Báo ClientHandler tự ngắt
    }
}

class StartStreamCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.startStreaming(); // Báo ClientHandler tự start
    }
}

// --- Lệnh Di chuyển/Cuộn ---

class MouseMoveCommand implements Command {
    private final int x, y;

    public MouseMoveCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute(ClientHandler context) {
        context.getMouseController().move(x, y);
    }
}

class ScrollCommand implements Command {
    private final int amount;

    public ScrollCommand(int amount) {
        this.amount = amount;
    }

    @Override
    public void execute(ClientHandler context) {
        context.getMouseController().scroll(amount);
    }
}

// --- Lệnh Click (Press + Release) ---

class ClickLeftCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.getMouseController().clickLeft();
    }
}

class ClickRightCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.getMouseController().clickRight();
    }
}

// --- Lệnh Giữ Chuột (Press) ---

class MousePressLeftCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.getMouseController().mousePressLeft();
    }
}

class MousePressRightCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.getMouseController().mousePressRight();
    }
}

// --- Lệnh Nhả Chuột (Release) ---

class MouseReleaseLeftCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.getMouseController().mouseReleaseLeft();
    }
}

class MouseReleaseRightCommand implements Command {
    @Override
    public void execute(ClientHandler context) {
        context.getMouseController().mouseReleaseRight();
    }
}
