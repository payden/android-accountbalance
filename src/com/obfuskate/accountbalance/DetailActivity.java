package com.obfuskate.accountbalance;

import java.text.NumberFormat;
import java.util.Date;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;


public class DetailActivity extends Activity {
  private TextView whenText;
  private TextView whereText;
  private TextView amountText;
  private GoogleMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    Intent detailIntent = getIntent();
    long tId = detailIntent.getLongExtra("tId", -1);
    if (tId == -1) {
      Toast.makeText(this, "Invalid transaction id", Toast.LENGTH_SHORT).show();
      Intent mainIntent = new Intent(this, MainActivity.class);
      startActivity(mainIntent);
      return;
    }
    whenText = (TextView) findViewById(R.id.textViewWhen);
    whereText = (TextView) findViewById(R.id.textViewWhere);
    amountText = (TextView) findViewById(R.id.textViewAmountDetail);
    BalanceDbHelper dbHelper = new BalanceDbHelper(this);
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String[] projection = {BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT,
        BalanceContract.BalanceEntry.COLUMN_NAME_DATE,
        BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION,
        BalanceContract.BalanceEntry.COLUMN_NAME_LAT,
        BalanceContract.BalanceEntry.COLUMN_NAME_LONG};
    Cursor cur = db.query(BalanceContract.BalanceEntry.TABLE_NAME, projection, BalanceContract.BalanceEntry._ID + " = ?", new String[] {String.valueOf(tId)}, null, null, null);
    cur.moveToFirst();
    NumberFormat fmt = NumberFormat.getCurrencyInstance();
    Date transactionDate = new Date(cur.getLong(cur.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_DATE)) * 1000);
    whenText.setText(android.text.format.DateFormat.format("MM/dd/yy @ h:mmaa", transactionDate));
    whereText.setText(cur.getString(cur.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION)));
    amountText.setText(fmt.format((double) cur.getLong(cur.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT)) / 100));
    db.close();
    
    //perform map setup
    LatLng transactionLatLng = new LatLng(Double.valueOf(cur.getString(cur.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_LAT))),
        Double.valueOf(cur.getString(cur.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_LONG))));
    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
    map.addMarker(new MarkerOptions().position(transactionLatLng).title(whereText.getText().toString()));
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(transactionLatLng, 12));
    // Show the Up button in the action bar.
    setupActionBar();
  }

  /**
   * Set up the {@link android.app.ActionBar}.
   */
  private void setupActionBar() {

    getActionBar().setDisplayHomeAsUpEnabled(true);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.detail, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      // This ID represents the Home or Up button. In the case of this
      // activity, the Up button is shown. Use NavUtils to allow users
      // to navigate up one level in the application structure. For
      // more details, see the Navigation pattern on Android Design:
      //
      // http://developer.android.com/design/patterns/navigation.html#up-vs-back
      //
      NavUtils.navigateUpFromSameTask(this);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
