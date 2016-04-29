package com.tokool.earphone.fragment;

import java.io.FileNotFoundException;

import com.tokool.earphone.R;
import com.tokool.earphone.activity.MessageActivity;
import com.tokool.earphone.activity.SettingActivity;
import com.tokool.earphone.activity.TargetActivity;
import com.tokool.earphone.customview.RoundCornerImageView;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MineFragment extends Fragment {
	
	private RoundCornerImageView ivHeadImage;
	private ImageView btnTarget, btnMessage, btnSetting;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView=inflater.inflate(R.layout.fragment_mine, container, false);
		
		ivHeadImage=(RoundCornerImageView)rootView.findViewById(R.id.iv_headimage);
		try {
			ivHeadImage.setImageBitmap(BitmapFactory.decodeStream(getActivity().openFileInput("headImage")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			ivHeadImage.setImageResource(R.drawable.default_headimage);
		}
		
		btnTarget=(ImageView)rootView.findViewById(R.id.btn_target);
		btnTarget.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getActivity(), TargetActivity.class));
			}
		});
		
		btnMessage=(ImageView)rootView.findViewById(R.id.btn_message);
		btnMessage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivityForResult(new Intent(getActivity(), MessageActivity.class), 1);
			}
		});
		
		btnSetting=(ImageView)rootView.findViewById(R.id.btn_setting);
		btnSetting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getActivity(), SettingActivity.class));
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode==1 && resultCode==1){
			try {
				ivHeadImage.setImageBitmap(BitmapFactory.decodeStream(getActivity().openFileInput("headImage")));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				ivHeadImage.setImageResource(R.drawable.default_headimage);
			}
		}
	}

}
