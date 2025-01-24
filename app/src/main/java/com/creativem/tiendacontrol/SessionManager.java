package com.creativem.tiendacontrol;
import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private static final String PREFS_NAME = "CodePrefs";
    private static final String LOGIN_STATUS = "loginStatus";
    private static final String CODE_KEY = "code";

    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLoggedIn(boolean isLoggedIn){
        editor.putBoolean(LOGIN_STATUS, isLoggedIn);
        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(LOGIN_STATUS, false);
    }

    public void setSavedCode(String savedCode){
        editor.putString(CODE_KEY, savedCode);
        editor.commit();
    }

    public String getSavedCode(){
        return pref.getString(CODE_KEY,null);
    }
    public void clearSavedCode(){
        editor.putString(CODE_KEY, null);
        editor.commit();
    }
}