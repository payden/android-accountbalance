package com.obfuskate.accountbalance;


import java.text.NumberFormat;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class RecentTransactionsAdapter extends CursorAdapter {

  Context context;
  Cursor cursor;
  int flags;
  
  public static class ViewHolder {
    public TextView dateView;
    public TextView placeView;
    public TextView amountView;
  }
  
  public RecentTransactionsAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
    this.context = context;
    this.cursor = c;
    this.flags = flags;
  }
  
  @Override
  public void bindView(View v, Context c, Cursor cur) {
    TextView dateView = (TextView) v.findViewById(R.id.textViewDate);
    TextView placeView = (TextView) v.findViewById(R.id.textViewPlace);
    TextView amountView = (TextView) v.findViewById(R.id.textViewAmountLbl);
    dateView.setText(android.text.format.DateFormat.format("M/d", Long.valueOf(cur.getString(cur.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_DATE))) * 1000));
    placeView.setText(cur.getString(cur.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION)));
    NumberFormat fmt = NumberFormat.getCurrencyInstance();
    Long amount = cur.getLong(cur.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT));
    amountView.setText(fmt.format((double)amount / 100));
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return ((Activity)context).getLayoutInflater().inflate(R.layout.recent_transaction_row, parent, false);
  }
}
