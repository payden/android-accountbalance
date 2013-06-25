package com.obfuskate.accountbalance;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;



public class PurchaseActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
  LocationClient mLocationClient;
  Location mCurrentLocation;
  LocationRequest mLocationRequest;
  List<Map<String, String>> arrayList;
  EstablishmentsAdapter mAdapter;
  private static AsyncTask<String, Void, JSONObject> loadMoreTask;
  private static AsyncTask<String, Void, JSONObject> findEstablishmentsTask;
  private final static String GENERIC_BUSINESS_ICON = "http://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png";
  private final static String LOG_TAG = PurchaseActivity.class.getCanonicalName();
  private final static int UPDATE_INTERVAL = 5000;
  private final static int UPDATE_INTERVAL_FASTEST = 1000;
  private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
  private final static String PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?rankby=distance&types=store%7Cfood%7Cestablishment&sensor=true&key=AIzaSyDFBRo_1r5mwd9p8BOfnTVoE4f1FzC8kNE&location=";
  
  
  private class LoadMoreTask extends AsyncTask<String, Void, JSONObject> {

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
      if (jsonObject == null) {
        return;
      }
      JSONArray results = null;
      String nextPageToken = "";
      try {
        if (jsonObject.has("next_page_token")) {
          nextPageToken = jsonObject.getString("next_page_token");
        }
        results = jsonObject.getJSONArray("results");
        if (results == null) {
          return;
        }
        for (int i = 0; i < results.length(); i++) {
          Map<String, String> map = new HashMap<String, String>();
          JSONObject jsonObjectPlace = results.getJSONObject(i);
          map.put("name", jsonObjectPlace.getString("name"));
          map.put("address", jsonObjectPlace.getString("vicinity"));
          map.put("icon", jsonObjectPlace.getString("icon"));
          map.put("id", jsonObjectPlace.getString("id"));
          arrayList.add(arrayList.size() - 1, map);
        }
      } catch(JSONException e) {
        Log.e("JSON", "JSON Exception in LoadMoreTask: " + e.getMessage());
      }
      mAdapter.notifyDataSetChanged();
      if (nextPageToken != null && !nextPageToken.equals("") && !loadMoreTask.isCancelled()) {
        loadMoreTask = new LoadMoreTask();
        loadMoreTask.execute(nextPageToken);
      }
    }
    
    @Override
    protected JSONObject doInBackground(String... pageToken) {
      InputStream is = null;
      String result = "";
      JSONObject jsonObject = null;
      if (pageToken[0] == null || pageToken[0].equals("")) {
        Log.e("AsyncTasks", "pageToken was null or empty in LoadMoreTask");
        return null;
      }
      ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = connManager.getActiveNetworkInfo();
      if (netInfo == null || !netInfo.isConnected()) {
        return null;
      }
      
      try {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(PLACES_URL + "&pagetoken=" + pageToken[0]);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        is = httpEntity.getContent();
      } catch (Exception e) {
        e.printStackTrace();
        Log.e(LOG_TAG, "Exception in doInBackground for findEstablishmentsTask: " + e.getMessage());
      }
      
      try {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
          stringBuilder.append(line);
        }
        is.close();
        result = stringBuilder.toString();
      } catch (Exception e) {
        Log.e(LOG_TAG, "Unable to build string using bufferedreader");
      }
      
      try {
        jsonObject = new JSONObject(result);
      } catch (Exception e) {
        Log.e(LOG_TAG, "Unable to convert to JSON array");
      }
      
      
      return jsonObject;
    }
    
  }
  
  private class FindEstablishmentsTask extends AsyncTask<String, Void, JSONObject> {
    
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
      String nextPageToken = null;
      JSONArray jsonArray = new JSONArray();
      try {
        if (jsonObject.has("next_page_token")) {
          nextPageToken = jsonObject.getString("next_page_token");
        }
        jsonArray = jsonObject.getJSONArray("results");
      } catch(JSONException e) {
        Log.e("JSON", "JSON Exception in FindEstablishmentsTask: " + e.getMessage());
      }
      ProgressBar loading = (ProgressBar) findViewById(R.id.progressBarSearching);
      loading.setVisibility(android.view.View.GONE);
      Button recordButton = (Button) findViewById(R.id.buttonRecordPurchase);
      recordButton.setVisibility(View.VISIBLE);
      
      
      if (jsonArray == null) {
        Toast.makeText(PurchaseActivity.this, "Unable to get location results", Toast.LENGTH_SHORT).show();
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        EditText otherText = (EditText) findViewById(R.id.editTextOther);
        otherText.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.INVISIBLE);
        
        return;
      }
      int i;
      arrayList = new ArrayList<Map<String, String>>();
      for(i = 0; i < jsonArray.length(); i++) {
        try {
          Map<String, String> map = new HashMap<String, String>();
          JSONObject jsonObjectPlace = jsonArray.getJSONObject(i);
          map.put("name", jsonObjectPlace.getString("name"));
          map.put("address", jsonObjectPlace.getString("vicinity"));
          map.put("icon", jsonObjectPlace.getString("icon"));
          map.put("id", jsonObjectPlace.getString("id"));
          arrayList.add(map);
        } catch(Exception e) {
          
        }
      }
      Map<String, String> map = new HashMap<String, String>();
      map.put("name", "Other");
      map.put("address", "Can't find store in list");
      map.put("icon", GENERIC_BUSINESS_ICON);
      map.put("id", "other");
      arrayList.add(map);
      Spinner spinner = (Spinner) findViewById(R.id.spinner1);
      EstablishmentsAdapter adapter = new EstablishmentsAdapter(PurchaseActivity.this, R.layout.simple_list_item_2_icon, arrayList);
      mAdapter = adapter;
      spinner.setAdapter(adapter);
      spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> av, View v, int position, long arg3) {
          String selectedName = ((Map<String, String>)arrayList.get(position)).get("name");
          if (selectedName.equals("Other")) {
            av.setVisibility(View.INVISIBLE);
            EditText otherText = (EditText) findViewById(R.id.editTextOther);
            otherText.setVisibility(View.VISIBLE);
            otherText.requestFocus();
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
          // TODO Auto-generated method stub
          
        }
        
      });
      
      if (nextPageToken != null && !nextPageToken.equals("") && !findEstablishmentsTask.isCancelled()) {
        //have nextPageToken, execute LoadMoreTask
        loadMoreTask = new LoadMoreTask();
        loadMoreTask.execute(nextPageToken);
      }
    }

    @Override
    protected JSONObject doInBackground(String... args) {
      InputStream is = null;
      String result = "";
      JSONObject jsonObject = null;
      
      if (args.length < 2) {
        throw new RuntimeException("Not enough arguments for location, contact developer");
      }
      
      ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = connManager.getActiveNetworkInfo();
      if (netInfo == null || !netInfo.isConnected()) {
        return null;
      }
      
      try {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(PLACES_URL + args[0] + "," + args[1]);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        is = httpEntity.getContent();
      } catch (Exception e) {
        e.printStackTrace();
        Log.e(LOG_TAG, "Exception in doInBackground for findEstablishmentsTask: " + e.getMessage());
      }
      
      try {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
          stringBuilder.append(line);
        }
        is.close();
        result = stringBuilder.toString();
      } catch (Exception e) {
        Log.e(LOG_TAG, "Unable to build string using bufferedreader");
      }
      
      try {
        jsonObject = new JSONObject(result);
      } catch (Exception e) {
        Log.e(LOG_TAG, "Unable to convert to JSON array");
      }
      
      
      return jsonObject;
    }
    
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_purchase);
    loadMoreTask = null;
    findEstablishmentsTask = null;
    mLocationClient = new LocationClient(this, this, this);
    mLocationRequest = LocationRequest.create();
    mLocationRequest.setInterval(UPDATE_INTERVAL);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    mLocationRequest.setFastestInterval(UPDATE_INTERVAL_FASTEST);
    
    Button recordButton = (Button) findViewById(R.id.buttonRecordPurchase);
    recordButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        BalanceDbHelper dbHelper = new BalanceDbHelper(v.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        EditText otherText = (EditText) findViewById(R.id.editTextOther);
        EditText amountText = (EditText) findViewById(R.id.editTextAmount);
        Spinner locationSpinner = (Spinner) findViewById(R.id.spinner1);
        String location = "";
        
        if (amountText.getText().toString().equals("")) {
          Toast.makeText(PurchaseActivity.this, "Amount can't be blank", Toast.LENGTH_SHORT).show();
          return;
        }
        
        if (locationSpinner.getVisibility() == View.INVISIBLE) {
          location = otherText.getText().toString();
        } else {
          location = arrayList.get(locationSpinner.getSelectedItemPosition()).get("name").toString();
        }
        if (location.equals("")) {
          Toast.makeText(PurchaseActivity.this, "Location can't be blank", Toast.LENGTH_SHORT).show();
          return;
        }
        values.put(BalanceContract.BalanceEntry.COLUMN_NAME_LOCATION, location);
        values.put(BalanceContract.BalanceEntry.COLUMN_NAME_AMOUNT, Double.parseDouble(amountText.getText().toString()) * -100); //Make negative for purchase and store as integer
        values.put(BalanceContract.BalanceEntry.COLUMN_NAME_DATE, System.currentTimeMillis() / 1000);
        values.put(BalanceContract.BalanceEntry.COLUMN_NAME_LAT, String.valueOf(mCurrentLocation.getLatitude()));
        values.put(BalanceContract.BalanceEntry.COLUMN_NAME_LONG, String.valueOf(mCurrentLocation.getLongitude()));
        db.insertOrThrow(BalanceContract.BalanceEntry.TABLE_NAME, null, values);
        Intent mainIntent = new Intent(v.getContext(), MainActivity.class);
        startActivity(mainIntent);
      }
      
    });
    // Show the Up button in the action bar.
    setupActionBar();
  }
  
  @Override
  protected void onStart() {
    super.onStart();
    mLocationClient.connect();
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    if (findEstablishmentsTask != null && !findEstablishmentsTask.isCancelled()) {
      findEstablishmentsTask.cancel(true);
    }
    if (loadMoreTask != null && !loadMoreTask.isCancelled()) {
      loadMoreTask.cancel(true);
    }
  }
  
  @Override
  protected void onStop() {
    mLocationClient.disconnect();   
    super.onStop();
  }

  /**
   * Set up the {@link android.app.ActionBar}.
   */
  @SuppressLint("NewApi")
  private void setupActionBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.purchase, menu);
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
    //We just want to get one good location update
    if (mLocationClient.isConnected()) {
      mLocationClient.removeLocationUpdates(this);
    }
    Log.e("Testing", "Lat: " + String.valueOf(location.getLatitude()) + ", Long: " + String.valueOf(location.getLongitude()));
    List<Map<String, String>> arrayList = new ArrayList<Map<String, String>>();
    Map<String, String> map = new HashMap<String, String>();
    map.put("name", "Searching nearby locations");
    arrayList.add(map);
    SimpleAdapter waitingAdapter = new SimpleAdapter(PurchaseActivity.this, arrayList, android.R.layout.simple_list_item_1, new String[] {"name"}, new int[] {android.R.id.text1});
    Spinner spinner = (Spinner) findViewById(R.id.spinner1);
    spinner.setAdapter(waitingAdapter);
    findEstablishmentsTask = new FindEstablishmentsTask();
    findEstablishmentsTask.execute(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
  }

}
