package com.example.ironwill;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendUserDataToRestroom extends AsyncTask<String, Void, Void> {

    private static final String TAG = "SendUserDataToRestRoom";

    @Override
    protected Void doInBackground(String... params) {
        String ipAddress = params[0];
        String username = params[1];
        String isReady = params[2];
        String needChange = params[3];
        String server_ip = params[4];

        try {
            // 指定服务器地址和端口
            Socket socket = new Socket(server_ip, 8888);
            Log.d(TAG, "server_ip: " + server_ip);
            // 准备要发送的数据
            String data = ipAddress + "," + username + "," + isReady + "," + needChange;
            Log.d(TAG, "发送data: " + data);
            // 发送数据
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data.getBytes());
            outputStream.flush();

            // 关闭连接
            socket.close();
            Log.d(TAG, "数据发送成功");
        } catch (UnknownHostException e) {
            Log.e(TAG, "未知主机: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "I/O 错误: " + e.getMessage());
        }
        return null;
    }
}

