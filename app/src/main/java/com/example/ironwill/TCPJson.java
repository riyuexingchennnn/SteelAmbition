package com.example.ironwill;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import com.google.gson.Gson;

public class TCPJson {

    private final String TAG = "JsonSender";
    private Gson gson;
    public TCPJson(){
        this.gson = new Gson();
    }
    public void send_json(Socket socket2server,String jsonData) {
        try {
            // 获取输出流
            OutputStream outputStream2server = socket2server.getOutputStream();
            // 发送 JSON 数据
            outputStream2server.write(jsonData.getBytes());
            Log.d(TAG,"json包发送成功");
        } catch (UnknownHostException e) {
            Log.e(TAG,"未知的主机异常: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG,"IO 异常: " + e.getMessage());
        }
    }

    public void receive_json(Socket socket2server) {
        try {
            InputStream inputStream = socket2server.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream));

            String line;
            // 读取数据直到遇到结束符
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "收到json信息：" + line);
                // 在这里处理收到的每一行数据
                Response response = gson.fromJson(line, Response.class);
                // 处理解析后的数据
//                Log.d(TAG, "Type: " + response.getType());
//                Log.d(TAG, "Result: " + response.getResult());
//                Log.d(TAG, "Message: " + response.getMessage());
            }
        } catch (UnknownHostException e) {
            Log.e(TAG,"未知的主机异常: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG,"IO 异常: " + e.getMessage());
        }
        finally {

        }
    }
}
