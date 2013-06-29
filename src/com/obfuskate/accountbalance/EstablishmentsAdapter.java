package com.obfuskate.accountbalance;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    String[] hexArray = {
        "00","01","02","03","04","05","06","07","08","09","0A","0B","0C","0D","0E","0F",
        "10","11","12","13","14","15","16","17","18","19","1A","1B","1C","1D","1E","1F",
        "20","21","22","23","24","25","26","27","28","29","2A","2B","2C","2D","2E","2F",
        "30","31","32","33","34","35","36","37","38","39","3A","3B","3C","3D","3E","3F",
        "40","41","42","43","44","45","46","47","48","49","4A","4B","4C","4D","4E","4F",
        "50","51","52","53","54","55","56","57","58","59","5A","5B","5C","5D","5E","5F",
        "60","61","62","63","64","65","66","67","68","69","6A","6B","6C","6D","6E","6F",
        "70","71","72","73","74","75","76","77","78","79","7A","7B","7C","7D","7E","7F",
        "80","81","82","83","84","85","86","87","88","89","8A","8B","8C","8D","8E","8F",
        "90","91","92","93","94","95","96","97","98","99","9A","9B","9C","9D","9E","9F",
        "A0","A1","A2","A3","A4","A5","A6","A7","A8","A9","AA","AB","AC","AD","AE","AF",
        "B0","B1","B2","B3","B4","B5","B6","B7","B8","B9","BA","BB","BC","BD","BE","BF",
        "C0","C1","C2","C3","C4","C5","C6","C7","C8","C9","CA","CB","CC","CD","CE","CF",
        "D0","D1","D2","D3","D4","D5","D6","D7","D8","D9","DA","DB","DC","DD","DE","DF",
        "E0","E1","E2","E3","E4","E5","E6","E7","E8","E9","EA","EB","EC","ED","EE","EF",
        "F0","F1","F2","F3","F4","F5","F6","F7","F8","F9","FA","FB","FC","FD","FE","FF"};

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
      File cacheDir = context.getCacheDir();
      MessageDigest md = null;
      String iconHash;
      StringBuffer hexString = new StringBuffer();
      byte[] iconHashBytes;
      
      try {
        md = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException nsae) {
        Log.e("", "No Such algorithm: MD5");
        return null;
      }
      
      md.update(data.getIconUrl().getBytes());
      
      iconHashBytes = md.digest();
      for (int i = 0; i < iconHashBytes.length; i++) {
        hexString.append(hexArray[iconHashBytes[i] & 0x000000ff]);
      }
      iconHash = hexString.toString();
      
      try {
        FileInputStream fis = new FileInputStream(cacheDir.getPath() + "/" + iconHash);
        Bitmap newBitmap = BitmapFactory.decodeStream(fis);
        fis.close();
        bitmaps.put(data.getIconUrl(), newBitmap);
        return loader;
      } catch (FileNotFoundException fnfe) {
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
            FileOutputStream fos = new FileOutputStream(context.getCacheDir().getPath() + "/" + iconHash);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            synchronized (bitmaps) {
              bitmaps.put(data.getIconUrl(), newBitmap);
            }
          } catch(Exception e) {
            Log.e("Image Download", "Exception thrown during image download: " + e.getMessage());
          }
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
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
