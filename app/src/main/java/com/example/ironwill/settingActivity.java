package com.example.ironwill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Map;
import android.content.SharedPreferences;

public class settingActivity extends AppCompatActivity implements OnServiceResolvedListener{

    private MDNSDiscovery mdnsDiscovery;
    private final String TAG = "SettingInfo";

    //================用户信息=================
    private EditText accountEdit;
    private String account;
    private String password;

    //==============服务器信息==================
    private String server_ip;
    private int server_port;
    private EditText connectIPEdit;


    @Override
    public void onBackPressed() {
        // 在这里添加你的操作，例如不执行任何操作，禁用导航栏的返回键
        // 或者显示一个提示
        // super.onBackPressed(); // 如果想要保留默认的返回键行为，不要调用super.onBackPressed();
    }


    // 实现接口方法，在这里处理接收到的IP地址和端口(必须要override的接口方法)
    @Override
    public void onServiceResolved(String ipAddress, int port) {
        // 在这里处理接收到的IP地址和端口
        Log.d(TAG, "Server IP Address: " + ipAddress);
        Log.d(TAG, "Server Port: " + port);
        server_ip = ipAddress;
        server_port = port;

        connectIPEdit.setText(server_ip);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用沉浸模式，并设置为粘性的沉浸模式
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // 设置屏幕方向为横向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_settingpage);

        // 创建 MDNSDiscovery 对象
        mdnsDiscovery = new MDNSDiscovery();
        mdnsDiscovery.discoverMDNSService(this);
        // 设置接口回调 **************
        mdnsDiscovery.setOnServiceResolvedListener(this); // ******
        connectIPEdit = findViewById(R.id.connectIPEdit);


        //--------------返回-------------------
        MediaPlayer pushButtonsound = MediaPlayer.create(this, R.raw.pushbutton);
        ImageButton backImageButton = findViewById(R.id.backButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮被点击时的操作
                pushButtonsound.start();
                Intent intent = new Intent();
                intent.setClass(settingActivity.this, LoadPageActivity.class);
                startActivity(intent);
                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        });
        //------------------------------------


        //--------------确认-------------------
        accountEdit = findViewById(R.id.accountEdit);
        loadUserInfo();
        ImageButton confirmButton = findViewById(R.id.confirmButton);
        // 添加确认按钮的点击事件
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里添加确认退出的操作
                saveUserInfo();
                server_ip = connectIPEdit.getText().toString();
                // 获取SharedPreferences对象
                SharedPreferences sharedPreferences = getSharedPreferences("server_info", MODE_PRIVATE);
                // 获取SharedPreferences.Editor对象
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // 存储String信息
                editor.putString("server_ip", server_ip);
                editor.putInt("server_port", server_port);
                // 应用更改
                editor.apply();
                // 关闭mDNS服务
                pushButtonsound.start();
                Intent intent = new Intent();
                intent.setClass(settingActivity.this,LoadPageActivity.class);
                startActivity(intent);
                finish(); // 关闭当前活动
            }
        });
        //-------------------------------------

        //-----------------
    }

    // 加载保存的用户信息
    private void loadUserInfo() {
        Map<String, String> userMap = FileSave.getUserInfo(this);
        if (userMap != null) {
            account = userMap.get("account");
            password = userMap.get("password");
            // 设置默认值
            accountEdit.setText(account);
        }
    }

    // 保存用户信息
    private void saveUserInfo () {
        account = accountEdit.getText().toString();
        // 保存用户信息
        boolean saved = FileSave.saveUserInfo(this, account, password);

        if (saved) {
            // 提示保存成功或其他逻辑
        } else {
            // 提示保存失败或其他逻辑
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mdnsDiscovery.stopDiscovery();
    }

}