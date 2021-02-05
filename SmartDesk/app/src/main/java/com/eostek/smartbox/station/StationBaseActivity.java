package com.eostek.smartbox.station;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eostek.smartbox.R;

public class StationBaseActivity extends Activity {

	private FragmentManager mFragmentManager;

	private TextView mTime;

	private TextView mDate;

	private TextView mStationNum;

	private TextView mStationState;

	private TextView mStationStateTime;

	private TextView mSmarkBoxLight;
	private TextView mSmarkBoxTemp;
	private TextView mSmarkBoxHum;

	private ImageView mSmarkBoxLock;
	private ImageView mSmarkBoxUsb0;
	private ImageView mSmarkBoxUsb1;
	private ImageView mSmarkBoxUsb2;
	private ImageView mSmarkBoxUsb3;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station);

		mTime = (TextView) findViewById(R.id.station_time);
		mDate = (TextView) findViewById(R.id.station_date);

		mStationNum = (TextView) findViewById(R.id.station_num_bis);

		mStationState = (TextView) findViewById(R.id.station_state);
		mStationStateTime = (TextView) findViewById(R.id.station_state_time);

		mSmarkBoxLight = (TextView) findViewById(R.id.smarkbox_light);
		mSmarkBoxTemp = (TextView) findViewById(R.id.smarkbox_temp);
		mSmarkBoxHum = (TextView) findViewById(R.id.smarkbox_hum);

		mSmarkBoxLock = (ImageView) findViewById(R.id.lock_right);
		mSmarkBoxUsb0 = (ImageView) findViewById(R.id.usb0_right);
		mSmarkBoxUsb1 = (ImageView) findViewById(R.id.usb1_right);
		mSmarkBoxUsb2 = (ImageView) findViewById(R.id.usb2_right);
		mSmarkBoxUsb3 = (ImageView) findViewById(R.id.usb3_right);

		mFragmentManager = getFragmentManager();
		// If the language changes, this activity will be destroyed and then on
		// create, but the back stack may have kept several fragments. We pop
		// the BackStack when activity is created, and then add the first
		// fragment.
		mFragmentManager.popBackStack();
	}

	public void addFragment(Fragment fragment) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
				R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit);
		transaction.replace(R.id.station_fragment_content, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public void replaceFragment(Fragment fragment) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.replace(R.id.station_fragment_content, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public void addFragmentTag(Fragment fragment, String tag) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
				R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit);
		transaction.replace(R.id.station_fragment_content, fragment, tag);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public void setTime(String time) {
		mTime.setText(time);
	}

	public void setDate(String date) {
		mDate.setText(date);
	}

	public void setStationNum(String card) {
		mStationNum.setText(card);
	}

	public void setStationState(String state) {
		mStationState.setText(state);
	}

	public void setStationStateTime(String time) {
		mStationStateTime.setText(time);
	}

	public void setSmarkBoxLight(String time) {
		if(mSmarkBoxLight.getVisibility() != View.VISIBLE){
			mSmarkBoxLight.setVisibility(View.VISIBLE);
		}
		mSmarkBoxLight.setText(time);
	}

	public void setSmarkBoxTemp(String time) {
		if(mSmarkBoxTemp.getVisibility() != View.VISIBLE){
			mSmarkBoxTemp.setVisibility(View.VISIBLE);
		}
		mSmarkBoxTemp.setText(time);
	}

	public void setSmarkBoxHum(String time) {
		if(mSmarkBoxHum.getVisibility() != View.VISIBLE){
			mSmarkBoxHum.setVisibility(View.VISIBLE);
		}
		mSmarkBoxHum.setText(time);
	}

	public void setWsnInvisible() {
		if(mSmarkBoxLight.getVisibility() == View.VISIBLE){
			mSmarkBoxLight.setVisibility(View.INVISIBLE);
		}

		if(mSmarkBoxTemp.getVisibility() == View.VISIBLE){
			mSmarkBoxTemp.setVisibility(View.INVISIBLE);
		}

		if(mSmarkBoxHum.getVisibility() == View.VISIBLE){
			mSmarkBoxHum.setVisibility(View.INVISIBLE);
		}

	}

	public void setSmarkBoxLock(boolean display,int enable) {
		if(display){
			mSmarkBoxLock.setVisibility(View.VISIBLE);
		}else{
			mSmarkBoxLock.setVisibility(View.INVISIBLE);
			return;
		}

		if(enable == 1){
			mSmarkBoxLock.setImageResource(R.drawable.lock_enable);
		}else{
			mSmarkBoxLock.setImageResource(R.drawable.lock_disable);
		}
	}
	public void setSmarkBoxUsb0(boolean display,int enable) {
		if(display){
			mSmarkBoxUsb0.setVisibility(View.VISIBLE);
		}else{
			mSmarkBoxUsb0.setVisibility(View.INVISIBLE);
			return;
		}

		if(enable == 1){
			mSmarkBoxUsb0.setImageResource(R.drawable.usb_enable);
		}else{
			mSmarkBoxUsb0.setImageResource(R.drawable.usb_disable);
		}
	}
	public void setSmarkBoxUsb1(boolean display,int enable) {
		if(display){
			mSmarkBoxUsb1.setVisibility(View.VISIBLE);
		}else{
			mSmarkBoxUsb1.setVisibility(View.INVISIBLE);
			return;
		}

		if(enable == 1){
			mSmarkBoxUsb1.setImageResource(R.drawable.usb_enable);
		}else{
			mSmarkBoxUsb1.setImageResource(R.drawable.usb_disable);
		}
	}
	public void setSmarkBoxUsb2(boolean display,int enable) {
		if(display){
			mSmarkBoxUsb2.setVisibility(View.VISIBLE);
		}else{
			mSmarkBoxUsb2.setVisibility(View.INVISIBLE);
			return;
		}

		if(enable == 1){
			mSmarkBoxUsb2.setImageResource(R.drawable.usb_enable);
		}else{
			mSmarkBoxUsb2.setImageResource(R.drawable.usb_disable);
		}
	}
	public void setSmarkBoxUsb3(boolean display,int enable) {
		if(display){
			mSmarkBoxUsb3.setVisibility(View.VISIBLE);
		}else{
			mSmarkBoxUsb3.setVisibility(View.INVISIBLE);
			return;
		}

		if(enable == 1){
			mSmarkBoxUsb3.setImageResource(R.drawable.usb_enable);
		}else{
			mSmarkBoxUsb3.setImageResource(R.drawable.usb_disable);
		}
	}

}
