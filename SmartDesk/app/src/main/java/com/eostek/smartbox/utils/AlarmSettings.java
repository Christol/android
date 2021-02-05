package com.eostek.smartbox.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemClock;

public class AlarmSettings {

	private AlarmManager am = null;

	private Context mContext = null;

	private static AlarmSettings instance;

	/**
	 * @return
	 * @throws @Title: getInstance
	 * @Description: 单例方式提供对象
	 */
	public static AlarmSettings getInstance(Context context) {
		if (instance == null) {
			synchronized (AlarmSettings.class) {
				if (instance == null) {
					instance = new AlarmSettings(context);
				}
			}
		}
		return instance;
	}

	public AlarmSettings(Context cxt) {
		mContext = cxt;
		am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	}

	/**
	 *
	 * @param time_current 需要设置的时间
	 * @param time_zone    需要设置的时区
	 */
	public void TimeSync(long time_current, String time_zone) {
		android.provider.Settings.System.putString(mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24, "24");

		SystemClock.setCurrentTimeMillis(time_current);

		setTimeZone(time_zone);
	}

	private void setTimeZone(String timeZone) {
		am.setTimeZone("Asia/Shanghai");// Asia/Taipei//GMT+08:00
	}

}
