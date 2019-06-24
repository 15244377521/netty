package InternetDemo.bio;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLOutput;
import java.util.Scanner;

/**
 * @author Niclas
 * @create 2019-06-12-20:14
 */
public class bioClient {
    private static Charset charset = Charset.forName("UTF-8");


    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",8080);
        OutputStream outputStream = socket.getOutputStream();
        Scanner s = new Scanner(System.in);
        System.out.println("请输入：");
        String msg = s.nextLine()+"\r\n";
        outputStream.write(msg.getBytes(charset));
        s.close();
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        while((msg=bufferedReader.readLine())!=null){
            if(msg.length()==0){
                break;
            }
            System.out.println(msg);
        }
        socket.close();
    }
}
