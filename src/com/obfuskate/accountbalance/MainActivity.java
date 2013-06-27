package com.obfuskate.accountbalance;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
  private TransactionsTaskData mTransactionsTaskData;

  private class TransactionsTaskData {
    public Cursor cursor;
    public ListView listView;
  }
  
  private class PopulateRecentTransactionsTask extends AsyncTask<ListView, Void, TransactionsTaskData> {

    @Override
    protected void onPostExecute(TransactionsTaskData d) {
      mTransactionsTaskData = d;
      ListView lv = d.listView;
      ProgressBar progress = (ProgressBar) findViewById(R.id.progressBarRecent);
      progress.setVisibility(View.GONE);
      Cursor cur = d.cursor;
      //RecentTransactionsAdapter adapter = new RecentTransactionsAdapter(lv.getContext(), R.id.listViewRecent, transactions);

      RecentTransactionsAdapter adapter = new RecentTransactionsAdapter(lv.getContext(), cur, 0);

      LayoutInflater inflater = ((Activity)lv.getContext()).getLayoutInflater();
      LinearLayout layout = new LinearLayout(lv.getContext());
      LinearLayout noTransactionsLayout = new LinearLayout(lv.getContext());
      inflater.inflate(R.layout.recent_transaction_header, layout);
      lv.addHeaderView(layout);
      /* fix this if (transactions.size() == 0) {
        inflater.inflate(R.layout.recent_transactions_none, noTransactionsLayout);
        lv.addHeaderView(noTransactionsLayout);
      } */
      
      lv.setAdapter(adapter);
      lv.setSelection(adapter.getCount() - 1);
      lv.setOnItemClickListener(new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> av, View v, int position, long garbage) {
          if (position == 0) {
            return;
          }
          Intent detailIntent = new Intent(v.getContext(), DetailActivity.class);
          detailIntent.putExtra("tId", av.getItemIdAtPosition(position));
          startActivity(detailIntent);
        }
        
      });
    }
    
    @Override
    protected TransactionsTaskData doInBackground(ListView... args) {
      ListView v = args[0];
      ArrayList<RecentTransaction> transactionsArray = new ArrayList<RecentTransaction>();
      BalanceDbHelper dbHelper = new BalanceDbHelper(v.getContext());
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      String[] projection = {BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT,
          BalanceContract.BalanceEntry.COLUMN_NAME_DATE,
          BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION,
          BalanceContract.BalanceEntry._ID};
      Cursor cur = db.query(BalanceContract.BalanceEntry.TABLE_NAME, projection, null, null, null, null, BalanceContract.BalanceEntry.COLUMN_NAME_DATE);
      TransactionsTaskData data = new TransactionsTaskData();
      data.listView = v;
      data.cursor = cur;
      return data;
    }
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    ListView lv = (ListView) findViewById(R.id.listViewRecent);

    //do database query, create adapter, and set listview adapter in background
    ProgressBar progress = (ProgressBar) findViewById(R.id.progressBarRecent);
    progress.setVisibility(View.VISIBLE);
    (new PopulateRecentTransactionsTask()).execute(lv);
    
    
    Button depositBtn = (Button) findViewById(R.id.buttonDeposit);
    Button purchaseBtn = (Button) findViewById(R.id.buttonPurchase);
    
    depositBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent depositIntent = new Intent(v.getContext(), DepositActivity.class);
        startActivity(depositIntent);
      }
    });
    
    purchaseBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent purchaseIntent = new Intent(v.getContext(), PurchaseActivity.class);
        startActivity(purchaseIntent);
      }
    });
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    updateBalance();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
  
  protected void updateBalance() {
    BalanceDbHelper dbHelper = new BalanceDbHelper(this);
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.rawQuery("SELECT TOTAL(" + BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT + ") FROM " + BalanceContract.BalanceEntry.TABLE_NAME, null);
    c.moveToFirst();
    TextView currentBal = (TextView) findViewById(R.id.textViewCurrent);
    NumberFormat fmt = NumberFormat.getCurrencyInstance();
    Double amt = (double) c.getLong(0) / 100;
    currentBal.setText(fmt.format(amt));
    db.close();
  }

}
