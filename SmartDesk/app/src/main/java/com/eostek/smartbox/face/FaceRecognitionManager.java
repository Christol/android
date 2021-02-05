package com.eostek.smartbox.face;

import android.util.Log;

import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.face.recognition.FaceSocketClien;
import com.eostek.smartbox.station.StationUiActivity;

public class FaceRecognitionManager {

    private static final String TAG = "face";

    private StationUiActivity mUiActivity;

    private FaceSocketClien mSocketClientManager;

    public FaceRecognitionManager(StationUiActivity Activity) {
        Log.d(TAG, "SocketClientManager...");
        this.mUiActivity = Activity;
        mSocketClientManager = new FaceSocketClien(mUiActivity);
    }

    public void initFaceSocket(){
        mSocketClientManager.connectSocket();
    }

    public void closeFaceSocket(){
        mSocketClientManager.clsoe();
    }

}
