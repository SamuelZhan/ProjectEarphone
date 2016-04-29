package com.tokool.earphone.customview;

import com.tokool.earphone.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class RoundCornerImageView extends ImageView {

	private Paint paint;
	private Xfermode xfermode=new PorterDuffXfermode(Mode.DST_IN);
	private Bitmap maskBitmap;
	private int cornerRadius;
	private int type;
	public static final int TYPE_CIRCLE=0;
	public static final int TYPE_ROUND=1;
	
	private final int SPACING_WIDTH_FIRST_CIRCLE=8;
	private final int SPACING_WIDTH_SECOND_CIRCLE=8;
	
	public RoundCornerImageView(Context context) {
		// TODO Auto-generated constructor stub
		this(context, null);
		paint=new Paint();
		paint.setAntiAlias(true);
	}
	
	public RoundCornerImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		paint=new Paint();
		paint.setAntiAlias(true);
		
		TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView);
		
		cornerRadius=a.getDimensionPixelSize(R.styleable.RoundCornerImageView_cornerRadius, 
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
		type=a.getInt(R.styleable.RoundCornerImageView_type, TYPE_CIRCLE);
		
		a.recycle();
	}
	
	public RoundCornerImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if(type==TYPE_CIRCLE){
			int width=Math.min(getMeasuredWidth(), getMeasuredHeight());
			setMeasuredDimension(width, width);
		}
	}

	@Override
	protected void onDraw(Canvas canvas){
		Bitmap bitmap=null;
		
		Drawable drawable=getDrawable();
		
		if(drawable==null) return;
		
		int dwidth=drawable.getIntrinsicWidth();
		int dheight=drawable.getIntrinsicHeight();
							
		bitmap=Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);					
		float scale=1.0f;
		
		//将创建的bitmap当做画布使用，可以在bitmap上绘图
		Canvas drawableCanvas=new Canvas(bitmap);
		if(type==TYPE_CIRCLE){
			scale=getWidth()*1.0f/Math.min(dwidth, dheight);
		}else{
			scale=Math.max(getWidth()*1.0f/dwidth, getHeight()*1.0f/dheight);
		}
		
		drawable.setBounds(0+dp2px(SPACING_WIDTH_FIRST_CIRCLE), 0+dp2px(SPACING_WIDTH_FIRST_CIRCLE), 
				(int)(scale*dwidth)-dp2px(SPACING_WIDTH_FIRST_CIRCLE), (int)(scale*dheight)-dp2px(SPACING_WIDTH_FIRST_CIRCLE));	
	
		drawable.draw(drawableCanvas);
		if(maskBitmap==null || maskBitmap.isRecycled()){
			maskBitmap=getBitmap();
		}
		paint.reset();
		paint.setFilterBitmap(false);
		paint.setXfermode(xfermode);
		
		drawableCanvas.drawBitmap(maskBitmap, 0	, 0, paint);
		
		paint.setXfermode(null);
		if(type==TYPE_CIRCLE){
			//画第一个圆环
			paint.setColor(0x22000000);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(4);
			paint.setAntiAlias(true);								
			drawableCanvas.drawCircle(getWidth()/2, getWidth()/2, getWidth()/2-2f, paint);
			
			//画第二个圆环
			paint.setColor(0x22000000);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(8);
			paint.setAntiAlias(true);								
			drawableCanvas.drawCircle(getWidth()/2, getWidth()/2, getWidth()/2-4f-SPACING_WIDTH_SECOND_CIRCLE, paint);
			
		}
		
		//最后把绘制好的bitmap画在view的canvas上
		canvas.drawBitmap(bitmap, 0, 0, null);	

	}

	private Bitmap getBitmap(){
		Bitmap bitmap=Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		Canvas canvas=new Canvas(bitmap);
		Paint paint =new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		
		if(type==TYPE_ROUND){
			canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), cornerRadius, cornerRadius, paint);
		}else{			
			canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2-dp2px(SPACING_WIDTH_FIRST_CIRCLE), paint);				
		}
		
		return bitmap;
	}
	
	private int dp2px(int dp){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

}