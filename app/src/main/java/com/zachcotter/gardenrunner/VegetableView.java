package com.zachcotter.gardenrunner;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;


public class VegetableView extends View {

  private int width;
  private int height;
  private Vegetable vegetable;

  public VegetableView(Context context,
                       AttributeSet attrs) {
    super(context,
          attrs);
  }

  public void setVegetable(Vegetable vegetable) {
    this.vegetable = vegetable;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec,
                           int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,
                    heightMeasureSpec);
    width = getMeasuredWidth();
    height = getMeasuredHeight();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    vegetable.draw(0,
                   0,
                   width,
                   height,
                   canvas,
                   getContext());
  }
}

