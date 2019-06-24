package InternetDemo.NIO;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Niclas
 * @create 2019-06-14-21:23
 */
public class server {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        socketChannel.socket().bind(new InetSocketAddress(8080));
        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){

               SelectionKey select = iterator.next();
                iterator.remove();
               if(select.isAcceptable()){
                   SocketChannel accept = ((ServerSocketChannel) select.channel()).accept();
                   accept.configureBlocking(false);
                   accept.register(selector,SelectionKey.OP_READ);
               }else if (select.isReadable()){
                try {
                    SocketChannel channel = (SocketChannel) select.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    while (channel.isOpen()&&channel.read(buffer)!=-1){
                        if(buffer.position()>0){
                            break;
                        }
                    }
                    if(buffer.position()==0){
                        continue;
                    }
                    buffer.flip();

                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    System.out.println(new String(bytes));
                    System.out.println("收到数据来自："+channel.getRemoteAddress());

                    // 响应结果 200
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 11\r\n\r\n" +
                            "Hello World";
                    ByteBuffer byteBuffer = ByteBuffer.wrap(response.getBytes());
                    while (byteBuffer.hasRemaining()){
                        channel.write(byteBuffer);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    select.cancel();
                }

               }
            }

        }

    }
}
