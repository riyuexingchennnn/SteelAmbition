package com.example.ironwill;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageButton;
import java.util.Map;
import android.os.Handler;
import android.app.AlertDialog;
import android.widget.TextView;

public class LoadPageActivity extends AppCompatActivity {

    private MediaPlayer bgm;
    private EditText editText;

    private String account;
    private String password;

    private AlertDialog exitConfirmationDialog;  // 在方法外部声明

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null);
        builder.setView(dialogView);

        // 找到 ImageButton 控件
        ImageButton confirmButton = dialogView.findViewById(R.id.confirmButton);
        ImageButton cancelButton = dialogView.findViewById(R.id.cancelButton);

        // 添加确认按钮的点击事件
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里添加确认退出的操作
                exitConfirmationDialog.dismiss();
                finish(); // 关闭当前活动
            }
        });

        // 添加取消按钮的点击事件
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 用户点击取消，不执行任何操作
                exitConfirmationDialog.dismiss();
            }
        });

        // 创建并显示弹窗
        exitConfirmationDialog = builder.create();
        exitConfirmationDialog.show();
    }

    @Override
    public void onBackPressed() {
        // 在这里添加你的操作，例如不执行任何操作，禁用导航栏的返回键
        // 或者显示一个提示
        //super.onBackPressed(); // 如果想要保留默认的返回键行为，不要调用super.onBackPressed();
        showExitConfirmationDialog();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用沉浸模式，并设置为粘性的沉浸模式
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // 设置屏幕方向为横向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_loadpage);


        bgm = MediaPlayer.create(this,R.raw.bgm1);
        bgm.setLooping(true); // 设置为单曲循环
        bgm.start();

        // 设置整体音量为0.5（范围是0.0到1.0）
        bgm.setVolume(0.5f, 0.5f);




        //-------------------单机驾驶------------------------
        ImageButton sure_Button = findViewById(R.id.signalButton);
        MediaPlayer pushButtonsound = MediaPlayer.create(this,R.raw.pushbutton);
        editText = findViewById(R.id.editText);
        loadUserInfo();// 这个位置别乱动
        // 加载保存的用户信息
        // 创建并设置点击监听器
        View.OnClickListener sure_ButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取EditText的文本内容
                String inputIP = editText.getText().toString();
                // 在这里处理按钮点击事件
                Intent intent = new Intent();
                intent.setClass(LoadPageActivity.this,MainActivity.class);
                intent.putExtra("car_ip",inputIP);
                pushButtonsound.start();
                saveUserInfo();
                startActivity(intent);

                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        };
        // 将监听器设置给按钮
        sure_Button.setOnClickListener(sure_ButtonClickListener);
        //---------------------------------------------------


        //-------------------多人竞技------------------------
        ImageButton multiButton = findViewById(R.id.multiButton);
        // 创建并设置点击监听器
        View.OnClickListener multiButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 在这里处理按钮点击事件
                Intent intent = new Intent();
                intent.setClass(LoadPageActivity.this,selectfightmode.class);
                pushButtonsound.start();
                saveUserInfo();
                startActivity(intent);
                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        };
        // 将监听器设置给按钮
        multiButton.setOnClickListener(multiButtonClickListener);
        //---------------------------------------------------

        //--------------------跳转设置-------------------------
        ImageButton settingButton = findViewById(R.id.settingButton);
        // 获取图像资源
        Drawable drawable = settingButton.getDrawable();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        settingButton.setImageDrawable(drawable);
        // 创建并设置点击监听器
        View.OnClickListener settingButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里处理按钮点击事件
                Intent intent = new Intent();
//                bgm.stop();
//                bgm.release();
                pushButtonsound.start();
                intent.setClass(LoadPageActivity.this,settingActivity.class);
                startActivity(intent);

                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        };
        // 将监听器设置给按钮
        settingButton.setOnClickListener(settingButtonClickListener);
        //---------------------------------------------------
    }

    // 加载保存的用户信息
    private void loadUserInfo() {
        Map<String, String> userMap = FileSave.getUserInfo(this);
        if (userMap != null) {
            account = userMap.get("account");
            password = userMap.get("password");

            // 设置默认值
            TextView accountView = findViewById(R.id.nameView);
            if(account == null)
                account = "未命名";
            accountView.setText(account);
            editText.setText(password);
        }
    }

    // 保存用户信息
    private void saveUserInfo() {

        password = editText.getText().toString();
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
        // 在 onDestroy 中释放 MediaPlayer 资源
        bgm.stop();
        bgm.release();
    }

    @Override
    protected void onUserLeaveHint() {
        // 在这里添加按下主页键时的操作
        super.onUserLeaveHint();
        finish();
    }


}
