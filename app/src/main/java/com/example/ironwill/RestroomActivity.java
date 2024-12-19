package com.example.ironwill;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.IOException;
import java.util.Map;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;

import org.json.JSONArray;

import android.os.AsyncTask;

// tcp发送和监听不能同时
public class RestroomActivity extends AppCompatActivity implements OnServiceResolvedListener{

    private TCPJson TCPJson;
    private MDNSDiscovery mdnsDiscovery;
    private final String TAG = "RestroomInfo";
    private String server_ipAddress = "192.168.1.106"; // 先默认随便写一个(宿舍路由器是192.168.0.102)
    private final int SERVER_PORT = 82;
    private MediaPlayer bgm;
    private Socket socket2server;
    private ScheduledExecutorService executor;

    private String ipAddress = "11111"; // 这里填写用户的IP地址
    private String username = "未命名"; // 这里填写用户的用户名
    private TCPServer tcpServer;

    // 实现接口方法，在这里处理接收到的IP地址和端口(必须要override的接口方法)
    @Override
    public void onServiceResolved(String ipAddress, int port) {
        // 在这里处理接收到的IP地址和端口
        Log.d(TAG, "Server IP Address: " + ipAddress);
        Log.d(TAG, "Server Port: " + port);

        server_ipAddress = ipAddress;

        Map<String, String> userMap = FileSave.getUserInfo(this);
        String account = null;
        if (userMap != null) {
            account = userMap.get("account");
        }
        if (account == null)
            account = "未命名";

        // JSON 数据
        String jsonData = "{\"type\": \"init\", \"id\": \"controller_{" + account + "}\", \"version\": \"0.1\"}\n";
        if(TCPJson == null)
            TCPJson = new TCPJson();
        try{
            if(socket2server == null)
                socket2server = new Socket(server_ipAddress, SERVER_PORT);
        }catch(IOException e){
            Log.e(TAG,e.getMessage());
        }
        TCPJson.send_json(socket2server,jsonData);
        Log.d(TAG,"init包发送成功");

        // 创建 ScheduledExecutorService 实例
        executor = Executors.newSingleThreadScheduledExecutor();
        // 定期发送心跳包
        executor.scheduleAtFixedRate(() -> sendHeartbeat(), 0, 5, TimeUnit.SECONDS);
    }

    //-------------------------发送心跳包--------------------------------
    public void sendHeartbeat() {
        // 构建 JSON 数据
        String jsonData = "{\"type\": \"heartbeat\"}\n";
        // 避免闪退
        try{
            if(socket2server == null)
                socket2server = new Socket(server_ipAddress, SERVER_PORT);
        }catch(IOException e){
            Log.e(TAG,e.getMessage());
        }

        TCPJson.send_json(socket2server,jsonData);

        Log.d(TAG,"heart包发送成功");
    }
    //----------------------------------------------------------------

    @Override
    public void onBackPressed() {
        // 在这里添加你的操作，例如不执行任何操作，禁用导航栏的返回键
        // 或者显示一个提示
        // super.onBackPressed(); // 如果想要保留默认的返回键行为，不要调用super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        // 在这里添加按下主页键时的操作
        super.onUserLeaveHint();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用沉浸模式，并设置为粘性的沉浸模式
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // 设置屏幕方向为横向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_restroom);

        // 创建 MDNSDiscovery 对象
        mdnsDiscovery = new MDNSDiscovery();
        mdnsDiscovery.discoverMDNSService(this);
        // 设置接口回调 **************
        mdnsDiscovery.setOnServiceResolvedListener(this); // ******
        if(TCPJson == null)
            TCPJson = new TCPJson();

        //---------------------该手机先发送一个进入的消息---------------------
        Map<String, String> userMap = FileSave.getUserInfo(this);
        if (userMap != null) {
            username = userMap.get("account");
            ipAddress = userMap.get("password");
        }
        // 依然采用强制等待1秒，等待mDNS先发现服务器
        try {
            Thread.sleep(1000); // 延时1秒（单位：毫秒）
            // 在这里执行延时后的操作
        } catch (InterruptedException e) {
            // 当线程被中断时，处理中断异常
            Log.e(TAG,"Thread interrupted while sleeping");
            // 可以选择重新设置线程的中断状态
            Thread.currentThread().interrupt();
        }
        SendUserDataToRestroom sendData = new SendUserDataToRestroom(); // SendData不能拎出来放上面
        sendData.execute(ipAddress, username, "no","no",server_ipAddress);
        //-----------------------------------------------------------------

        //------------------------BGM设置-----------------------
        bgm = MediaPlayer.create(this,R.raw.bgm2);
        bgm.setLooping(true); // 设置为单曲循环
        bgm.start();
        // 设置整体音量为0.5（范围是0.0到1.0）
        bgm.setVolume(0.5f, 0.5f);
        //------------------------------------------------------


        tcpServer = new TCPServer();

        //---------------标题------------------
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        TextView titleView = findViewById(R.id.titleView);
        if(mode.equals("teamfight")){
            titleView.setText("团队乱斗模式");
        }else{
            titleView.setText("XXX模式");
        }
        //------------------------------------

        //--------------返回-------------------
        MediaPlayer pushButtonsound = MediaPlayer.create(this, R.raw.pushbutton);
        ImageButton backImageButton = findViewById(R.id.backButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮被点击时的操作
                pushButtonsound.start();
                Intent intent = new Intent();
                intent.setClass(RestroomActivity.this, selectfightmode.class);
                startActivity(intent);
                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        });
        //------------------------------------

        //--------------准备-------------------
        ImageButton readyImageButton = findViewById(R.id.readyButton);
        TextView readyTextView = findViewById(R.id.readytextView);
        readyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮被点击时的操作
                pushButtonsound.start();

                readyTextView.setText("已 准 备");

                tcpServer.stop();
                SendUserDataToRestroom sendData = new SendUserDataToRestroom(); // SendData不能拎出来放上面
                sendData.execute(ipAddress, username, "yes","no",server_ipAddress);
                tcpServer.start(SERVER_PORT, RestroomActivity.this);

            }
        });
        //------------------------------------


        //--------------换边-------------------
        ImageButton changeImageButton = findViewById(R.id.changeteamButton);
        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮被点击时的操作
                pushButtonsound.start();
                tcpServer.stop();
                SendUserDataToRestroom sendData = new SendUserDataToRestroom(); // SendData不能拎出来放上面
                sendData.execute(ipAddress, username, "no","yes",server_ipAddress);
                tcpServer.start(SERVER_PORT, RestroomActivity.this);

            }
        });
        //------------------------------------


        //--------------------监听服务器(这个是与restroom的交互)---------------------
        // 在后台线程中启动服务器
        new Thread(new Runnable() {
            @Override
            public void run() {
                tcpServer.start(SERVER_PORT, RestroomActivity.this);
                new ClientAsyncTask().execute();//两个后台不能同时
            }
        }).start();

    }


    //-----------------------后台处理Client--------------------------------
    private class ClientAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d(TAG, "等待收到json文件");
                // 避免闪退
                if(socket2server == null)
                    socket2server = new Socket(server_ipAddress, SERVER_PORT);
                InputStream inputStream = socket2server.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));

                // 读取数据直到遇到换行符
                String line;
                StringBuilder dataBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    Log.d(TAG, "收到信息" + line);
                    dataBuilder.append(line);
                    if (line.endsWith("\n")) {
                        // 完整接收到一条 JSON 数据
                        String jsonData = dataBuilder.toString().trim(); // 去掉收尾空白字符
                        Log.d(TAG, "收到json文件：" + jsonData);

                        // 在这里解析 JSON 数据
                        try {
                            JSONObject jsonObject = new JSONObject(jsonData);
                            String type = jsonObject.getString("type");
                            Log.d(TAG, "Type: " + type);

                            if (jsonObject.has("list")) {
                                JSONArray list = jsonObject.getJSONArray("list");
                                // 处理列表数据
                            } else if (jsonObject.has("pretime")) {
                                int pretime = jsonObject.getInt("pretime");
                                // 处理预开始时间
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // 清空 StringBuilder 准备接收下一条 JSON 数据
                        dataBuilder.setLength(0);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
    }
    //-----------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在 onDestroy 中释放 MediaPlayer 资源
        bgm.stop();
        bgm.release();

        SendUserDataToRestroom sendData = new SendUserDataToRestroom(); // SendData不能拎出来放上面
        sendData.execute("out", username,"no","no",server_ipAddress);
        tcpServer.stop(); // 停止服务器
        // 停止发现 mDNS 服务
        mdnsDiscovery.stopDiscovery();
        // 关闭 ScheduledExecutorService 实例

        // 在销毁 Activity 时停止线程
//        if (clientThread != null) {
//            clientThread.interrupt(); // 设置线程中断标志
//            clientThread = null; // 将线程对象置为空
//        }
        try{
            if(executor != null)
                executor.shutdown();
            if(socket2server != null)
                socket2server.close();
        } catch (IOException e) {
            // 捕获并处理 IOException 异常
            Log.e(TAG,e.getMessage());
        }

    }
}