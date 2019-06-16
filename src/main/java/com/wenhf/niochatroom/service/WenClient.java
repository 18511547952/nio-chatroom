package com.wenhf.niochatroom.service;

import java.io.IOException;

public class WenClient {
    public static void main(String[] args) throws IOException {
        new NIOClient("文红枫").start();
    }
}
