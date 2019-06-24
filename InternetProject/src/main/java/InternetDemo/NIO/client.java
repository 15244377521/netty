package InternetDemo.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Niclas
 * @create 2019-06-14-21:24
 */
public class client {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress("localhost",8080));
        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (selectionKey.isConnectable()){
                    if(socketChannel.finishConnect()){
                        System.out.println("连接成功："+socketChannel);
                        ByteBuffer buffer = ByteBuffer.allocateDirect(20480);

                        selectionKey.attach(buffer);
                        //改成自己想要的时间
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    }
                }else if(selectionKey.isWritable()){
                    ByteBuffer buff = (ByteBuffer)selectionKey.attachment();
                    buff.clear();
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("请输入。。。");
                    String str = scanner.nextLine();
                    scanner.close();

                    buff.put(str.getBytes());
                    buff.flip();
                    while (buff.hasRemaining()){
                            socketChannel.write(buff);
                        }
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }else if (selectionKey.isReadable()){
                    System.out.println("收到服务端响应:");
                    ByteBuffer b = ByteBuffer.allocate(1024);

                    while (socketChannel.isOpen()&&socketChannel.read(b)!=-1){
                        if(b.position()>0){
                            break;
                        }
                    }
                    b.flip();
                    byte[] bytes = new byte[b.remaining()];
                    b.get(bytes);
                    System.out.println(new String(bytes));
                }
            }
        }
    }
}
