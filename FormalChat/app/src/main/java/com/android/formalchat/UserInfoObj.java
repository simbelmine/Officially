package com.android.formalchat;

import android.content.Context;

/**
 * Created by Sve on 1/16/16.
 */
public class UserInfoObj {
    private String userName;
    private ZodiacSign zodiacSignEnum;
    private String location;
    private String age;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public ZodiacSign getZodiacSign() {
        return zodiacSignEnum;
    }

    public void setZodiacSign(ZodiacSign zodiacSignEnum) {
        this.zodiacSignEnum = zodiacSignEnum;
    }
}
