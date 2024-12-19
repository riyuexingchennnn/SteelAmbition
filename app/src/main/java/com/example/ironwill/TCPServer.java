package com.example.ironwill;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.*;
import android.content.pm.ActivityInfo;

public class TCPServer {
    private static final String TAG = "TCPServer";
    private ServerSocket serverSocket;
    private boolean running = false;

    private RestroomActivity restroom_activity;

    private TextView user1;
    private TextView user2;
    private TextView user3;
    private TextView user4;
    private TextView user5;
    private TextView user6;

    // 启动服务器
    public void start(int port,RestroomActivity activity) {
        running = true;
        new ServerTask(activity).execute(port);
        restroom_activity = activity;
    }

    // 停止服务器
    public void stop() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ServerTask extends AsyncTask<Integer, String, Void> {

        private WeakReference<RestroomActivity> weakActivity;

        // 构造函数
        public ServerTask(RestroomActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                serverSocket = new ServerSocket(params[0]);
                Log.d(TAG, "Server started on port " + params[0]);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    Log.d(TAG, "Client connected: " + clientSocket.getInetAddress().getHostAddress());
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String message;
                    // 定义JSON格式的正则表达式
                    String jsonRegex = "^\\{.*\\}$";

                    while ((message = in.readLine()) != null) {
                        Log.d(TAG, "Message from client: " + message);
                        // 使用正则表达式匹配JSON字符串
                        boolean isJson = Pattern.matches(jsonRegex, message);
                        if(isJson){
                            // 解析JSON字符串为JSONObject对象
                            // 尝试解析JSON字符串
                            try {
                                // 解析JSON字符串为JSONObject对象
                                JSONObject jsonObject = new JSONObject(message);
                                // 如果没有抛出异常，说明字符串是合法的JSON格式
                                // 获取JSONObject中的值
                                String type = jsonObject.getString("type");
                                int pretime = jsonObject.getInt("pretime");
                                if(type.equals("game_prestart_time")){
                                    Map<String, String> userMap = FileSave.getUserInfo(restroom_activity);
                                    String car_ip = "192.168.0.1"; // 默认初始
                                    if (userMap != null) {
                                        car_ip = userMap.get("password");
                                    }
                                    Intent intent = new Intent();
                                    intent.setClass(restroom_activity, MainActivity.class);
                                    intent.putExtra("ip",car_ip);
                                    restroom_activity.startActivity(intent);
                                    restroom_activity.finish();
                                    Log.d(TAG, "房主已准备，游戏开始");

                                    //this.cancel(true); // true表示取消时是否中断正在执行的任务

                                }
                            } catch (JSONException e) {
                                // 如果抛出异常，说明字符串不是合法的JSON格式
                                Log.e(TAG,"字符串不是合法的JSON格式");
                            }
                        }
                        else
                            // 这里可以对接收到的消息进行处理
                            publishProgress(message);
                    }

                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error in server: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            // 在UI线程中处理接收到的消息
            String message = values[0];


            // 按照 '/' 符号分割字符串，得到用户名信息和 teaminfo
            String[] parts = message.split("/");

            String[] userList = parts[0].split(",");
            String[] teamList = parts[1].split(",");
            // 现在 messageList 中存储了根据逗号分隔后的子字符串
            for(String user : userList)
                Log.d(TAG,"user列表: " + user);
            for(String team :  teamList)
                Log.d(TAG,"team列表: " + team);
            // 创建一个长度为6的新数组
            String[] extendedUserList = new String[6];
            int redTeamNum = 0,blueTeamNum = 3;
            // 将原始数组的内容复制到新数组中
            for (int i = 0; i < userList.length; i++) {
                if(teamList[i].equals("False"))
                    extendedUserList[redTeamNum++] = userList[i];
                if(teamList[i].equals("True"))
                    extendedUserList[blueTeamNum++] = userList[i];
            }
            // 如果原始数组长度小于6，则使用 "空闲" 填充空白位置
            for (int i = 0; i < 6; i++) {
                if(extendedUserList[i] == null)
                    extendedUserList[i] = "空 闲";
            }

            // 获取 RestroomActivity 的实例
            RestroomActivity activity = weakActivity.get();
            if (activity != null) {
                // 更新 textView 的内容
                user1 = activity.findViewById(R.id.user1);
                user2 = activity.findViewById(R.id.user2);
                user3 = activity.findViewById(R.id.user3);
                user4 = activity.findViewById(R.id.user4);
                user5 = activity.findViewById(R.id.user5);
                user6 = activity.findViewById(R.id.user6);

                user1.setText(extendedUserList[0]);
                user2.setText(extendedUserList[1]);
                user3.setText(extendedUserList[2]);
                user4.setText(extendedUserList[3]);
                user5.setText(extendedUserList[4]);
                user6.setText(extendedUserList[5]);

            }
        }
    }
}
