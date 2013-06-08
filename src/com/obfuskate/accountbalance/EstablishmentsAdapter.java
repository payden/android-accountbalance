package com.obfuskate.accountbalance;


import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class EstablishmentsAdapter extends ArrayAdapter<Map<String, String>> {

  Context context;
  int layoutResourceId;
  List<Map<String, String>> establishments;
  Map<String, Boolean> downloading;
  Map<String, Bitmap> bitmaps;
  
  private class DownloadTaskData {
    private String iconUrl;
    private ViewHolder holder;
    private long startTime;
    
    public DownloadTaskData(String iconUrl, ViewHolder holder) {
      this.iconUrl = iconUrl;
      this.holder = holder;
      this.startTime = System.currentTimeMillis();
    }
    
    public long getStartTime() {
      return this.startTime;
    }
    
    public String getIconUrl() {
      return this.iconUrl;
    }
    
    public ViewHolder getHolder() {
      return this.holder;
    }
  }
  
  private class ImageViewLoader {
    private String iconUrl;
    private ViewHolder holder;
    private long startTime;
    
    public ImageViewLoader(String iconUrl, ViewHolder holder, long startTime) {
      this.iconUrl = iconUrl;
      this.holder = holder;
      this.startTime = startTime;
    }
    
    public ViewHolder getHolder() {
      return this.holder;
    }
    
    public void load() {
      Bitmap toLoad = null;
      if (this.holder.reuseTime > this.startTime) {
        Log.e("timer", "Reusetime after task start time, not loading image.");
        return;
      }
      synchronized (bitmaps) {
        if (!bitmaps.containsKey(iconUrl)) {
          Log.e("ImageViewLoader", "load called and bitmap not ready");
          return;
        }
        toLoad = bitmaps.get(this.iconUrl);
      }
      this.holder.imageView.setImageBitmap(toLoad);
    }
    
  }
  
  private class DownloadImageTask extends AsyncTask<DownloadTaskData, Void, ImageViewLoader> {

    @Override
    protected void onPostExecute(ImageViewLoader loader) {
      if (loader != null) {
        loader.load();
      }
    }
    
    @Override
    protected ImageViewLoader doInBackground(DownloadTaskData... args) {
      DownloadTaskData data = args[0];
      ImageViewLoader loader = new ImageViewLoader(data.getIconUrl(), data.getHolder(), data.getStartTime());
      if (downloading.containsKey(data.getIconUrl())) {
        int limitTries = 2;
        Boolean haveBitmap = false;
        synchronized (bitmaps) {
          haveBitmap = bitmaps.containsKey(data.getIconUrl());
        }
        while (!haveBitmap && limitTries-- > 0) {
          try {
            Log.d("doInBackground", "File doesn't exist.. sleeping 2");
            Thread.sleep(2);
            Log.d("Sleep", "Slept 2 seconds");
          } catch(InterruptedException e) {
            Log.e("Sleep", "Sleep interrupted in DownloadImageTask");
          }
        }
        return haveBitmap ? loader : null;
      } else {
        try {
          downloading.put(data.getIconUrl(), true);
          URL imageUrl = new URL(data.getIconUrl());
          URLConnection urlConnection = imageUrl.openConnection();
          urlConnection.connect();
          Bitmap newBitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
          synchronized (bitmaps) {
            bitmaps.put(data.getIconUrl(), newBitmap);
          }
        } catch(Exception e) {
          Log.e("Image Download", "Exception thrown during image download: " + e.getMessage());
        }
      }
      return loader;
    }
    
  }
  
  public class ViewHolder {
    public ImageView imageView;
    public TextView textViewBig;
    public TextView textViewSmall;
    public long reuseTime = 0;
  }
  
  public EstablishmentsAdapter(Context context, int textViewResourceId, List<Map<String, String>> objects) {
    super(context, textViewResourceId, objects);
    this.context = context;
    this.layoutResourceId = textViewResourceId;
    this.establishments = objects;
    this.downloading = new HashMap<String, Boolean>();
    this.bitmaps = new HashMap<String, Bitmap>();
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return getCustomView(position, convertView, parent, false);
  }
  
  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    return getCustomView(position, convertView, parent, true);
  }
  
  public View getCustomView(int position, View convertView, ViewGroup parent, Boolean dropDown) {
    View row = convertView;
    ViewHolder holder = null;
    /* without view reuse
    LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    row = li.inflate(R.layout.simple_list_item_2_icon, parent, false);
    holder = new ViewHolder();
    holder.imageView = (ImageView) row.findViewById(R.id.imageView1);
    holder.textViewBig = (TextView) row.findViewById(R.id.text1);
    holder.textViewSmall = (TextView) row.findViewById(R.id.text2);
    */

    if (row == null) {
      LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      row = li.inflate(R.layout.simple_list_item_2_icon, parent, false);
      holder = new ViewHolder();
      holder.imageView = (ImageView) row.findViewById(R.id.imageView1);
      holder.textViewBig = (TextView) row.findViewById(R.id.text1);
      holder.textViewSmall = (TextView) row.findViewById(R.id.text2);
      row.setTag(holder);
    } else {
      holder = (ViewHolder) row.getTag();
      holder.reuseTime = System.currentTimeMillis();
    }


    Map<String, String> establishment = establishments.get(position);
    if (establishment != null) {
      holder.textViewBig.setText(establishment.get("name"));
      holder.textViewSmall.setText(establishment.get("address"));
      holder.imageView.setTag((String)establishment.get("name"));

      String iconUrlUnique = establishment.get("icon");
      Boolean alreadyHaveBitmap = null;
      synchronized (bitmaps) {
        alreadyHaveBitmap = bitmaps.containsKey(iconUrlUnique);
      }
      if (!alreadyHaveBitmap) {
        DownloadTaskData d = new DownloadTaskData(iconUrlUnique, holder);
        (new DownloadImageTask()).execute(d);
      } else {
        (new ImageViewLoader(iconUrlUnique, holder, holder.reuseTime)).load();
      }
    }
    return row;
  }

}
