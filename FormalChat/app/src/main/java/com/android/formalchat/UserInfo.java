package com.android.formalchat;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Sve on 2/20/15.
 */
@ParseClassName("UserInfo")
public class UserInfo extends ParseObject{
    public UserInfo() {

    }

    public String getLoginName() {
        return getString("loginName");
    }

    public void setLoginName(String loginName) {
        put("loginName", loginName);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public int getGender() {
        return getInt("gender");
    }

    public void setGender(int gender) {
        put("gender", gender);
    }

    public String getAge() {
        return getString("age");
    }

    public void setAge(String age) {
        put("age", age);
    }

    public String getLocation() {
        return getString("location");
    }

    public void setLocation(String location) {
        put("location", location);
    }

    public int getInterestedIn() {
        return getInt("interestedIn");
    }

    public void setInterestedIn(int interestedIn) {
        put("interestedIn", interestedIn);
    }

    public int getLookingFor() {
        return getInt("lookingFor");
    }

    public void setLookingFor(int lookingFor) {
        put("lookingFor", lookingFor);
    }

    public String getAboutMe() {
        return getString("aboutMe");
    }

    public void setAboutMe(String aboutMe) {
        put("aboutMe", aboutMe);
    }

    public int getRelationship() {
        return getInt("relationship");
    }

    public void setRelationship(int relationship) {
        put("relationship", relationship);
    }

    public int getBodyType() {
        return getInt("bodyType");
    }

    public void setBodyType(int bodyType) {
        put("bodyType", bodyType);
    }

    public int getEthnicity() {
        return getInt("ethnicity");
    }

    public void setEthnicity(int ethnicity) {
        put("ethnicity", ethnicity);
    }

    public int getInterests() {
        return getInt("interests");
    }

    public void setInterests(int interests) {
        put("interests", interests);
    }
}
