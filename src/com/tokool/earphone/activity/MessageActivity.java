package com.tokool.earphone.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tokool.earphone.R;
import com.tokool.earphone.customview.RoundCornerImageView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import net.simonvt.numberpicker.NumberPicker;

public class MessageActivity extends Activity {

	private ImageView btnBack;
	private RoundCornerImageView btnHeadImage;
	private LinearLayout btnSex, btnHeight, btnWeight, btnStepLength;
	private TextView tvSex, tvHeight, tvWeight, tvStepLength;
	private EditText etNickname;
	private MyOnClickListener listener;
	private boolean headImageHasChanged=false;
	private String nickname;
	private int sex, height, weight, stepLength, unit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_message);
		
		nickname=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("nickname", "");
		sex=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("sex", 0);
		height=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("height", 170);
		weight=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("weight", 65);
		stepLength=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("stepLength", 65);	
		unit=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("unit", 0);
		
		listener=new MyOnClickListener();
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(listener);
		
		btnHeadImage=(RoundCornerImageView)findViewById(R.id.btn_headimage);
		btnHeadImage.setOnClickListener(listener);		
		try {
			btnHeadImage.setImageBitmap(BitmapFactory.decodeStream(openFileInput("headImage")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			btnHeadImage.setImageResource(R.drawable.default_headimage);
		}
		
		etNickname=(EditText)findViewById(R.id.et_nickname);
		etNickname.setText(nickname);
		etNickname.setSelectAllOnFocus(true);
		etNickname.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence nickname, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putString("nickname", nickname.toString()).commit();
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		btnSex=(LinearLayout)findViewById(R.id.btn_sex);
		btnSex.setOnClickListener(listener);
		
		btnHeight=(LinearLayout)findViewById(R.id.btn_height);
		btnHeight.setOnClickListener(listener);
		
		btnWeight=(LinearLayout)findViewById(R.id.btn_weight);
		btnWeight.setOnClickListener(listener);
		
		btnStepLength=(LinearLayout)findViewById(R.id.btn_step_length);
		btnStepLength.setOnClickListener(listener);
		
		tvSex=(TextView)findViewById(R.id.tv_sex);
		if(sex==0){
			tvSex.setText(getString(R.string.male));
		}else{
			tvSex.setText(getString(R.string.female));
		}
		
		tvHeight=(TextView)findViewById(R.id.tv_height);
		if(unit==0){
			tvHeight.setText(height+"cm");
		}else{
			tvHeight.setText(Math.round(height/2.54f)+"in");
		}	
		
		tvWeight=(TextView)findViewById(R.id.tv_weight);		
		if(unit==0){
			tvWeight.setText(weight+"kg");
		}else{
			tvWeight.setText(Math.round(weight/0.45359f)+"lb");
		}
		
		tvStepLength=(TextView)findViewById(R.id.tv_step_length);
		if(unit==0){
			tvStepLength.setText(stepLength+"cm");
		}else{
			tvStepLength.setText(Math.round(stepLength/2.54f)+"in");
		}
		
		
	}
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.ACTION_UP){
			if(headImageHasChanged){
				setResult(1);
			}
		}
		return super.onKeyUp(keyCode, event);
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode==1 && resultCode==RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				return;
			}
			Intent intent=new Intent();
			intent.setAction("com.android.camera.action.CROP");
			intent.setDataAndType(uri, "image/*");			
			intent.putExtra("crop", true);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 80);
			intent.putExtra("outputY", 80);
			intent.putExtra("return-data", true);
			
			startActivityForResult(intent, 3);
		}
		if(requestCode==2 && resultCode==RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				Bundle bundle=data.getExtras();
				if(bundle!=null){
					Bitmap bitmap=(Bitmap)bundle.get("data");
					File f=new File(Environment.getExternalStorageDirectory()+"/earphone", "clipImage.png");
					if(f.exists()){
						f.delete();
					}
					try{
						FileOutputStream fos=new FileOutputStream(f);
						bitmap.compress(CompressFormat.PNG, 80, fos);
						fos.flush();
						fos.close();
					}catch(Exception e){						
						Log.d("zz", e.toString());
						return;
					}
					uri=Uri.fromFile(f);
				}
			}
			Intent intent=new Intent();
			intent.setAction("com.android.camera.action.CROP");
			intent.setDataAndType(uri, "image/*");			
			intent.putExtra("crop", true);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 80);
			intent.putExtra("outputY", 80);
			intent.putExtra("return-data", true);				
			startActivityForResult(intent, 3);
				
			
		}
		if(requestCode==3 && resultCode==RESULT_OK){
			final Bitmap bitmap=data.getParcelableExtra("data");
			btnHeadImage.setImageBitmap(bitmap);
			headImageHasChanged=true;
			try {
				ByteArrayOutputStream baos=new ByteArrayOutputStream();						
				bitmap.compress(CompressFormat.PNG, 10, baos);
				FileOutputStream fos=openFileOutput("headImage", MODE_PRIVATE);			
				fos.write(baos.toByteArray());
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}								
			
		}
		
	}
	
	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId()==R.id.btn_back){
				if(headImageHasChanged){
					setResult(1);
				}
				finish();
			}else if(v.getId()==R.id.btn_headimage){
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_headimage, null);
				
				final AlertDialog alertDialog=new AlertDialog.Builder(MessageActivity.this, R.style.MyDialogTheme)
						.setView(dialog)
						.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								
							}
						})
						.create();					
						alertDialog.show();
				Button btnAlbum=(Button)dialog.findViewById(R.id.btn_album);
				btnAlbum.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(Intent.ACTION_PICK);
						intent.setType("image/*");
						startActivityForResult(intent, 1);
						alertDialog.dismiss();
					}
				});
				
				Button btnCamera=(Button)dialog.findViewById(R.id.btn_camera);
				btnCamera.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
						startActivityForResult(intent, 2);
						alertDialog.dismiss();
					}
				});
				
			}else if(v.getId()==R.id.btn_sex){
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_sex, null);
				RadioButton btnMale=(RadioButton)dialog.findViewById(R.id.sex_male);
				RadioButton btnFemale=(RadioButton)dialog.findViewById(R.id.sex_female);
				final RadioGroup radioGroup=(RadioGroup)dialog.findViewById(R.id.sex_radiogroup);
				if(sex==0){
					btnMale.setChecked(true);
				}else{
					btnFemale.setChecked(true);
				}				
				AlertDialog alertDialog=new AlertDialog.Builder(MessageActivity.this, R.style.MyDialogTheme)
				.setView(dialog)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(radioGroup.getCheckedRadioButtonId()==R.id.sex_male){
							sex=0;
							tvSex.setText(getString(R.string.male));
							
						}else{							
							sex=1;
							tvSex.setText(getString(R.string.female));
						}
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("sex", sex).commit();
					}
				})
				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create();
				alertDialog.show();
				
			}else if(v.getId()==R.id.btn_height){
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_height_weight_steplength, null);
				((TextView)dialog.findViewById(R.id.tv_dialog_title)).setText(getString(R.string.height));;				
				final NumberPicker np=(NumberPicker) dialog.findViewById(R.id.number_picker);				
				if(unit==0){
					np.setMinValue(130);
					np.setMaxValue(220);
					np.setValue(height);
					np.setLable(" cm");
				}else{
					np.setMinValue(50);
					np.setMaxValue(90);
					np.setValue(Math.round(height/2.54f));
					np.setLable(" in");
				}
				AlertDialog alertDialog=new AlertDialog.Builder(MessageActivity.this, R.style.MyDialogTheme)
				.setView(dialog)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(unit==0){
							height=np.getValue();
							tvHeight.setText(height+"cm");
						}else{
							height=Math.round(np.getValue()*2.54f);
							tvHeight.setText(np.getValue()+"in");
						}
						
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("height", height).commit();
					}
				})
				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create();
				alertDialog.show();
				
			}else if(v.getId()==R.id.btn_weight){
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_height_weight_steplength, null);
				((TextView)dialog.findViewById(R.id.tv_dialog_title)).setText(getString(R.string.weight));;				
				final NumberPicker np=(NumberPicker) dialog.findViewById(R.id.number_picker);
				if(unit==0){
					np.setMinValue(30);
					np.setMaxValue(150);
					np.setValue(weight);
					np.setLable(" kg");
				}else{
					np.setMinValue(60);
					np.setMaxValue(330);
					np.setValue(Math.round(weight/0.45359f));
					np.setLable(" lb");
				}
				
				AlertDialog alertDialog=new AlertDialog.Builder(MessageActivity.this, R.style.MyDialogTheme)
				.setView(dialog)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(unit==0){
							weight=np.getValue();
							tvWeight.setText(weight+"kg");
						}else{
							weight=Math.round(np.getValue()*0.45359f);
							tvWeight.setText(np.getValue()+"lb");
						}
						
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("weight", weight).commit();
					}
				})
				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create();
				alertDialog.show();
				
			}else if(v.getId()==R.id.btn_step_length){
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_height_weight_steplength, null);
				((TextView)dialog.findViewById(R.id.tv_dialog_title)).setText(getString(R.string.step_length));;				
				final NumberPicker np=(NumberPicker) dialog.findViewById(R.id.number_picker);
				if(unit==0){
					np.setMinValue(20);
					np.setMaxValue(120);
					np.setValue(stepLength);
					np.setLable(" cm");
				}else{
					np.setMinValue(0);
					np.setMaxValue(50);
					np.setValue(Math.round(stepLength/2.54f));
					np.setLable(" in");
				}
				
				AlertDialog alertDialog=new AlertDialog.Builder(MessageActivity.this, R.style.MyDialogTheme)
				.setView(dialog)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(unit==0){
							stepLength=np.getValue();
							tvStepLength.setText(stepLength+"cm");
						}else{
							stepLength=Math.round(np.getValue()*2.54f);
							tvStepLength.setText(np.getValue()+"in");
						}						
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("stepLength", stepLength).commit();
					}
				})
				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create();
				alertDialog.show();

			}
			
		}
		
	}

}
