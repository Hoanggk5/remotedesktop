package com.example.client;

import java.io.PrintWriter;

public class RemoteKeyboardClient {

    private final PrintWriter out;

    public RemoteKeyboardClient(PrintWriter out) {
        this.out = out;
    }

    public void pressKey(int keyCode) {
        out.println("KEY_PRESS " + keyCode);
    }

    public void releaseKey(int keyCode) {
        out.println("KEY_RELEASE " + keyCode);
    }

    public void typeKey(int keyCode) {
        out.println("KEY_TYPE " + keyCode);
    }
}
