package com.obfuskate.accountbalance;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

public class CustomGraphView extends View {

  private Paint p;
  private int startX;
  private int startY;
  private int radius;
  private ArrayList<Integer> colors;
  private ArrayList<Integer> values;
  private RectF rectF;
  
  public CustomGraphView(Context context) {
    this(context, 100, 100, 10);
  }

  public CustomGraphView(Context context, int parentWidth, int parentHeight, int r) {
    super(context);
    Log.e("", "X: " + parentWidth + "Y: " + parentHeight);
    rectF = new RectF();
    p = new Paint();
    p.setColor(Color.BLUE);
    p.setAntiAlias(true);

    colors = new ArrayList<Integer>();
    values = new ArrayList<Integer>();
    

    startX = parentWidth / 2 - r / 2;
    startY = 0;
    radius =  r;

    colors.add(Color.GREEN);
    colors.add(Color.CYAN);
    colors.add(Color.MAGENTA);
    colors.add(Color.BLUE);
    colors.add(Color.RED);

    values.add(0);
    values.add(1);
    values.add(3);
    values.add(0);
    values.add(2);

  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

      int desiredWidth = 2000;
      int desiredHeight = radius;

      int widthMode = MeasureSpec.getMode(widthMeasureSpec);
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);
      int heightMode = MeasureSpec.getMode(heightMeasureSpec);
      int heightSize = MeasureSpec.getSize(heightMeasureSpec);

      int width;
      int height;

      //Measure Width
      if (widthMode == MeasureSpec.EXACTLY) {
          //Must be this size
          width = widthSize;
      } else if (widthMode == MeasureSpec.AT_MOST) {
          //Can't be bigger than...
          width = Math.min(desiredWidth, widthSize);
      } else {
          //Be whatever you want
          width = desiredWidth;
      }

      //Measure Height
      if (heightMode == MeasureSpec.EXACTLY) {
          //Must be this size
          height = heightSize;
      } else if (heightMode == MeasureSpec.AT_MOST) {
          //Can't be bigger than...
          height = Math.min(desiredHeight, heightSize);
      } else {
          //Be whatever you want
          height = desiredHeight;
      }

      //MUST CALL THIS
      setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Log.e("", "onDraw() is called...");

    float offset = 0;
    float sum = 0;
    for (int a = 0; a < values.size(); a++) {
      sum += values.get(a);
    }

    float angle = (float) (360 / sum);

    Log.e("angle", "" + angle);

    rectF.set(getStartX(), getStartY(), getStartX() + getRadius(), getStartY()
        + getRadius());

    for (int i = 0; i < values.size(); i++) {

      p.setColor(colors.get(i));

      if (i == 0) {
        canvas.drawArc(rectF, 0, values.get(i) * angle, true, p);
      } else {
        canvas.drawArc(rectF, offset, values.get(i) * angle, true, p);
      }

      offset += (values.get(i) * angle);
    }

    canvas.save();
  }

  public int getStartX() {
    return startX;
  }

  public void setStartX(int startX) {
    this.startX = startX;
  }

  public int getStartY() {
    return startY;
  }

  public void setStartY(int startY) {
    this.startY = startY;
  }

  public int getRadius() {
    return radius;
  }

  public void setRadius(int radius) {
    this.radius = radius;
  }

  public ArrayList<Integer> getColors() {
    return colors;
  }

  public void setColors(ArrayList<Integer> colors) {
    this.colors = colors;
  }

  public ArrayList<Integer> getValues() {
    return values;
  }

  public void setValues(ArrayList<Integer> values) {
    this.values = values;
  }
}