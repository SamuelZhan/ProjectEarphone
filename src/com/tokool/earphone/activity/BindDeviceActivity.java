package com.tokool.earphone.activity;

import java.util.ArrayList;
import java.util.HashMap;

import com.tokool.earphone.R;
import com.tokool.earphone.customview.CircleProgress;
import com.tokool.earphone.customview.LoadingDialog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BindDeviceActivity extends Activity {
	
	private Button btnRefresh;
	private RelativeLayout layout;
	private TextView tvState;
	private CircleProgress circleProgress;
	private ImageView ivError;
	private ListView lvDevices;
	private BluetoothAdapter btAdapter;
	private ArrayList<HashMap<String, Object>> devices;
	private MyBaseAdapter lvAdapter;
	private Handler handler;
//	private LoadingDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		String address=getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getString("address", "unbound");
		if(!address.equals("unbound")){
			startActivity(new Intent(this, GuideActivity.class));
			finish();
		}
		
		setContentView(R.layout.activity_bind_device);
		
		handler=new Handler();
		
//		dialog=new LoadingDialog(this, "正在绑定");
	
		btAdapter=((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
		if(btAdapter!=null && !btAdapter.isEnabled()){
			btAdapter.enable();
		}
		
		btnRefresh=(Button)findViewById(R.id.btn_refresh);
		btnRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				scan();
			}
		});
		
		tvState=(TextView)findViewById(R.id.tv_state);
		
		circleProgress=(CircleProgress)findViewById(R.id.circle_progress);
		
		ivError=(ImageView)findViewById(R.id.iv_error);
				
		devices=new ArrayList<HashMap<String, Object>>();
		lvDevices=(ListView)findViewById(R.id.lv_devices);
		lvAdapter=new MyBaseAdapter();
		lvDevices.setAdapter(lvAdapter);
		lvDevices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
//				dialog.show();
				String name=(String)devices.get(position).get("name");
				String address=(String) devices.get(position).get("address");
				getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putString("name", name).commit();
				getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).edit().putString("address", address).commit();
				startActivity(new Intent(BindDeviceActivity.this, GuideActivity.class));
				finish();
			}
		});
		
		layout=(RelativeLayout)findViewById(R.id.layout);		
		
		scan();
		
	}
	
	private void scan(){
		// TODO Auto-generated method stub
		layout.setVisibility(View.VISIBLE);
		circleProgress.setVisibility(View.VISIBLE);
		lvDevices.setVisibility(View.GONE);
		ivError.setVisibility(View.GONE);
		devices.clear();
		if(btAdapter.isDiscovering()){
			btAdapter.stopLeScan(mLeScanCallback);
		}
		btAdapter.startLeScan(mLeScanCallback);
		btnRefresh.setEnabled(false);
		btnRefresh.setText(getString(R.string.searching));
		btnRefresh.setBackgroundResource(R.drawable.shape_btn_scan_grey);	
		tvState.setText(getString(R.string.searching_device));
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				btAdapter.stopLeScan(mLeScanCallback);
				btnRefresh.setEnabled(true);
				btnRefresh.setText(getString(R.string.refresh));
				btnRefresh.setBackgroundResource(R.drawable.selector_btn_white_to_grey_round);
				if(devices.size()>0){
					layout.setVisibility(View.GONE);
					lvDevices.setVisibility(View.VISIBLE);
					lvAdapter.notifyDataSetChanged();							
				}else{
					tvState.setText(getString(R.string.no_device_discovered));
					circleProgress.setVisibility(View.GONE);
					ivError.setVisibility(View.VISIBLE);
				}
			}
		}, 3000);
	}
	
	private LeScanCallback mLeScanCallback=new LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice btDevice, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
			String name=btDevice.getName();
			if(name!=null && name.equals("CL-1003")){
				for(HashMap<String, Object> device: devices){
					if(device.get("address").equals(btDevice.getAddress())){
						return;
					}
				}
				HashMap<String, Object> device=new HashMap<String, Object>();
				device.put("name", btDevice.getName());
				device.put("address", btDevice.getAddress());
				devices.add(device);
			}

		}
	};
	
	private class MyBaseAdapter extends BaseAdapter{
		
		private class ViewHolder{
			
			private TextView tvAddress;
			private TextView tvName;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return devices.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return devices.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView==null){
				convertView=getLayoutInflater().inflate(R.layout.list_item_devices, null);
				holder=new ViewHolder();
				holder.tvAddress=(TextView)convertView.findViewById(R.id.tv_address);
				holder.tvName=(TextView)convertView.findViewById(R.id.tv_name);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			HashMap<String, Object> device=devices.get(position);
			holder.tvAddress.setText((String)device.get("address"));
			holder.tvName.setText((String)device.get("name"));
			return convertView;
		}
		
	}

}
