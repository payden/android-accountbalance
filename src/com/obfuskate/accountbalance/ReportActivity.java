package com.obfuskate.accountbalance;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ReportActivity extends Activity {
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

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
    Cursor c = db.rawQuery("SELECT " + BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION + ", SUM(" + BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT + ") total_amount FROM " + BalanceContract.BalanceEntry.TABLE_NAME + " " +
    		"WHERE " + BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT + " < 0 GROUP BY " + BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION + " ORDER BY total_amount LIMIT 10", null);
    
    
    LinearLayout layout = new LinearLayout(this);
    ScrollView scrollView = new ScrollView(this);
    scrollView.addView(layout);
    layout.setBackgroundColor(0xff000000);
    int i = 0;
    if (c.moveToFirst()) {
      ArrayList<ReportData> data = new ArrayList<ReportData>();
      do {
        String location = c.getString(c.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION));
        Long amount = c.getLong(c.getColumnIndex("total_amount")) * -1; //Flip sign
        ReportData d = new ReportData(location, amount.intValue());
        d.color = d.colorSet[i++];
        data.add(d);
      } while (c.moveToNext());
      ArrayList<Integer> values = new ArrayList<Integer>();
      ArrayList<Integer> colors = new ArrayList<Integer>();
      Integer total = 0;
      for(i = 0; i < data.size(); i++) {
        total += data.get(i).value;
        values.add(data.get(i).value);
        colors.add(data.get(i).color);
      }
      
      NumberFormat fmt = NumberFormat.getCurrencyInstance();
      
      TextView tvTotal = new TextView(this);
      tvTotal.setTextColor(0xffffffff);
      tvTotal.setTextSize(24);
      tvTotal.setPadding(5, 5, 5, 5);
      tvTotal.setText("Total: " + fmt.format((double) total / 100));
      CustomGraphView graph = new CustomGraphView(this, width, height, 300);
      graph.setValues(values);
      graph.setColors(colors);
      graph.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.addView(tvTotal);
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
    getMenuInflater().inflate(R.menu.report, menu);
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
