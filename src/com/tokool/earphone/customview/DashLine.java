package com.tokool.earphone.customview;

import com.tokool.earphone.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DashLine extends View {
	
	private int lineColor;
	private int dashWidth;
	private int solidWidth;
	private Paint paint;

	public DashLine(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		
		TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.DashLine);
		
		lineColor=array.getColor(R.styleable.DashLine_lineColor, Color.BLACK);
		dashWidth=array.getDimensionPixelSize(R.styleable.DashLine_dashWidth, 20);
		solidWidth=array.getDimensionPixelSize(R.styleable.DashLine_solidWidth, 20);
		
		array.recycle();
		
		paint=new Paint();
		paint.setColor(lineColor);
		
		paint.setStrokeWidth(4f);
		paint.setStyle(Style.STROKE);
//		paint.setAntiAlias(true);
		
	}

	public DashLine(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public DashLine(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		DashPathEffect effect=new DashPathEffect(new float[]{8, 8}, 50);
		paint.setPathEffect(effect);
		Path path=new Path();
		path.moveTo(0, getHeight()/2);
		path.lineTo(getWidth(), getHeight()/2);
		canvas.drawPath(path, paint);
	}

	
}
