package com.tokool.earphone.service;

import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class CallingRemindService extends Service {
	
	private UUID serviceUUID=UUID.fromString("00001523-1212-efde-1523-785feabcd123");
	private UUID characteristicWriteUUID=UUID.fromString("00001524-1212-efde-1523-785feabcd123");
	private UUID characteristicReadUUID=UUID.fromString("00001525-1212-efde-1523-785feabcd123");
	private UUID descriptorUUID=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	private BluetoothGatt btGatt;
	private BluetoothGattCharacteristic characteristic;
	private String address;

	private Handler handler;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();		
		handler=new Handler();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();		
		handler.removeCallbacksAndMessages(null);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		address=getSharedPreferences("BoundDevice", Context.MODE_PRIVATE).getString("address", "unbound");
		if(!address.equals("未绑定")){
			BluetoothDevice device=((BluetoothManager)getSystemService(BLUETOOTH_SERVICE))
					.getAdapter().getRemoteDevice(address);
			if(device.getName()!=null){				
				btGatt=device.connectGatt(this, true, mBluetoothGattCallback);			
			}
		}	
		return super.onStartCommand(intent, flags, startId);
		
		
	}	
	
	private BluetoothGattCallback mBluetoothGattCallback=new BluetoothGattCallback(){

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			// TODO Auto-generated method stub
			if(newState==BluetoothGatt.STATE_CONNECTED){
				gatt.discoverServices();
			}
		}
		
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			if(status==BluetoothGatt.GATT_SUCCESS){
				BluetoothGattService gattService=gatt.getService(serviceUUID);							
				characteristic=gattService.getCharacteristic(characteristicWriteUUID);
				gatt.setCharacteristicNotification(characteristic, true);
											
				BluetoothGattCharacteristic gattCharacteristicRead=gattService.getCharacteristic(characteristicReadUUID);
				gatt.setCharacteristicNotification(gattCharacteristicRead, true);
				BluetoothGattDescriptor gattDescriptor=gattCharacteristicRead.getDescriptor(descriptorUUID);
				gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				gatt.writeDescriptor(gattDescriptor);
				
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						byte[] bytes=new byte[4];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x97;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x14;
						if(btGatt!=null && characteristic!=null){							
							characteristic.setValue(bytes);					
							btGatt.writeCharacteristic(characteristic);
						}
					}
				}, 500);
				
			}
		}	
		
	};
	

}
