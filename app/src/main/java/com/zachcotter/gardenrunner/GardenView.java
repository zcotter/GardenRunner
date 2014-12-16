package com.zachcotter.gardenrunner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GardenView extends View implements OnTouchListener {


  private static final short VEGETABLES_PER_ROW = 10;
  private static final short VEGETABLES_PER_SCREEN_HEIGHT = 10;
  private int width_pixels_per_vegetable;
  private int height_pixels_per_vegetable;

  public Garden garden;

  public GardenView(Context context,
                    AttributeSet attrs) {
    super(context,
          attrs);

    this.setOnTouchListener(this);
    garden = new Garden(getContext());
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    for(short i = 0; i < garden.getVeggies().size(); i++) {
      int row = i / VEGETABLES_PER_ROW;
      int column = i % VEGETABLES_PER_ROW;
      int x = column * width_pixels_per_vegetable;
      int y = row * height_pixels_per_vegetable;
      garden.getVeggies().get(i).draw(x,
                                      y,
                                      width_pixels_per_vegetable,
                                      height_pixels_per_vegetable,
                                      canvas,
                                      getContext());
    }
    drawFarmer(canvas);
  }

  private void drawFarmer(Canvas canvas) {
    Drawable farmer = getContext().getResources().getDrawable(R.drawable.farmer);
    int i = (int) Math.floor(garden.lastDistance / garden.METERS_PER_VEGETABLE);
    if(i != 0){
      i -= 1;
    }
    int x = i % VEGETABLES_PER_ROW;
    int y = i / VEGETABLES_PER_ROW;
    x *= width_pixels_per_vegetable;
    y *= height_pixels_per_vegetable;
    farmer.setBounds(x,
                     y,
                     x + width_pixels_per_vegetable,
                     y + height_pixels_per_vegetable);
    farmer.draw(canvas);

  }

  public void update(float newDistance) {

    garden.update(newDistance,
                  getContext());
    this.invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec,
                           int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,
                    heightMeasureSpec);
    width_pixels_per_vegetable = getMeasuredWidth() / VEGETABLES_PER_ROW;
    height_pixels_per_vegetable = getMeasuredHeight() / VEGETABLES_PER_SCREEN_HEIGHT;
  }

  @Override
  public boolean onTouch(View view,
                         MotionEvent motionEvent) {
    int x = (int) motionEvent.getX();
    int y = (int) motionEvent.getY();
    x /= width_pixels_per_vegetable;
    y /= height_pixels_per_vegetable;
    int i = x + (y * VEGETABLES_PER_ROW);
    if(i < garden.getLengthInNumberOfVegetables()) { garden.getVeggies().get(i).touch(getContext()); }
    return false;
  }
}
