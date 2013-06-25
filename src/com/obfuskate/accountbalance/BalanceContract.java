package com.obfuskate.accountbalance;

import android.provider.BaseColumns;

public class BalanceContract {
  
  private BalanceContract() {}
  
  
  
  public static abstract class BalanceEntry implements BaseColumns {
    public static final String TABLE_NAME = "entry";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_LOCATION = "location";
    public static final String COLUMN_NAME_AMOUNT = "amount";
    public static final String COLUMN_NAME_LAT = "lat";
    public static final String COLUMN_NAME_LONG = "long";
  }
}
