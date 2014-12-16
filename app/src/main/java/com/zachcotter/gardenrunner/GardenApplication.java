package com.zachcotter.gardenrunner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GardenApplication {
  private static final String GARDEN_ENABLED_KEY = "gardneanableed";
  public static Activity currentActivity;

  public static boolean gardenEnabled(Context context){
    SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY, Context.MODE_PRIVATE);
    return prefs.getBoolean(GARDEN_ENABLED_KEY, false);
  }

  public static void enableGarden(Context context){
    SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY, Context.MODE_PRIVATE);
    if(!prefs.getBoolean(GARDEN_ENABLED_KEY, false)){
      Editor editor = prefs.edit();
      editor.putBoolean(GARDEN_ENABLED_KEY, true);
      editor.commit();
    }
  }
}
