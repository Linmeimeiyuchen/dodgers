package com.example.f1285.loginpink;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by f1285 on 2017/7/10.
 */

public class Session {

    public void setSession(Context context, String name, String key, String value){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putString(key, value);
        PE.commit();
    }

    public void setSession(Context context, String name, String key, int value){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putInt(key, value);
        PE.commit();
    }

    public String getSession(Context context, String name, String key, String def){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        return settings.getString(key, def);
    }

    public int getSession(Context context, String name, String key, int def){
        SharedPreferences settings = context.getSharedPreferences(name, MODE_PRIVATE);
        return settings.getInt(key, def);
    }
}
