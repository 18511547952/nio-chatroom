package com.wenhf.niochatroom.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

    private void start() throws IOException {

        //创建selector多路复用器对象
        Selector selector = Selector.open();

        //创建服务器SocketChannel对象
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //将服务器通道绑定指定的监听端口
        serverSocketChannel.bind(new InetSocketAddress(9090));

        //将通道设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //将通道注册到selector，并设置要监听的事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动，开始监听连接事件");


        //循环获取selector监听事件的就绪状况
        while (true) {

            //调用selectoKeys方法获取已就绪的事件集合
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            int readyChannels = selector.select();
            if (readyChannels == 0) continue;

            //遍历就绪事件集合，根据事件类型做出不同的业务处理
            Iterator iterator = selectionKeySet.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = (SelectionKey) iterator.next();

                //在集合中取出一个selectKey后，需要将他从集合中移除
                iterator.remove();
                if (selectionKey.isAcceptable()) {//接入事件
                    dealAcceptEvent(serverSocketChannel, selector);
                }
                if (selectionKey.isReadable()) {//读事件
                    dealReadEvent(selectionKey, selector);
                }
            }
        }
    }

    /**
     * 处理接入事件
     *
     * @param serverSocketChannel
     * @param selector
     * @throws IOException
     */
    private void dealAcceptEvent(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.write(Charset.forName("UTF-8").encode("你已经是群成员了，快退个群给大家看看吧！"));
    }


    /**
     * 处理可读事件
     *
     * @param selectionKey
     * @param selector
     * @throws IOException
     */
    private void dealReadEvent(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        String request = "";
        while (socketChannel.read(byteBuffer) > 0) {
            byteBuffer.flip();
            request += (Charset.forName("UTF-8").decode(byteBuffer));
        }
        socketChannel.register(selector, SelectionKey.OP_READ);

        //将读取信息广播给所有client
        if (request.length() > 0) {
            boardCast(selector,socketChannel,request);
        }
    }

    private void boardCast(Selector selector, SocketChannel sourceChannel, String requset) {
        selector.keys().forEach(selectionKey -> {
            Channel targetChannel = selectionKey.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != sourceChannel) {

                try {
                    ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(requset));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        NIOServer nioServer = new NIOServer();
        nioServer.start();
    }
}
