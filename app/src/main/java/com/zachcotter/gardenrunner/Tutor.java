package com.zachcotter.gardenrunner;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Gravity;
import android.widget.Toast;


public class Tutor {

  public static final Tip WELCOME = new Tip("Welcome to your garden! Looks like your tomatoes need to be watered!");
  public static final Tip EXPLAIN_WATER = new Tip("Walk about " + Garden.METERS_PER_VEGETABLE + " meters to water each plant");
  public static final Tip FIRST_WATER = new Tip("Great! Your first tomato plant has been watered and you harvested 1 tomato!");
  public static final Tip FIRST_WATER_2 = new Tip("Keep walking to water the rest of your plants. Each plant in your garden needs to be watered every 3 days or they will die");
  public static final Tip END_TUTORIAL = new Tip("Great! All of your plants have been watered. You can keep walking to plant some new tomatoes");
  public static final Tip END_TUTORIAL_2 = new Tip("When you are done with your walk, press End Run");
  public static final Tip END_RUN = new Tip("Good work! The plants you've harvested have been exchanged for garden money!");
  public static final Tip END_RUN_2 = new Tip("Garden money can be used to unlock new types of vegetables to plant in the garden");
  public static final Tip END_RUN_3 = new Tip("You also earn garden money for each meter you travelled");


  public static final String TUTORIAL_PREFERENCES_KEY = "TUTORIAL_TIPS";

  private SharedPreferences prefs;
  private Editor prefsEditor;

  public Tutor(Context context) {
    prefs = context.getSharedPreferences(TUTORIAL_PREFERENCES_KEY,
                                         Activity.MODE_PRIVATE);
    prefsEditor = prefs.edit();
  }

  public void show(Context context,
                   Tip tip) {
    if(prefs.getBoolean(tip.getKey(),
                        false) == false) {
      Toast toast = Toast.makeText(context,
                                   tip.getText(),
                                   Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER,
                       0,
                       0);
      toast.show();
      prefsEditor.putBoolean(tip.getKey(),
                             true);
      prefsEditor.commit();
    }
  }


}


