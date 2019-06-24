package InternetDemo.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Niclas
 * @create 2019-06-12-20:15
 */
public class bioServer {
    public static void main(String[] args) throws IOException {
            ServerSocket socket = new ServerSocket(8080);
        System.out.println("服务启动");
            while (!socket.isClosed()){
                Socket s =socket.accept();//堵塞
                try {
                    System.out.println("收到新连接："+s.toString());

                   InputStream inputStream = s.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
                    String msg;
                    while ((msg=bufferedReader.readLine())!=null){//没有数据堵塞
                        if(msg.length()==0){
                            break;
                        }
                        System.out.println(msg);
                    }
                  OutputStream outputStream = s.getOutputStream();
                    outputStream.write("HTTP/1.1 200 ok \r\n".getBytes());
                    outputStream.write("Content-Lenth: 10\r\n\r\n".getBytes());
                    outputStream.write("qiuwenlong".getBytes());
                      System.out.println("收到数据 来自："+s.toString());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    s.close();
                }
            }
        socket.close();
    }
}
