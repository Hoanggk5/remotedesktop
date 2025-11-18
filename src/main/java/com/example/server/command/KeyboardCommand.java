package com.example.server.command;

import com.example.server.ClientHandler;
import com.example.server.ClientManager;

/**
 * Command chung cho các lệnh bàn phím
 */
public interface KeyboardCommand extends Command {
}

// // --- Lệnh gõ phím đơn ---
// class KeyPressCommand implements KeyboardCommand {
// private final int keyCode;

// public KeyPressCommand(int keyCode) {
// this.keyCode = keyCode;
// }

// @Override
// public void execute(ClientHandler context) {
// context.getKeyboardController().keyPress(keyCode);
// }
// }

// class KeyReleaseCommand implements KeyboardCommand {
// private final int keyCode;

// public KeyReleaseCommand(int keyCode) {
// this.keyCode = keyCode;
// }

// @Override
// public void execute(ClientHandler context) {
// context.getKeyboardController().keyRelease(keyCode);
// }
// }

// // --- Lệnh gõ phím hoàn chỉnh (press + release) ---
// class KeyTypeCommand implements KeyboardCommand {
// private final int keyCode;

// public KeyTypeCommand(int keyCode) {
// this.keyCode = keyCode;
// }

// @Override
// public void execute(ClientHandler context) {
// context.getKeyboardController().typeKey(keyCode);
// }
// }

// --- Lệnh nhấn phím xuống ---
class KeyPressCommand implements KeyboardCommand {
    private final int keyCode;

    public KeyPressCommand(int keyCode) {
        this.keyCode = keyCode;
    }

    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) { // <-- Áp dụng Security Check
            context.getKeyboardController().keyPress(keyCode);
        }
    }
}

// --- Lệnh nhả phím ra ---
class KeyReleaseCommand implements KeyboardCommand {
    private final int keyCode;

    public KeyReleaseCommand(int keyCode) {
        this.keyCode = keyCode;
    }

    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) { // <-- Áp dụng Security Check
            context.getKeyboardController().keyRelease(keyCode);
        }
    }
}

// --- Lệnh gõ phím hoàn chỉnh (press + release) ---
class KeyTypeCommand implements KeyboardCommand {
    private final int keyCode;

    public KeyTypeCommand(int keyCode) {
        this.keyCode = keyCode;
    }

    @Override
    public void execute(ClientHandler context) {
        if (SecurityCheck.allow(context)) { // <-- Áp dụng Security Check
            context.getKeyboardController().typeKey(keyCode);
        }
    }
}