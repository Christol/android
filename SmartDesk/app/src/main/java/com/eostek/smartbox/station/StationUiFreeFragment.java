package com.eostek.smartbox.station;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eostek.smartbox.MyApplication;
import com.eostek.smartbox.R;
import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.utils.UrlConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class StationUiFreeFragment extends Fragment{

    private static final String TAG = "smartbox";

	private StationUiActivity mActivity;

    private TextView freeWelcome;
    private ImageView freeQr;

    public static final int GET_DATA_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;
    //子线程不能操作UI，通过Handler设置图片
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_DATA_SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if(bitmap != null){
                        freeQr.setImageBitmap(bitmap);
                    }
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getContext(),getResources().getString(R.string.toast_info_net_fail),Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(getContext(),getResources().getString(R.string.toast_info_server_fail),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "StationUiFreeFragment  onCreateView" );
        return inflater.inflate(R.layout.activity_station_free, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActivity = (StationUiActivity) getActivity();
        Log.d(TAG, "StationUiFreeFragment  onActivityCreated" );
        MyApplication.getInstance().setfaceRecognitionSuccessfulUserID(-1);
        SmartBoxInfo.setFaceStateSuccess(false);
        mActivity.setStationState(mActivity.getResources().getString(R.string.station_state));
        mActivity.setStationStateTime(mActivity.getResources().getString(R.string.station__free_state));
        initView();
        initQRCode(MyApplication.getInstance().getSmarkBoxID());
    }

    private void initView() {
        freeWelcome = (TextView) mActivity.findViewById(R.id.station_free_welcome);
        freeQr = (ImageView) mActivity.findViewById(R.id.station_free_QR);
    }

    public void initQRCode(int deviceID) {
        Log.d(TAG, "initQRCode"  +deviceID);
//        freeQr.setImageResource(CreateQRCode.creatQRCode());
        if(freeQr != null && deviceID != -1){
            setImageURL(UrlConstants.getQRCodeUrl(deviceID));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "StationUiFreeFragment  onCreate" );
    }
    //设置网络图片
    public void setImageURL(final String path) {
        //开启一个线程用于联网
        new Thread() {
            @Override
            public void run() {
                try {
                    //把传过来的路径转成URL
                    URL url = new URL(path);
                    //获取连接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //使用GET方法访问网络
                    connection.setRequestMethod("GET");
                    //超时时间为10秒
                    connection.setConnectTimeout(10000);
                    //获取返回码
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = connection.getInputStream();
                        //使用工厂把网络的输入流生产Bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        Message msg = Message.obtain();
                        msg.obj = bitmap;
                        msg.what = GET_DATA_SUCCESS;
                        handler.sendMessage(msg);
                        //利用Message把图片发给Handler
                        inputStream.close();
                    }else {
                        //服务启发生错误
                        handler.sendEmptyMessage(NETWORK_ERROR);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //网络连接错误
                    handler.sendEmptyMessage(SERVER_ERROR);
                }
            }
        }.start();
    }
}
