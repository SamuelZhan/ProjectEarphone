package com.tokool.earphone.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql="CREATE TABLE sportData(_id INTEGER PRIMARY KEY AUTOINCREMENT, steps INT, targetSteps INT,"
				+ "calorie FLOAT, distance INT, sportTime INT, stepString VARCHAR, date VARCHAR)";
		db.execSQL(sql);
		sql="CREATE TABLE sportDataOfYear(_id INTEGER PRIMARY KEY AUTOINCREMENT, totalSteps INT, "
				+ "totalCalorie FLOAT, totalDistance INT, month VARCHAR)";
		db.execSQL(sql);
		sql="CREATE TABLE heartRate(_id INTEGER PRIMARY KEY AUTOINCREMENT, heartRate INT, time VARCHAR, date VARCHAR, month VARCHAR)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
