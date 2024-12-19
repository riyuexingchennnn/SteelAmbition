package com.example.ironwill;

import com.google.gson.annotations.SerializedName;

public class Response {
    @SerializedName("type")
    private String type;

    @SerializedName("result")
    private int result;

    @SerializedName("msg")
    private String message;

    @SerializedName("robot_ip")
    private String robot_ip;

    @SerializedName("score")
    private int score;

    @SerializedName("HP")
    private int HP ;

    @SerializedName("damage_val")
    private int damage_val;

    @SerializedName("grade")
    private int grade;

    public String getType() {
        return type;
    }
    public String getMessage() {
        return message;
    }

    public int getResult() {
        return result;
    }

    public String getRobot_ip() {
        return robot_ip;
    }

    public int getHP() {
        return HP;
    }

    public int getScore() {
        return score;
    }

    public int getGrade() {
        return grade;
    }

    public int getDamage_val() {
        return damage_val;
    }
}
