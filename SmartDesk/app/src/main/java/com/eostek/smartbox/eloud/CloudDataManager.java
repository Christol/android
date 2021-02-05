package com.eostek.smartbox.eloud;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eostek.smartbox.utils.UrlConstants;
import com.eostek.smartbox.utils.JsonHelp;
import com.eostek.smartbox.utils.Utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

@SuppressLint("NewApi")
public class CloudDataManager {
	
	
	private static final String TAG = "CloudDataManager";
	private static CloudDataManager instance;
	private Handler mHandler;
	
	public void setHander(Handler handler) {
		Utils.print(TAG, " setHander : " + handler);
		mHandler = handler;
	}

	public synchronized static CloudDataManager getInstance() {
		if (instance == null) {
			instance = new CloudDataManager();
		}
		return instance;
	}

	public void getDeviceTime(final DeviceTimeInfoListener listener) {
		Utils.print(TAG, " getDeviceTime  " );
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						long time = JsonHelp.getDeviceTime(UrlConstants.getServiceIpAndProtUrl());
						if (listener != null) {
							listener.onResult(time);
							return;
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
						if (listener != null) {
							listener.onResult(-1);
						}
					}

				}

			});
		}
	}

	public void uploadEnvData(int deviceID, int userID, float lux, float temp, float hum) {
		final String write = "work_space_env_set&device=" + deviceID + "&user=" + userID + "&temp=" + temp + "&humi=" + hum
				+ "&light=" + lux;
		Utils.print(TAG, " write : " + write);
		if(mHandler != null){
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							//Utils.print(TAG, " json : " + jsonObject.toString());
							// success 或 error
							int result = jsonObject.optInt("ret");
							if (result == 1) {
								Utils.print(TAG, "uploadEnvData success");
							} else {
								Utils.print(TAG, "uploadEnvData fail!");
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
					}
				}
				
			});
		}
		
	}
	
	String json = "[{\"id\": 1,\"name\": \"11\",\"depart\": \"ggg\",\"pos\": \"sdwq\",\"e-mail\": \"11@mail.1\",\"mobil\": \"1890000117\",\"photo\": \"inc/user/1/1.jpg\" },"
			+ "{\"id\": 2,\"name\": \"44\",\"depart\": \"qwdq\", \"pos\": \"qwqf\",\"e-mail\": \"22@mail.2\",\"mobil\": \"1730000111\",\"photo\": \"inc/user/2/2.jpg\"},"
			+ "{\"id\": 3,\"name\": \"55\",\"depart\": \"qwfqf\",\"pos\": \"UXfqwfwq\",\"e-mail\": \"33@mail.com\",\"mobil\": \"17374647784\",\"photo\": \"inc/user/3/3.jpg\"}]";

	public void getUserList(int id, final UserInfoListListener listener) {
		final String write = "user_list&device=" + id;
		Utils.print(TAG, " write : " + write);
		//Utils.print(TAG, " mHandler : " + mHandler);
		if(mHandler != null){
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							//Utils.print(TAG, " json : " + jsonObject.toString());
							JSONArray jsonArray = jsonObject.optJSONArray("list");
							if(jsonArray != null){
								//Utils.print(TAG, " jsonArray : " + jsonArray.toString());
								ArrayList<UserData> userList = new ArrayList<UserData>();
								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject jsonObject3 = (JSONObject) jsonArray.opt(i);
									//Utils.print(TAG, " jsonObject3 : " + jsonObject3.toString());
									UserData userData = JsonHelp.jsonObjectToUserListData(jsonObject3);

									userList.add(userData);
								}
								
								if(listener != null) {
									listener.onResult(1, userList);
									return;
								}
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
						if(listener != null) {
							listener.onResult(-1, null);
						}
					} 
					
					/*try {
						//JSONObject jsonObject = new JSONObject(json);
						//Utils.print(TAG, " json : " + jsonObject.toString());
						
						JSONArray jsonArray = new JSONArray(json);
						Utils.print(TAG, " jsonArray : " + jsonArray.toString());
						
					} catch (JSONException e) {
						e.printStackTrace();
						Utils.print(TAG, "JSONException : " + e.getMessage());
					}*/
				}
				
			});
		}
	}
	
	public void getDeviceID(String mac, final DeviceInfoListener listener) {
		final String write = "device_info&mac=" + mac;
		Utils.print(TAG, " write : " + write);
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							DeviceInfo info = new DeviceInfo();
							info.setId(jsonObject.optInt("id"));
							info.setMac(jsonObject.optString("mac"));
							info.setName(jsonObject.optString("name"));
							if (listener != null) {
								listener.onResult(1, info);
								return;
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
						if (listener != null) {
							listener.onResult(-1, null);
						}
					}

				}

			});
		}
	}
	
	public void getUserRight(int deviceId, int userId, final UserRightListener listener){
		final String write = "user_right&device=" + deviceId + "&user=" + userId;
		Utils.print(TAG, " write : " + write);
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							UserRight info = new UserRight();
							info.setUserID(jsonObject.optInt("id"));
							info.setMaglockRight(jsonObject.optInt("mag-lock"));
							info.setUsb1Right(jsonObject.optInt("usb-1"));
							info.setUsb2Right(jsonObject.optInt("usb-2"));
							info.setUsb3Right(jsonObject.optInt("usb-3"));
							info.setUsb4Right(jsonObject.optInt("usb-4"));
							if (listener != null) {
								listener.onResult(1, info);
								return;
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
						if (listener != null) {
							listener.onResult(-1, null);
						}
					} 

				}

			});
		}
	}
	
	public void uploadLight(int deviceId, int userId, int light){
		final String write = "user_light_set&device=" + deviceId + "&user=" + userId + "&light=" + light;
		Utils.print(TAG, " write : " + write);
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							int result = jsonObject.optInt("ret");
							if (result == 1) {
								Utils.print(TAG, "uploadLight success");
							} else {
								Utils.print(TAG, "uploadLight fail!");
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
					}
				}

			});
		}
	}
	
	public void getLight(int deviceId, int userId, final UserLightListener listener){
		final String write = "user_light_get&device=" + deviceId + "&user=" + userId;
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							int light = jsonObject.optInt("val1");
							if (listener != null) {
								listener.onResult(1, light);
								return;
							}
						}else{
							Utils.print(TAG, "getLight");
							if (listener != null) {
								listener.onResult(1, 100);
								return;
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
						if (listener != null) {
							listener.onResult(-1, -1);
						}
					}

				}

			});
		}
	}
	//{"val1-h":10,"val1-l":20,"val2-h":30,"val2-l":40,"val3-h":50,"val3-l":60}
	public void uploadPositionInfo(int deviceID, int userID, long id1_h, long id1_l, long id2_h, long id2_l, long id3_h, long id3_l){
		final String write = "robot_pos_set&device=" + deviceID + "&user=" + userID + "&val1-h=" + id1_h + "&val1-l=" + id1_l
				 + "&val2-h=" + id2_h + "&val2-l=" + id2_l + "&val3-h=" + id3_h + "&val3-l=" + id3_l;
		Log.d(TAG, "uploadPositionInfo : " +write);
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							int result = jsonObject.optInt("ret");
							if (result == 1) {
								Utils.print(TAG, "uploadPositionInfo success");
							} else {
								Utils.print(TAG, "uploadPositionInfo fail!");
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
					} 

				}

			});
		}
	}
	
	public void getPositionInfo(int deviceID, int userID, final PositionInfoListener listener){
		final String write = "robot_pos_get&device=" + deviceID + "&user=" + userID;
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							PositionInfo info = new PositionInfo();
							info.setId1_h(jsonObject.optLong("val1-h"));
							info.setId1_l(jsonObject.optLong("val1-l"));
							info.setId2_h(jsonObject.optLong("val2-h"));
							info.setId2_l(jsonObject.optLong("val2-l"));
							info.setId3_h(jsonObject.optLong("val3-h"));
							info.setId3_l(jsonObject.optLong("val3-l"));
							if(listener != null) {
								listener.onResult(1, info);
								return;
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
						
					} 

				}

			});
		}
		if(listener != null) {
			listener.onResult(-1, null);
		}
	}
	//http://172.16.1.79:8888/ada/lx-interface-1.jsp?method=workseat_power_set&device=1&power=350
	public void uploadPowerData(int deviceId, int power){
		final String write = "workseat_power_set&device=" + deviceId + "&power=" + power;
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							int result = jsonObject.optInt("ret");
							if (result == 1) {
								Utils.print(TAG, "uploadPowerData success");
							} else {
								Utils.print(TAG, "uploadPowerData fail!");
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
					}

				}

			});
		}
	}

	//http://172.23.67.78:8013/ada/lx-interface.jsp?method=work_station_status&device=1
	//http://172.23.67.78:8013/ada/lx-interface.jsp?method=
	public void getLimitTimeUserInfo(int id, final LimitTimeUserInfoListener listener) {
		final String write = "work_station_status&device=" + id;
		Utils.print(TAG, " write : " + write);
		if(mHandler != null){
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							LimitTimeUserInfo info = new LimitTimeUserInfo();
							if(jsonObject.optInt("ret") == -99){
								info.setState(jsonObject.optInt("ret"));
							}else{
								info.setId(jsonObject.optInt("id"));
								info.setState(jsonObject.optInt("work_station_busy"));
								info.setBeginTime(jsonObject.optString("busy_start"));
								info.setEndTime(jsonObject.optString("busy_end"));
								info.setUserId(jsonObject.optInt("user"));
							}
							if(listener != null) {
								listener.onResult(1, info);
								return;
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
					}
				}

			});
		}
	}

	public void getLockState(int deviceId, final com.eostek.smartbox.cloud.DeviceLockListener listener){
		final String write = "shall_unlock&device=" + deviceId;
		if (mHandler != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						JSONObject jsonObject = JsonHelp.getJsonObjectWithTimeOut(UrlConstants.getServiceUrl(), write);
						if (jsonObject != null) {
							int state = jsonObject.optInt("unlock");
							if (listener != null) {
								listener.onResult(1, state);
								return;
							}
						}
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						Utils.print(TAG, "SocketTimeoutException");
						if (listener != null) {
							listener.onResult(-1, -1);
						}
					}
				}

			});
		}
	}

}
