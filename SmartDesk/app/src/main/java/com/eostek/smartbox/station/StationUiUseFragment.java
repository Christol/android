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

import com.eostek.smartbox.R;
import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class StationUiUseFragment extends Fragment{

    private static final String TAG = "smartbox";

	private StationUiActivity mActivity;

    private TextView userStationDepartment;
    private TextView userStationTitle;
    private TextView userStationName;
    private TextView userStationEmail;
    private TextView userStationPhone;
    private ImageView userImageView;

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
                        userImageView.setImageBitmap(bitmap);
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
        Log.d(TAG, "StationUiUseFragment  onCreateView" );
        return inflater.inflate(R.layout.activity_station_use, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActivity = (StationUiActivity) getActivity();
        SmartBoxInfo.setFaceStateSuccess(true);
        Log.d(TAG, "StationUiUseFragment  onActivityCreated" );

        initView();
        initData();
    }

    private void initView() {

        userStationDepartment = (TextView) mActivity.findViewById(R.id.station_department_value);
        userStationTitle = (TextView) mActivity.findViewById(R.id.station_title_value);
        userStationName = (TextView) mActivity.findViewById(R.id.station_name_value);
        userStationEmail = (TextView) mActivity.findViewById(R.id.station_email_value);
        userStationPhone = (TextView) mActivity.findViewById(R.id.station_phone_value);

        userImageView = (ImageView) mActivity.findViewById(R.id.user_photo);
    }

    private void initData() {
        if(SmartBoxInfo.getUserInfo() != null){
            String Depart  = SmartBoxInfo.getUserInfo().getDepart();
            if(Depart != null){
                userStationDepartment.setText(Utils.toUtf8(Depart));
            }else{
                userStationDepartment.setText(mActivity.getResources().getString(R.string.station_department_value));
            }
            String Title  = SmartBoxInfo.getUserInfo().getPosition();
            if(Title != null){
                userStationTitle.setText(Utils.toUtf8(Title));
            }else{
                userStationTitle.setText(mActivity.getResources().getString(R.string.station_title_value));
            }
            String Name  = SmartBoxInfo.getUserInfo().getName();
            if(Name != null){
                userStationName.setText(Utils.toUtf8(Name));
            }else{
                userStationName.setText(mActivity.getResources().getString(R.string.station_name_value));
            }
            String Email  = SmartBoxInfo.getUserInfo().getEmail();
            if(Email != null){
                userStationEmail.setText(Utils.toUtf8(Email));
            }else{
                userStationEmail.setText(mActivity.getResources().getString(R.string.station_email_value));
            }
            String Phone  = SmartBoxInfo.getUserInfo().getMobil();
            if(Phone != null){
                userStationPhone.setText(Utils.toUtf8(Phone));
            }else{
                userStationPhone.setText(mActivity.getResources().getString(R.string.station_phone_value));
            }

            String Photo  = SmartBoxInfo.getUserInfo().getPhoto();
            if(Photo != null){
                Log.d(TAG, "Photo : "+Photo );
                String path = Photo;
                setImageURL(path);
            }
        }else{
            userStationDepartment.setText(mActivity.getResources().getString(R.string.station_department_value));
            userStationTitle.setText(mActivity.getResources().getString(R.string.station_title_value));
            userStationName.setText(mActivity.getResources().getString(R.string.station_name_value));
            userStationEmail.setText(mActivity.getResources().getString(R.string.station_email_value));
            userStationPhone.setText(mActivity.getResources().getString(R.string.station_phone_value));
        }
        mActivity.initLimitTime();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "StationUiUseFragment  onCreate" );
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
