package com.obfuskate.accountbalance;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

public class ReportActivity extends Activity {
  
  private Spinner spinnerReport;
  private Spinner spinnerRange;

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_report);
    spinnerReport = (Spinner) findViewById(R.id.spinnerSelectReport);
    spinnerRange = (Spinner) findViewById(R.id.spinnerSelectRange);
    Button goBtn = (Button) findViewById(R.id.buttonGo);
    //setup report drop down
    List<Map<String, String>> reportList = new ArrayList<Map<String, String>>();
    Map<String, String> tmpMap = new HashMap<String, String>();
    tmpMap.put("name", "Spending Report");
    reportList.add(tmpMap);
    SimpleAdapter reportAdapter = new SimpleAdapter(this, reportList, android.R.layout.simple_list_item_1, new String[] {"name"}, new int[] {android.R.id.text1});
    spinnerReport.setAdapter(reportAdapter);
    
    //setup range drop down
    List<Map<String, String>> rangeList = new ArrayList<Map<String, String>>();
    tmpMap = new HashMap<String, String>();
    tmpMap.put("name", "This week");
    rangeList.add(tmpMap);
    tmpMap = new HashMap<String, String>();
    tmpMap.put("name", "This month");
    rangeList.add(tmpMap);
    tmpMap = new HashMap<String, String>();
    tmpMap.put("name", "This year");
    rangeList.add(tmpMap);
    tmpMap = new HashMap<String, String>();
    tmpMap.put("name", "All time");
    rangeList.add(tmpMap);
    SimpleAdapter rangeAdapter = new SimpleAdapter(this, rangeList, android.R.layout.simple_list_item_1, new String[] {"name"}, new int[] {android.R.id.text1});
    spinnerRange.setAdapter(rangeAdapter);
    goBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        String reportType = (String) ((Map<String, String>) spinnerReport.getSelectedItem()).get("name");
        String reportRange = (String) ((Map<String, String>) spinnerRange.getSelectedItem()).get("name");
        if (reportType.equals("Spending Report")) {
          Intent spendingReportIntent = new Intent(v.getContext(), SpendingReportActivity.class);
          spendingReportIntent.putExtra("range", reportRange);
          startActivity(spendingReportIntent);
        }
      }
      
    });
    
    
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
