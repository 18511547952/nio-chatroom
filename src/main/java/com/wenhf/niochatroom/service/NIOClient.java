package com.wenhf.niochatroom.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NIOClient {
    private String name;

    public NIOClient(String name) {
        this.name = name;
    }

    public void start() throws IOException {

        //创建SocketChannel连接至服务器
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9090));
        System.out.println("客户端连接成功，可以开始聊天...");

        //创建selector
        Selector selector = Selector.open();

        //设置socketChannel为非阻塞
        socketChannel.configureBlocking(false);

        //socketChannel注册到selector
        socketChannel.register(selector, SelectionKey.OP_READ);

        //创建处理可读事件的线程
        new Thread(new NIOClientHandler(selector)).start();

        //获取键盘输入，发送至服务器
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String input = scanner.nextLine();
            if (input != null) {
                socketChannel.write(Charset.forName("UTF-8").encode(name +":"+input));
            }
        }
    }

    /*public static void main(String[] args) throws IOException {
        NIOClient nioClient = new NIOClient();
        nioClient.start();
    }*/
}
