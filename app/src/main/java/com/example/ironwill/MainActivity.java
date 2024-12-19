package com.example.ironwill;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.MotionEvent;
import android.content.Intent;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.widget.ImageButton;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.widget.ImageView;
import android.media.MediaPlayer;
import android.animation.ValueAnimator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.GestureDetector;
import android.os.Looper;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.content.SharedPreferences;


public class MainActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        // 在这里添加你的操作，例如不执行任何操作，禁用导航栏的返回键
        // 或者显示一个提示
        // super.onBackPressed(); // 如果想要保留默认的返回键行为，不要调用super.onBackPressed();
    }
    //===========网络=============
    private Socket socket2server;
    private TCPJson TCPJson;
    private String esp32IpAddress;
    private String serverIPAddress;
    private int serverport;

    //=============属性内参==============
    private String name;
    private final String TAG ="MainActivityInfo";
    private TextView mTextViewCoordinateRight;
    private int teal700Color = Color.parseColor("#FF018786");
    private ScheduledExecutorService executor;

    private float startX, startY, endX, endY,deltaX=0 ,deltaY=0;
    private static final int MIN_DISTANCE = 150 / 10;
    private int joystick_x=0,joystick_y=0;


    private void setupJoystick(JoystickView joystickRight,String ip) {
        mTextViewCoordinateRight = findViewById(R.id.textRight);

        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, int strength) {
                joystick_x = (joystickRight.getNormalizedX() - 50) * 2;
                joystick_y = (-joystickRight.getNormalizedY() + 50) * 2;

                mTextViewCoordinateRight.setText(
                        String.format("   偏航 x%03d : y%03d", joystick_x, joystick_y)
                );

                String controlCommand =  String.format(
                        "var=joystick&val=%d,%d,%d,%d",joystick_x,joystick_y,(int)deltaX ,(int)deltaY);
                SendControlCommandTask sendTask = new SendControlCommandTask();
                sendTask.execute(esp32IpAddress, controlCommand);

            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用沉浸模式，并设置为粘性的沉浸模式
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // 设置屏幕方向为横向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String ip = intent.getStringExtra("car_ip");
        esp32IpAddress = ip + ":80";  // tcp协议


        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = getSharedPreferences("server_info", MODE_PRIVATE);
        // 获取String信息
        serverIPAddress = sharedPreferences.getString("server_ip", "192.168.0.1");
        serverport = sharedPreferences.getInt("server_port", 82);

        Log.d(TAG,serverIPAddress);


        Map<String, String> userMap = FileSave.getUserInfo(this);
        String account = null;
        if (userMap != null) {
            account = userMap.get("account");
        }
        if (account == null)
            account = "未命名";
        name = account;
        if(TCPJson == null)
            TCPJson = new TCPJson();

        // 获取屏幕尺寸
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        // 在Log中显示整数变量的值
        Log.e("Iron Will", "screenWidth: " + screenWidth);
        Log.e("Iron Will", "screenHeight: " + screenHeight);

        WebView webView = findViewById(R.id.webView);     // 初始化 WebView
        WebSettings webSettings = webView.getSettings();  // 获取 WebView 的设置
        webSettings.setJavaScriptEnabled(true);           // 启用 JavaScript

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //自适应屏幕
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        // 禁止WebView滑动
//        webView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true; // 返回true表示事件被消费，不会向上传递
//            }
//        });

        // 设置缩放控制
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        // 加载网页
        webView.loadUrl("http://"+ ip +":81/stream");
        webView.setWebViewClient((new WebViewClient()));

        // 获取默认的 WebView 设置
        WebSettings settings = webView.getSettings();

        // 启用 JavaScript
        settings.setJavaScriptEnabled(true);

        // 设置 WebView 的旋转属性，实现上下翻转
        //webView.setRotationX(180);
        // 缩放WebView，实现镜像效果
        webView.setScaleY(-1);
        webView.setScaleX(-1);

        webView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        endX = event.getX();
                        endY = event.getY();

                        deltaX = endX - startX;
                        deltaY = endY - startY;

                        deltaX /= 10;
                        deltaY /= 10;

                        // 至少要在右方区域
                        if(startX > 200)
                            if((Math.abs(deltaX) > MIN_DISTANCE) || (Math.abs(deltaY) > MIN_DISTANCE)){
                                if(deltaX > 0)
                                    deltaX = 20;
                                if(deltaX < 0)
                                    deltaX = -20;
                                String controlCommand =  String.format(
                                        "var=joystick&val=%d,%d,%d,%d",joystick_x,joystick_y,(int)deltaX,(int)deltaY);
                                Log.d("joystick", joystick_x + "," + joystick_y + "," + deltaX + "," + deltaY);
                                SendControlCommandTask sendTask = new SendControlCommandTask();
                                sendTask.execute(esp32IpAddress, controlCommand);
                                deltaX = 0;

//                                try {
//                                    Thread.sleep(50); // 延时0.05秒（单位：毫秒）
//                                    // 在这里执行延时后的操作
//                                } catch (InterruptedException e) {
//                                    // 当线程被中断时，处理中断异常
//                                    System.err.println("Thread interrupted while sleeping");
//                                    // 可以选择重新设置线程的中断状态
//                                    Thread.currentThread().interrupt();
//                                }

                            }

                        break;
                    case MotionEvent.ACTION_UP:
                        startX = 0;
                        startY = 0;
                        deltaX = 0;
                        deltaY = 0;
                        String controlCommand =  String.format(
                                "var=joystick&val=%d,%d,%d,%d",joystick_x,joystick_y,(int)deltaX,(int)deltaY);
                        SendControlCommandTask sendTask = new SendControlCommandTask();
                        sendTask.execute(esp32IpAddress, controlCommand);
                        break;
                }
                return true; // 返回true表示事件被消费，不会向上传递
                // 返回 false 以确保 WebView 仍然可以处理触摸事件
            }
        });


        // ---------------------设置摇杆---------------------
        final JoystickView joystickRight = findViewById(R.id.joystickView_right);
        setupJoystick(joystickRight,ip);
        //-------------------------------------------------


        //--------------准心-------------------
        ImageView aimImageView = findViewById(R.id.aimimageView);
        aimImageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        // 创建一个 ValueAnimator 实例
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, Color.RED, Color.WHITE);
        colorAnimator.setDuration(400); // 0.2秒
        // 设置动画更新监听器
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                aimImageView.setColorFilter(animatedValue, PorterDuff.Mode.SRC_IN);
            }
        });
        // 创建放大再缩小动画
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(aimImageView, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(aimImageView, "scaleY", 1f, 1.5f, 1f);
        scaleXAnimator.setDuration(400); // 0.2秒
        scaleYAnimator.setDuration(400); // 0.2秒
        //------------------------------------

        //--------------开火-------------------
        MediaPlayer raysound = MediaPlayer.create(this,R.raw.raysound);
        ImageButton fireImageButton = findViewById(R.id.fireButton);
        fireImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动动画
                colorAnimator.start();
                scaleXAnimator.start();
                scaleYAnimator.start();
                // 在这里执行按钮被点击时的操作
                raysound.start();

                try{
                    URL url = new URL("http://" + esp32IpAddress + "/control?var=fire&val=0");
                    // 打开连接
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    // 发送GET请求
                    InputStream in = urlConnection.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //------------------------------------

        //--------------返回-------------------
        // 获取backButton，修改颜色
        ImageButton backImageButton = findViewById(R.id.backButton);
        MediaPlayer pushButtonsound = MediaPlayer.create(this,R.raw.pushbutton);
        // 获取图像资源
        Drawable drawable = backImageButton.getDrawable();
        drawable.setColorFilter(teal700Color, PorterDuff.Mode.SRC_ATOP);
        backImageButton.setImageDrawable(drawable);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮被点击时的操作
                //onBackPressed(); //物理返回
                // 物理返回不可取，finish页面就销毁了
                webView.stopLoading();
                pushButtonsound.start();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,LoadPageActivity.class);
                startActivity(intent);
                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        });
        //------------------------------------

        //--------------排位-------------------
        // 获取awardButton，修改颜色
        ImageButton awardImageButton = findViewById(R.id.awardButton);
        // 获取图像资源
        drawable = awardImageButton.getDrawable();
        drawable.setColorFilter(teal700Color, PorterDuff.Mode.SRC_ATOP);
        awardImageButton.setImageDrawable(drawable);
        //-------------------------------------

        //--------------设置-------------------
        // 获取awardButton，修改颜色
        ImageButton settingImageButton = findViewById(R.id.settingButton);
        // 获取图像资源
        drawable = settingImageButton.getDrawable();
        drawable.setColorFilter(teal700Color, PorterDuff.Mode.SRC_ATOP);
        settingImageButton.setImageDrawable(drawable);
        // 创建并设置点击监听器
        View.OnClickListener settingButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里处理按钮点击事件
                pushButtonsound.start();
                webView.stopLoading();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,settingActivity.class);
                startActivity(intent);
                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        };
        // 将监听器设置给按钮
        settingImageButton.setOnClickListener(settingButtonClickListener);
        //-------------------------------------


        //--------------血条-------------------
        HealthBarView healthBar = findViewById(R.id.healthBar);
        healthBar.setProgress(0.7f); // Set progress to 70%
        //------------------------------------

        //------------------------网络----------------------
        // 在后台线程中启动服务器
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new ClientAsyncTask().execute();
//            }
//        }).start();
        //----------------------------------------------------
    }

    //-------------------------发送心跳包--------------------------------
    public void sendHeartbeat() {
        // 构建 JSON 数据
        String jsonData = "{\"type\": \"heartbeat\"}\n";
        TCPJson.send_json(socket2server,jsonData);
        Log.d(TAG,"heart包发送成功");
    }
    //----------------------------------------------------------------

    //-----------------------后台处理Client--------------------------------
    private class ClientAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // 避免闪退
                if(socket2server == null)
                    socket2server = new Socket(serverIPAddress, serverport);

//                try {
//                    Thread.sleep(2000);
//                }catch (InterruptedException e) {
//                    // 当线程被中断时，处理中断异常
//                    Log.e(TAG,"Thread interrupted while sleeping");
//                    // 可以选择重新设置线程的中断状态
//                    Thread.currentThread().interrupt();
//                }

                // 创建 ScheduledExecutorService 实例
                executor = Executors.newSingleThreadScheduledExecutor();
                // 定期发送心跳包
                executor.scheduleAtFixedRate(() -> sendHeartbeat(), 0, 5, TimeUnit.SECONDS);

                // JSON 数据
                String jsonData = "{\"type\": \"init\", \"id\": \"controller_{" + name + "}\", \"version\": \"0.1\"}\n";
                if(TCPJson == null)
                    TCPJson = new TCPJson();
                TCPJson.send_json(socket2server,jsonData);
                Log.d(TAG,"init包发送成功");

                String robot_json = "{\"type\": \"set_robot\", \"name\": \"{" + name + "}\", \"team_color\": 0, \"robot_color\": 0}\n";
                TCPJson.send_json(socket2server,robot_json);
                Log.d(TAG,"robot包发送成功");


                TCPJson.receive_json(socket2server);

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
