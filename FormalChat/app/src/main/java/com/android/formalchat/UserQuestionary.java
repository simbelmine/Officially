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

    public String getQuestionOne() {
        return getString("questionOne");
    }
    public void setQuestionOne(String questionOne) {
        put("questionOne", questionOne);
    }

    public String getQuestionTwo() {
        return getString("questionTwo");
    }
    public void setQuestionTwo(String questionTwo) {
        put("questionTwo", questionTwo);
    }

    public String getQuestionTree() {
        return getString("questionTree");
    }
    public void setQuestionTree(String questionTree) {
        put("questionTree", questionTree);
    }

    public String getQuestionFour() {
        return getString("questionFour");
    }
    public void setQuestionFour(String questionFour) {
        put("questionFour", questionFour);
    }

    public String getQuestionFive() {
        return getString("questionFive");
    }
    public void setQuestionFive(String questionFive) {
        put("questionFive", questionFive);
    }

    public String getQuestionSix() {
        return getString("questionSix");
    }
    public void setQuestionSix(String questionSix) {
        put("questionSix", questionSix);
    }

    public String getQuestionSeven() {
        return getString("questionSeven");
    }
    public void setQuestionSeven(String questionSeven) {
        put("questionSeven", questionSeven);
    }

}
