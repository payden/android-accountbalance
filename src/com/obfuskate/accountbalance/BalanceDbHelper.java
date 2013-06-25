package com.obfuskate.accountbalance;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class BalanceDbHelper extends SQLiteOpenHelper {

  public static final String SQL_CREATE = 
      "CREATE TABLE " + BalanceContract.BalanceEntry.TABLE_NAME + " (" +
      BalanceContract.BalanceEntry._ID + " INTEGER PRIMARY KEY," +
      BalanceContract.BalanceEntry.COLUMN_NAME_DATE + " INTEGER," +
      BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION + " TEXT," +
      BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT + " INTEGER," +
      BalanceContract.BalanceEntry.COLUMN_NAME_LAT + " TEXT," +
      BalanceContract.BalanceEntry.COLUMN_NAME_LONG + " TEXT" +
      ")";
  public static final String SQL_DELETE = 
      "DROP TABLE IF EXISTS " + BalanceContract.BalanceEntry.TABLE_NAME;
  
  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "balance.db";
  
  public BalanceDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE);
  }
  
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SQL_DELETE);
    onCreate(db);
  }

}
