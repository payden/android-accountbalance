package com.obfuskate.accountbalance;


import java.text.NumberFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class RecentTransactionsAdapter extends ArrayAdapter<RecentTransaction> {

  Context context;
  int layoutResourceId;
  List<RecentTransaction> transactions;
  
  public static class ViewHolder {
    public TextView dateView;
    public TextView placeView;
    public TextView amountView;
  }
  
  public RecentTransactionsAdapter(Context context, int textViewResourceId, List<RecentTransaction> objects) {
    super(context, textViewResourceId, objects);
    this.context = context;
    this.layoutResourceId = textViewResourceId;
    this.transactions = objects;
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;
    ViewHolder holder = null;
    if (row == null) {
      LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      row = li.inflate(R.layout.recent_transaction_row, null);
      holder = new ViewHolder();
      holder.dateView = (TextView) row.findViewById(R.id.textViewDate);
      holder.placeView = (TextView) row.findViewById(R.id.textViewPlace);
      holder.amountView = (TextView) row.findViewById(R.id.textViewAmountLbl);
      row.setTag(holder);
    } else {
      holder = (ViewHolder) row.getTag();
    }
    final RecentTransaction transaction = transactions.get(position);
    if (transaction != null) {
      NumberFormat fmt = NumberFormat.getCurrencyInstance();
      holder.dateView.setText(android.text.format.DateFormat.format("M/d", transaction.transactionDate));
      holder.placeView.setText(transaction.transactionPlace);
      holder.amountView.setText(fmt.format((double) transaction.transactionAmount / 100));
      
    }
    return row;
  }

}
