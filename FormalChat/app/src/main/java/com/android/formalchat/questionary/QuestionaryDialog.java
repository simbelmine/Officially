package com.android.formalchat.questionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.formalchat.MainActivity;
import com.android.formalchat.R;

/**
 * Created by Sve on 3/17/15.
 */
public class QuestionaryDialog extends DialogFragment {
    private static final String PREFS_NAME = "FormalChatPrefs";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.alert_dialog)
                .setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setDoneQuestionary();
                        startMainActivity();
                    }
                })
                .create();
    }


    private void startMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void setDoneQuestionary() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("questionary_done", true);
        editor.commit();
    }
}