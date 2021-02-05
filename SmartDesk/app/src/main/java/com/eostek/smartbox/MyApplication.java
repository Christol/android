package com.eostek.smartbox;

import android.app.Application;

import com.eostek.smartbox.data.Constants;
import com.eostek.smartbox.eloud.LimitTimeUserInfo;
import com.eostek.smartbox.utils.UrlConstants;
import com.eostek.smartbox.utils.Utils;

import java.util.HashMap;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    private static MyApplication instance;
    public synchronized static MyApplication getInstance() {
        if(instance == null){
            instance = new MyApplication();
        }
        return instance;
    }

    public MyApplication() {
        super();
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.makeMacDirectory(UrlConstants.TEST_DIR);
        Utils.makeMacDirectory(UrlConstants.TEST_DIR);
    }

    // 本地存放地址
    private int SmarkBoxID = Constants.SmarkBoxID;

    private int SmarkBoxUserID = Constants.SmarkBoxUserID;

    private String SmarkBoxIP = Constants.SmarkBoxIP;

    private String SmarkBoxServerIP = Constants.SmarkBoxServerIP;

    private int SmarkBoxServerProt = Constants.SmarkBoxServerProt;

    private int faceSuccessfulUserID = -1;

    public void setSmarkBoxID(int id){
        SmarkBoxID = id;
    }

    public int getSmarkBoxID(){
        return SmarkBoxID;
    }

    public void setSmarkBoxUserID(int id){
        SmarkBoxUserID = id;
    }

    public int getSmarkBoxUserID(){
        return SmarkBoxUserID;
    }

    public void setSmarkBoxIP(String ip){
        SmarkBoxIP = ip;
    }

    public String getSmarkBoxIP(){
        return SmarkBoxIP;
    }

    public void setSmarkBoxServerIP(String ip){
        SmarkBoxServerIP = ip;
    }

    public String getSmarkBoxServerIP(){
        return SmarkBoxServerIP;
    }

    public void setSmarkBoxServerProt(int Prot){
        SmarkBoxServerProt = Prot;
    }

    public int getSmarkBoxServerProt(){
        return SmarkBoxServerProt;
    }

    public void setfaceRecognitionSuccessfulUserID(int UserID){
        faceSuccessfulUserID = UserID;
    }

    public int getfaceRecognitionSuccessfulUserID(){
        return faceSuccessfulUserID;
    }

    private LimitTimeUserInfo limitTimeUserID = null;

    public void setLimitTimeUserID(LimitTimeUserInfo UserID){
            limitTimeUserID = UserID;
    }

    public LimitTimeUserInfo getLimitTimeUserID(){
        return limitTimeUserID;
    }

//test-----------//
    private int SmarkBoxFileID = -1;
    public void setSmarkBoxFileID(int id) {
        SmarkBoxFileID = id;
    }

    public int getSmarkBoxFileID(){
        return SmarkBoxFileID;
    }

    private int SmarkBoxUserFileID = -1;
    public void setSmarkBoxUserFileID(int userId) {
        SmarkBoxUserFileID = userId;
    }

    public int getSmarkBoxUserFileID(){
        return SmarkBoxUserFileID;
    }

}
