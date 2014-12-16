package com.zachcotter.gardenrunner;

import android.content.Context;

import edu.neu.madcourse.zachcotter.Tutor;

public class TutorialVegetable extends Vegetable {

  private int index;

  public TutorialVegetable(Species species,
                           int index) {
    super(species,
          true);
    this.index = index;
  }

  @Override
  public void water(Context context) {
    super.water(context);
    if(context == null) {return;}
    Tutor tutor = new Tutor(context);
    if(index == 0) {
      tutor.show(context,
                 Tutor.FIRST_WATER);
      tutor.show(context,
                 Tutor.FIRST_WATER_2);
    }
    if(index == 4) {
      tutor.show(context,
                 Tutor.END_TUTORIAL);
      tutor.show(context,
                 Tutor.END_TUTORIAL_2);
    }
  }
}
