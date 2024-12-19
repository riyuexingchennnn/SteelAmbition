package com.example.ironwill;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileSave{

    public static boolean saveUserInfo(Context context, String account, String
            password) {
        FileOutputStream fos = null;
        try {

            fos = context.openFileOutput("data.txt",
                    Context.MODE_PRIVATE);

            fos.write((account + ":" + password).getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            try {
                if(fos != null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 在Android开发中，Context 是一个表示应用程序环境信息的对象。它提供了访问应用程序资源、启动组件（如Activity、Service）和访问系统服务等功能。Context 是一个抽象类，具体的实现是 Context 类的子类，比如 Activity、Service、Application 等。
    public static Map<String, String> getUserInfo(Context context) {
        String content = "";
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("data.txt");

            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            content = new String(buffer);
            Map<String, String> userMap = new HashMap<String, String>();
            String[] infos = content.split(":");
            userMap.put("account", infos[0]);
            userMap.put("password", infos[1]);
            return userMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            try {
                if(fis != null){
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


