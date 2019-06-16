package com.wenhf.niochatroom.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NIOClientHandler implements Runnable {

    private Selector selector;

    public NIOClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {

            while (true) {
                int evetCount = selector.select();
                if(evetCount == 0) continue;

                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator iterator = selectionKeySet.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) iterator.next();
                    iterator.remove();

                    if(selectionKey.isReadable()){
                        dealReadEvent(selectionKey);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void dealReadEvent(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        String response = "";
        while (socketChannel.read(byteBuffer)>0){
            byteBuffer.flip();
            response+=(Charset.forName("UTF-8").decode(byteBuffer));
        }
        socketChannel.register(selector,SelectionKey.OP_READ);
        System.out.println(response);
    }
}
