package com.tokool.earphone.fragment;

import com.tokool.earphone.R;
import com.tokool.earphone.activity.BindDeviceActivity;
import com.tokool.earphone.activity.ClockActivity;
import com.tokool.earphone.service.BleService;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceFragment extends Fragment {
	
	private LinearLayout btnClock;
	private TextView tvElectricity, tvDeviceName, tvSynchronizeTip, tvConnectionTip;
	private Button btnUnbundle, btnCalled, btnSit;
	private MyBroadcastReceiver receiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
	
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("com.tokool.earphone.action.connected");
		filter.addAction("com.tokool.earphone.action.disconnected");
		filter.addAction("update_electricity");
		getActivity().registerReceiver(receiver, filter);
		
		View rootView=inflater.inflate(R.layout.fragment_device, container, false);
		
		btnClock=(LinearLayout)rootView.findViewById(R.id.btn_clock);
		btnClock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getActivity(), ClockActivity.class));
			}
		});
		
		btnUnbundle=(Button)rootView.findViewById(R.id.btn_unbind);
		btnUnbundle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LinearLayout dialog=(LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.dialog_tip, null);
				((TextView)dialog.findViewById(R.id.tv_title)).setText(getString(R.string.unbind));
				((TextView)dialog.findViewById(R.id.tv_tip)).setText(getString(R.string.sure_to_unbind));
				new AlertDialog.Builder(getActivity())
					.setView(dialog)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().remove("address").commit();
							getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().remove("name").commit();
							startActivity(new Intent(getActivity(), BindDeviceActivity.class));
							getActivity().finish();
						}
					})
					.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							
						}
					})
					.create()
					.show();
			}
		});
		
		tvDeviceName=(TextView)rootView.findViewById(R.id.tv_device_name);
		tvDeviceName.setText(getString(R.string.device_name)+getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getString("name", getString(R.string.no_device)));
		
		tvSynchronizeTip=(TextView)rootView.findViewById(R.id.tv_sychronize_tip);
		String dateString=getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getString("sychronize_time", null);
		if(dateString==null){
			tvSynchronizeTip.setText(getString(R.string.has_not_synchronize));
		}else{
			tvSynchronizeTip.setText(getString(R.string.sychronize_time)+dateString);
		}
				
		tvConnectionTip=(TextView)rootView.findViewById(R.id.tv_connection_tip);
		if(BleService.isConnected){
			tvConnectionTip.setText(getString(R.string.status_connected));
		}else{
			tvConnectionTip.setText(getString(R.string.status_disconnected));
		}
		
		tvElectricity=(TextView)rootView.findViewById(R.id.tv_electricity);
		tvElectricity.setText(getString(R.string.electricity)+BleService.electricity+"%");
		
		btnCalled=(Button)rootView.findViewById(R.id.btn_toggle_remind_when_called);
		BleService.isCalledOpen=getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getBoolean("isCalledOpen", false);
		if(BleService.isCalledOpen){
			btnCalled.setBackgroundResource(R.drawable.toggle_open);
		}else{
			btnCalled.setBackgroundResource(R.drawable.toggle_close);
		}
		btnCalled.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(BleService.isConnected){
					if(BleService.isCalledOpen){
						//关闭来电拦截
						btnCalled.setBackgroundResource(R.drawable.toggle_close);
						BleService.isCalledOpen=false;
						getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putBoolean("isCalledOpen", false).commit();
					}else{
						//开启来电拦截
						btnCalled.setBackgroundResource(R.drawable.toggle_open);
						BleService.isCalledOpen=true;
						getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putBoolean("isCalledOpen", true).commit();
					}
				}else{
					Toast.makeText(getActivity(), getString(R.string.can_not_change_when_disconnected), Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		btnSit=(Button)rootView.findViewById(R.id.btn_toggle_remind_when_sit);
		BleService.isSitOpen=getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getBoolean("isSitOpen", false);
		if(BleService.isSitOpen){
			btnSit.setBackgroundResource(R.drawable.toggle_open);
		}else{
			btnSit.setBackgroundResource(R.drawable.toggle_close);
		}
		btnSit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(BleService.isConnected){
					if(BleService.isSitOpen){
						//关闭久坐提醒
						btnSit.setBackgroundResource(R.drawable.toggle_close);
						getActivity().sendBroadcast(new Intent("close_long_sit_remind"));
						BleService.isSitOpen=false;
						getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putBoolean("isSitOpen", false).commit();
					}else{
						//开启久坐提醒
						btnSit.setBackgroundResource(R.drawable.toggle_open);
						getActivity().sendBroadcast(new Intent("open_long_sit_remind"));
						BleService.isSitOpen=true;
						getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putBoolean("isSitOpen", true).commit();
					}
				}else{
					Toast.makeText(getActivity(), getString(R.string.can_not_change_when_disconnected), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
	}

	public class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("com.tokool.earphone.action.connected")){
				tvConnectionTip.setText(getString(R.string.status_connected));
			}
			if(action.equals("com.tokool.earphone.action.disconnected")){
				tvConnectionTip.setText(getString(R.string.status_disconnected));
				tvElectricity.setText(getString(R.string.electricity)+BleService.electricity+"%");
			}
			if(action.equals("update_electricity")){
				tvElectricity.setText(getString(R.string.electricity)+BleService.electricity+"%");
			}
			if(action.equals("history_steps_data")){
				String dateString=getActivity().getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getString("sychronize_time", null);
				if(dateString!=null){
					tvSynchronizeTip.setText(getString(R.string.sychronize_time)+dateString);
				}
			}
			if(action.equals("update_long_sit_toggle")){
				if(BleService.isSitOpen){
					btnCalled.setBackgroundResource(R.drawable.toggle_open);
				}else{
					btnCalled.setBackgroundResource(R.drawable.toggle_close);
				}
			}
		}
		
	}
}
