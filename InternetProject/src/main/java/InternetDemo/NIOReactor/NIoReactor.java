package InternetDemo.NIOReactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Niclas
 * @create 2019-06-19-20:18
 */
public class NIoReactor {
    private ServerSocketChannel socketChannel;
    private reacttorThread[] mainReactorThreads=new reacttorThread[16] ;
    private ExecutorService service = Executors.newCachedThreadPool();
    private reacttorThread[] mainReactor=new reacttorThread[1] ;


    public static void main(String[] args) throws IOException {
        NIoReactor nIoReactorDemo = new NIoReactor();
        nIoReactorDemo.newGroup();
        nIoReactorDemo.init();
        nIoReactorDemo.bind();
    }
    public void bind() throws IOException {
        socketChannel.bind(new InetSocketAddress(8080));
    }
    public void init() throws IOException {
        socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        SelectionKey register = mainReactor[0].register(socketChannel);
        register.interestOps(SelectionKey.OP_ACCEPT);
        mainReactor[0].start();
    }

    public void newGroup() throws IOException {
        for(int i = 0;i<mainReactor.length;i++){
            mainReactor[i] = new reacttorThread(){
                AtomicInteger atomicInteger = new AtomicInteger(0);
                @Override
                protected void handler(SelectableChannel channel) throws IOException {
                    SocketChannel socketChannel = (SocketChannel) channel;
                    socketChannel.configureBlocking(false);
                    int index = atomicInteger.getAndIncrement()%mainReactorThreads.length;
                    reacttorThread subReactor = mainReactorThreads[index];
                    SelectionKey selectionKey = subReactor.register(socketChannel);
                    selectionKey.interestOps(SelectionKey.OP_ACCEPT);
                    subReactor.start();
                }
            };
        }
        for (int i = 0;i<mainReactorThreads.length;i++){
            mainReactorThreads[i]=new reacttorThread() {
                @Override
                protected void handler(SelectableChannel channel) throws IOException {
                    SocketChannel socketChannel = (SocketChannel) channel;
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    while (socketChannel.isOpen()&& socketChannel.read(buffer)!=-1){
                        if(buffer.position()>0) break;

                    }
                    if (buffer.position()==0)return;
                    buffer.flip();
                    final byte[] bytes = new byte[buffer.remaining()];
                    buffer.get();
                    service.execute(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(new String(bytes));
                        }
                    });

                    System.out.println("收到数据来自："+ socketChannel.getRemoteAddress());

                    // 响应结果 200
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 11\r\n\r\n" +
                            "Hello World";
                    ByteBuffer byteBuffer = ByteBuffer.wrap(response.getBytes());
                    while (byteBuffer.hasRemaining()){
                        socketChannel.write(byteBuffer);
                    }
                }
            };
        }
    }

    abstract  class reacttorThread extends Thread{
        private Selector selector;
        public reacttorThread() throws IOException {
            selector = Selector.open();
        }
        public SelectionKey register(SelectableChannel socketChannel) throws IOException {
            return socketChannel.register(selector, 0);

        }
        protected abstract void handler(SelectableChannel channel) throws IOException;
        @Override
        public void run() {
            while (true){
                try {
                    selector.select();
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        int readyOps = key.readyOps();
                        if ((readyOps&(SelectionKey.OP_READ|SelectionKey.OP_ACCEPT))!=0||readyOps==0){
                            key.channel().configureBlocking(false);
                            handler(( key.channel()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }


    }

}
