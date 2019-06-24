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
public class NIoReactorDemo {
  private ServerSocketChannel socketChannel;
  private SubReactor[] subReactor ;
  private ExecutorService service = Executors.newCachedThreadPool();
  private mainReactor mainReactor;

    public static void main(String[] args) throws IOException {
        NIoReactorDemo nIoReactorDemo = new NIoReactorDemo();
        nIoReactorDemo.init();
        nIoReactorDemo.newGroup();
    }

    public void newGroup() throws IOException {
        mainReactor = new mainReactor();
    }

    public void init() throws IOException {
        socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        mainReactor.Register(socketChannel);
        mainReactor.start();
        mainReactor.bind();
    }



    class mainReactor extends  Thread{
        private Selector selector;

        public mainReactor() throws IOException {
            this.selector = Selector.open();
        }
        public void bind() throws IOException {
            socketChannel.bind(new InetSocketAddress(8080));
        }
        AtomicInteger atomicInteger = new AtomicInteger(0);
        public void Register(SelectableChannel selectableChannel) throws ClosedChannelException {
            selectableChannel.register(selector, SelectionKey.OP_ACCEPT);

        }
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
                        if (key.isAcceptable()){
                            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();

                          new Acceptor(socketChannel);
                        }
                    }
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }



        class Acceptor{
            public Acceptor(SocketChannel socketChannel) throws IOException {
                socketChannel.configureBlocking(false);
                int index = atomicInteger.getAndIncrement()%subReactor.length;
                SubReactor subReactor = NIoReactorDemo.this.subReactor[index];
                subReactor.start();
                    subReactor.register(socketChannel);
            }
        }
    }

    class SubReactor extends Thread{
    private Selector selector;
    private volatile boolean running;

        @Override
        public synchronized void start() {
            if (!running) {
                running = true;
                super.start();
            }
        }

        public SubReactor() throws IOException {
            this.selector = Selector.open();
        }

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
                        if (key.isReadable()){
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            new Handler(socketChannel);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void register(SocketChannel socketChannel) throws IOException {
            socketChannel.register(selector,SelectionKey.OP_READ);
            socketChannel.configureBlocking(false);

        }
        class Handler{
            byte[] bytes=null;
            public Handler(SocketChannel socketChannel) {
                try {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    while (socketChannel.isOpen()&&socketChannel.read(buffer)!=-1){
                        if(buffer.position()>0) break;

                    }
                    if (buffer.position()==0)return;
                    buffer.flip();
                    bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    service.execute(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(new String(bytes));
                        }
                    });

                    System.out.println("收到数据来自："+socketChannel.getRemoteAddress());

                    // 响应结果 200
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 11\r\n\r\n" +
                            "Hello World";
                    ByteBuffer byteBuffer = ByteBuffer.wrap(response.getBytes());
                    while (byteBuffer.hasRemaining()){
                        socketChannel.write(byteBuffer);
                    }
                }catch (Exception e){
                    e.printStackTrace();
            }
            }
        }
    }
}
