package com.example.ironwill;

import android.os.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendControlCommandTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        String ipAddress = params[0];
        String controlCommand = params[1];

        HttpURLConnection urlConnection = null;

        try {
            // 构建URL
            URL url = new URL("http://" + ipAddress + "/control?" + controlCommand);

            // 打开连接
            urlConnection = (HttpURLConnection) url.openConnection();

            // 发送GET请求
            InputStream in = urlConnection.getInputStream();

            // 可以读取响应，如果需要的话

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }
}
