package com.obfuskate.accountbalance;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SpendingReportActivity extends Activity {
  private class ReportData {
    Integer value;
    Integer color;
    String location;
    public final Integer[] colorSet = {
        Color.BLUE,
        Color.GRAY,
        Color.GREEN,
        Color.MAGENTA,
        Color.YELLOW,
        Color.RED,
        Color.DKGRAY,
        Color.CYAN,
        Color.LTGRAY,
        0xff2a4480
    };
    
    public ReportData(String l, Integer v) {
      this.location = l;
      this.value = v;
      
    }
  }
  
  protected Map<String, String> getRangeMapForString(String rangeStr) {
    Map<String, String> theMap = new HashMap<String, String>();
    if (rangeStr == null) {
      return null;
    }
    
    if (rangeStr.equals("This week")) {
      Calendar cal = Calendar.getInstance();
      cal.setFirstDayOfWeek(Calendar.MONDAY);
      cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      theMap.put("startTime", String.valueOf(cal.getTime().getTime() / 1000));
      Log.e("", cal.getTime().toString());
      cal.add(Calendar.DAY_OF_WEEK, 7);
      theMap.put("endTime", String.valueOf(cal.getTime().getTime() / 1000));
      Log.e("", cal.getTime().toString());
      return theMap;
    } else if (rangeStr.equals("This month")) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      theMap.put("startTime", String.valueOf(cal.getTime().getTime() / 1000));
      Log.e("", cal.getTime().toString());
      cal.add(Calendar.MONTH, 1);
      Log.e("", cal.getTime().toString());
      theMap.put("endTime", String.valueOf(cal.getTime().getTime() / 1000));
      return theMap;
    } else if (rangeStr.equals("This year")) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_YEAR, 1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      Log.e("", cal.getTime().toString());
      theMap.put("startTime", String.valueOf(cal.getTime().getTime() / 1000));
      cal.add(Calendar.YEAR, 1);
      theMap.put("endTime", String.valueOf(cal.getTime().getTime() / 1000));
      Log.e("", cal.getTime().toString());
      return theMap;
    } else { //all time
      return null;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Map<String, String> rangeMap = null;
    Intent thisIntent = getIntent();
    Bundle extras = thisIntent.getExtras();
    if (extras != null && extras.containsKey("range")) {
      this.setTitle(this.getTitle() + " - " + extras.getCharSequence("range"));
      rangeMap = getRangeMapForString((String) extras.get("range"));
    }
    
    
    Display disp = getWindowManager().getDefaultDisplay();
    int width;
    int height;
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
      Point point = new Point();
      disp.getSize(point);
      width = point.x;
      height = point.y;
    } else {
      width = disp.getWidth();
      height = disp.getHeight();
    }
    BalanceDbHelper dbHelper = new BalanceDbHelper(this);
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = null;
    String query = null;
    if (rangeMap == null) {
      query = "SELECT " + BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION + ", SUM(" + BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT + ") total_amount FROM " + BalanceContract.BalanceEntry.TABLE_NAME + " " +
          "WHERE " + BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT + " < 0 GROUP BY " + BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION + " ORDER BY total_amount";
    } else {
      query = "SELECT " + BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION + ", SUM(" + BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT + ") total_amount FROM " + BalanceContract.BalanceEntry.TABLE_NAME + " " +
          "WHERE " + BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT + " < 0 AND " + BalanceContract.BalanceEntry.COLUMN_NAME_DATE + " >= " + rangeMap.get("startTime") + " AND " +
          BalanceContract.BalanceEntry.COLUMN_NAME_DATE + " < " + rangeMap.get("endTime") +
          " GROUP BY " + BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION + " ORDER BY total_amount";
    }
    Log.e("", "report query: " + query);
    
    c = db.rawQuery(query, null);
    
    LinearLayout layout = new LinearLayout(this);
    ScrollView scrollView = new ScrollView(this);
    scrollView.addView(layout);
    layout.setBackgroundColor(0xff000000);
    int i = 0;
    if (c.moveToFirst()) {
      ArrayList<ReportData> data = new ArrayList<ReportData>();
      Long total = 0L;
      Long otherAmount = 0L;
      do {
        if (i + 1 < 10) {
          String location = c.getString(c.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION));
          Long amount = c.getLong(c.getColumnIndex("total_amount")) * -1; //Flip sign
          ReportData d = new ReportData(location, amount.intValue());
          d.color = d.colorSet[i++];
          data.add(d);
        } else {
          Long amount = c.getLong(c.getColumnIndex("total_amount")) * -1;
          otherAmount += amount;
        }
      } while (c.moveToNext());
      
      if (otherAmount > 0) {
        ReportData d = new ReportData("Other", otherAmount.intValue());
        d.color = d.colorSet[i];
        data.add(d);
      }
      
      ArrayList<Integer> values = new ArrayList<Integer>();
      ArrayList<Integer> colors = new ArrayList<Integer>();
      

      
      for(i = 0; i < data.size(); i++) {
        total += data.get(i).value;
        values.add(data.get(i).value);
        colors.add(data.get(i).color);
      }


      
      NumberFormat fmt = NumberFormat.getCurrencyInstance();
      LinearLayout totalLayout = new LinearLayout(this);
      totalLayout.setOrientation(LinearLayout.VERTICAL);
      TextView tvTotalLbl = new TextView(this);
      TextView tvTotal = new TextView(this);
      tvTotalLbl.setTextColor(0xffffffff);
      tvTotalLbl.setTextSize(24);
      tvTotalLbl.setPadding(5, 5, 5, 5);
      tvTotalLbl.setText("Total");
      tvTotal.setTextColor(0xffffffff);
      tvTotal.setPadding(5, 5, 5, 5);
      tvTotal.setText(fmt.format((double) total / 100));
      totalLayout.addView(tvTotalLbl);
      totalLayout.addView(tvTotal);
      CustomGraphView graph = new CustomGraphView(this, width, height, 300);
      graph.setPadding(0, 5, 0, 5);
      graph.setValues(values);
      graph.setColors(colors);
      graph.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.addView(totalLayout);
      layout.addView(graph);
      for(i = 0; i < data.size(); i++) {
        LinearLayout tmpLayoutParent = new LinearLayout(this);
        LinearLayout tmpLayout = new LinearLayout(this);
        tmpLayoutParent.setOrientation(LinearLayout.HORIZONTAL);
        tmpLayout.setPadding(5, 5, 5, 5);

        tmpLayout.setOrientation(LinearLayout.VERTICAL);
        TextView tvLocation = new TextView(this);
        TextView tvAmount = new TextView(this);
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new LayoutParams(40, LayoutParams.MATCH_PARENT));
        iv.setPadding(10, 10, 10, 10);
        ColorDrawable cd = new ColorDrawable(data.get(i).color);
        iv.setImageDrawable(cd);
        tmpLayoutParent.addView(iv);
        tmpLayoutParent.addView(tmpLayout);
        tvLocation.setTextSize(24);
        tvLocation.setTextColor(0xffffffff);
        tvLocation.setText(data.get(i).location);
        tvAmount.setTextColor(0xffffffff);
        tvAmount.setText(fmt.format((double) data.get(i).value / 100));
        tmpLayout.addView(tvLocation);
        tmpLayout.addView(tvAmount);
        layout.addView(tmpLayoutParent);
      }
      
    } else {
      TextView tv = new TextView(this);
      tv.setTextColor(0xffffffff);
      tv.setText("No data yet");
      layout.addView(tv);
    }
    db.close();
    

    setContentView(scrollView);
    // Show the Up button in the action bar.
    setupActionBar();
  }

  /**
   * Set up the {@link android.app.ActionBar}, if the API is available.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void setupActionBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.spending_report, menu);
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
