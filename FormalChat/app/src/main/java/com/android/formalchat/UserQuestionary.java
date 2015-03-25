package com.android.formalchat;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Sve on 3/13/15.
 */

@ParseClassName("UserQuestionary")
public class UserQuestionary extends ParseObject {
    public UserQuestionary () {}

    public String getLoginName() {
        return getString("loginName");
    }

    public void setLoginName(String loginName) {
        put("loginName", loginName);
    }

}
