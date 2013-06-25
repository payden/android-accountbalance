package com.obfuskate.accountbalance;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;


public class DepositActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
  public Button depositButton;
  public EditText amountText;
  public EditText memoText;
  LocationClient mLocationClient;
  Location mCurrentLocation;
  LocationRequest mLocationRequest;
  private final static int UPDATE_INTERVAL = 5000;
  private final static int UPDATE_INTERVAL_FASTEST = 1000;
  private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
  private boolean bSwitched;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_deposit_waiting);
    mLocationClient = new LocationClient(this, this, this);
    mLocationRequest = LocationRequest.create();
    mLocationRequest.setInterval(UPDATE_INTERVAL);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    mLocationRequest.setFastestInterval(UPDATE_INTERVAL_FASTEST);
    mLocationClient.connect();
    // Show the Up button in the action bar.
    setupActionBar();
  }
  
  protected void onStop() {
    if (mLocationClient.isConnected()) {
      mLocationClient.disconnect();
    }
    super.onStop();
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
    getMenuInflater().inflate(R.menu.deposit, menu);
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

  public static class ErrorDialogFragment extends DialogFragment {
    private Dialog mDialog;
    
    public ErrorDialogFragment() {
      super();
      mDialog = null;
    }
    
    public void setDialog(Dialog d) {
      mDialog = d;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return mDialog;
    }
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case CONNECTION_FAILURE_RESOLUTION_REQUEST:
      switch (resultCode) {
      case Activity.RESULT_OK:
        //try again
        break;
      }
    }
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    if (connectionResult.hasResolution()) {
      try {
        connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
      } catch (IntentSender.SendIntentException e) {
        e.printStackTrace();
      }
    } else {
      Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
      if (errorDialog != null) {
        ErrorDialogFragment errorFragment = new ErrorDialogFragment();
        errorFragment.setDialog(errorDialog);
        errorFragment.show(getSupportFragmentManager(), "Location Tools");
      }
    }
    
  }

  @Override
  public void onConnected(Bundle arg0) {
    mLocationClient.requestLocationUpdates(mLocationRequest, this);
  }

  @Override
  public void onDisconnected() {
    mLocationClient.removeLocationUpdates(this);
  }

  @Override
  public void onLocationChanged(Location location) {
    mCurrentLocation = location;
    
    if (!bSwitched) {
      setContentView(R.layout.activity_deposit);
      depositButton = (Button) findViewById(R.id.buttonRecordDeposit);
      amountText = (EditText) findViewById(R.id.editTextDepositAmount);
      memoText = (EditText) findViewById(R.id.editTextDepositMemo);
      depositButton.setOnClickListener(new OnClickListener() {
  
        @Override
        public void onClick(View v) {
          DepositActivity dActivity = (DepositActivity) v.getContext();
          
          if (dActivity.amountText == null || dActivity.memoText == null) {
            Toast.makeText(v.getContext(), "This just happened", Toast.LENGTH_SHORT).show();
            return;
          }
          
          String amount = dActivity.amountText.getText().toString();
          String memo = dActivity.memoText.getText().toString();
          
          if (amount == null || amount.length() == 0) {
            Toast.makeText(v.getContext(), "Must enter amount", Toast.LENGTH_LONG).show();
            return;
          }
          
          if (memo == null || memo.length() == 0) {
            Toast.makeText(v.getContext(), "Must enter memo", Toast.LENGTH_LONG).show();
            return;
          }
          
          
          int lastIndex = amount.length() - 1;
          if (amount.indexOf('.') != -1 && lastIndex - amount.indexOf('.') > 2) {
            Toast.makeText(v.getContext(), "Please only use 2 decimal places", Toast.LENGTH_LONG).show();
            return;
          }
          
          String amountStr = String.valueOf(Float.valueOf(Float.parseFloat(amount) * 100).intValue());
          
          BalanceDbHelper dbHelper = new BalanceDbHelper(v.getContext());
          SQLiteDatabase db = dbHelper.getWritableDatabase();
          ContentValues values = new ContentValues();
          values.put(BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION, memo);
          values.put(BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT, amountStr);
          values.put(BalanceContract.BalanceEntry.COLUMN_NAME_DATE, System.currentTimeMillis() / 1000);
          values.put(BalanceContract.BalanceEntry.COLUMN_NAME_LAT, String.valueOf(mCurrentLocation.getLatitude()));
          values.put(BalanceContract.BalanceEntry.COLUMN_NAME_LONG, String.valueOf(mCurrentLocation.getLongitude()));
          db.insertOrThrow(BalanceContract.BalanceEntry.TABLE_NAME, null, values);
          Intent mainIntent = new Intent(v.getContext(), MainActivity.class);
          startActivity(mainIntent);
        }
        
      });
      bSwitched = true;
    }
  }

}
