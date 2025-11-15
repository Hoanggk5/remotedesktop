package com.example.server.command;

/**
 * Lớp tiện ích để dịch String từ client thành Command objects.
 */
public class CommandParser {

    public static Command parse(String clientMessage) {
        if (clientMessage == null || clientMessage.isEmpty()) {
            return null;
        }

        String message = clientMessage.trim();
        String[] parts = message.split(" ");
        String commandName = parts[0].toUpperCase();

        try {
            switch (commandName) {
                case "KEY_PRESS":
                    if (parts.length >= 2) {
                        int keyCode = Integer.parseInt(parts[1]);
                        return new KeyPressCommand(keyCode);
                    }
                    break;

                case "KEY_RELEASE":
                    if (parts.length >= 2) {
                        int keyCode = Integer.parseInt(parts[1]);
                        return new KeyReleaseCommand(keyCode);
                    }
                    break;

                // --- Lệnh Hệ thống ---
                case "DISCONNECT":
                    return new DisconnectCommand();
                case "START_SCREEN":
                    return new StartStreamCommand();

                // --- Lệnh Di chuyển/Cuộn ---
                case "MOUSE_MOVE":
                    if (parts.length >= 3) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        return new MouseMoveCommand(x, y);
                    }
                    break;

                case "SCROLL":
                    if (parts.length >= 2) {
                        int amount = Integer.parseInt(parts[1]);
                        return new ScrollCommand(amount);
                    }
                    break;

                // --- Lệnh Click ---
                case "CLICK_LEFT":
                    return new ClickLeftCommand();
                case "CLICK_RIGHT":
                    return new ClickRightCommand();

                // --- Lệnh Giữ/Nhả Chuột ---
                case "MOUSE_PRESS":
                    if (parts.length >= 2) {
                        if (parts[1].equalsIgnoreCase("LEFT"))
                            return new MousePressLeftCommand();
                        if (parts[1].equalsIgnoreCase("RIGHT"))
                            return new MousePressRightCommand();
                    }
                    break;
                case "MOUSE_RELEASE":
                    if (parts.length >= 2) {
                        if (parts[1].equalsIgnoreCase("LEFT"))
                            return new MouseReleaseLeftCommand();
                        if (parts[1].equalsIgnoreCase("RIGHT"))
                            return new MouseReleaseRightCommand();
                    }
                    break;

            }
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse command arguments: " + clientMessage);
            return null;
        }

        return null; // Lệnh không xác định
    }
}