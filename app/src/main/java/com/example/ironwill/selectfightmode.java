package com.example.ironwill;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class selectfightmode extends AppCompatActivity {

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
        setContentView(R.layout.activity_selectfightmode);


        //--------------返回-------------------
        MediaPlayer pushButtonsound = MediaPlayer.create(this, R.raw.pushbutton);
        ImageButton backImageButton = findViewById(R.id.backButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮被点击时的操作
                pushButtonsound.start();
                Intent intent = new Intent();
                intent.setClass(selectfightmode.this, LoadPageActivity.class);
                startActivity(intent);
                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        });
        //------------------------------------

        //-------------------团队乱斗------------------
        ImageButton teamFightButton = findViewById(R.id.teamFightButton);
        teamFightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮被点击时的操作
                pushButtonsound.start();
                Intent intent = new Intent();
                intent.setClass(selectfightmode.this, RestroomActivity.class);
                String mode = "teamfight";
                intent.putExtra("mode",mode);
                startActivity(intent);
                // 手动调用 finish() 来销毁当前 Activity
                finish();
            }
        });
        //-------------------------------------------

    }
}