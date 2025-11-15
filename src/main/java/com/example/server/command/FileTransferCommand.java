package com.example.server.command;

import com.example.server.ClientHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Command để nhận file từ client
 */
public class FileTransferCommand implements Command {

    private final String filename;
    private final long fileSize;
    private final InputStream fileStream;

    public FileTransferCommand(String filename, long fileSize, InputStream fileStream) {
        this.filename = filename;
        this.fileSize = fileSize;
        this.fileStream = fileStream;
    }

    @Override
    public void execute(ClientHandler context) {
        try {
            File dir = new File("received");
            if (!dir.exists())
                dir.mkdirs();

            File outFile = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            while (remaining > 0) {
                int read = fileStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1)
                    break;
                fos.write(buffer, 0, read);
                remaining -= read;
            }
            fos.close();

            System.out.println("Received file: " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}