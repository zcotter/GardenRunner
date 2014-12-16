package com.zachcotter.gardenrunner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Vegetable implements JSONSerializable {

  public static final String VEGETABLE_KEY = "vegeatbel";
  protected Time lastWatered;
  private static final long MILLIS_TIL_DEATH = 259200000; // THREE DAYS
  protected Species species;

  public enum Species {
    PUMPKIN, EGGPLANT, TOMATO, PEPPER;

    public int getCost(){
      switch(this){
        case TOMATO:
          return 0;
        case PEPPER:
          return 1000;
        case EGGPLANT:
          return 10000;
        case PUMPKIN:
          return 100000;
        default:
          throw new RuntimeException("Species not found");
      }
    }

    public Drawable getIcon(Context context){
      int id = context.getResources().getIdentifier(this.toString().toLowerCase(),
                                                    "drawable",
                                                    context.getPackageName());
      return context.getResources().getDrawable(id);
    }

    public int getSellingPrice(){
      switch(this){
        case TOMATO:
          return 1;
        case PEPPER:
          return 2;
        case EGGPLANT:
          return 3;
        case PUMPKIN:
          return 4;
        default:
          throw new RuntimeException("Species not found");
      }
    }
  }

  public Vegetable(Species species) {
    this.lastWatered = new Time();
    this.water(null);
    this.species = species;
  }

  public Vegetable(Species species, boolean tutorial){
    this.lastWatered = new Time();
    this.water(null);
    this.makeThirsty();
    this.species = species;
  }

  public void water(Context context) {
    lastWatered.setToNow();
  }

  private void makeThirsty(){
    long birthday = lastWatered.toMillis(false) - (MILLIS_TIL_DEATH / 2);
    lastWatered.set(birthday);
  }

  public boolean isThirsty() {
    Time now = new Time();
    now.setToNow();
    return now.after(timeOfNeedsWater());
  }

  public boolean isDead() {
    Time now = new Time();
    now.setToNow();
    return now.after(timeOfDeath());
  }

  public Time timeOfDeath() {
    Time death = new Time();
    death.set(lastWatered.toMillis(false) + MILLIS_TIL_DEATH);
    return death;
  }

  public Time timeOfNeedsWater() {
    Time water = new Time();
    water.set(lastWatered.toMillis(false) + (MILLIS_TIL_DEATH / 2));
    return water;
  }

  @Override
  public void deserialize(String json) {
    Type thisType = new TypeToken<Vegetable>() {}.getType();
    Vegetable clone = new Gson().fromJson(json,
                                          thisType);
    this.lastWatered = clone.getLastWatered();
    this.species = clone.getSpecies();
  }

  public Species getSpecies() {
    return this.species;
  }

  @Override
  public String serialize() {
    return new Gson().toJson(this);
  }

  public Time getLastWatered() {
    return lastWatered;
  }

  public void draw(int x,
                   int y,
                   int width,
                   int height,
                   Canvas canvas,
                   Context context) {
    Paint paint = new Paint();
    paint.setColor(getColor());
    paint.setStyle(Style.FILL);
    canvas.drawRect(x,
                    y,
                    x + width,
                    y + height,
                    paint);

    Drawable tile = this.getSpecies().getIcon(context);
    tile.setBounds(x,
                   y,
                   x + width,
                   y + height);
    tile.draw(canvas);

  }

  private int getColor() {
    if(isThirsty()) {
      return Color.RED;
    }
    return Color.BLUE;
  }

  public void touch(Context context) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(this.getSpecies().name());
    View dialogView = View.inflate(context,
                                   R.layout.vegetable,
                                   null);
    ((VegetableView) dialogView.findViewById(R.id.vegetable_view)).setVegetable(this);
    try {
      Date lastWatered = new SimpleDateFormat("yyyyMMdd-HHmmss").parse(this.getLastWatered().format2445().replace('T',
                                                                                                                  '-'));
      Date tOD = new SimpleDateFormat("yyyyMMdd-HHmmss").parse(this.timeOfDeath().format2445().replace('T',
                                                                                                       '-'));

      String lastWateredDate = new SimpleDateFormat("EEE, MMM dd").format(lastWatered);
      String lastWateredTime = new SimpleDateFormat("h:mm a").format(lastWatered);
      String deathDate = new SimpleDateFormat("EEE, MMM dd").format(tOD);
      String deathTime = new SimpleDateFormat("h:mm a").format(tOD);

      String text = "This " + this.getSpecies().toString().toLowerCase() + " was last watered on ";
      text += lastWateredDate + " at " + lastWateredTime + ".\nIt needs to be watered again by ";
      text += deathTime + " on " + deathDate + ".";

      ((TextView) dialogView.findViewById(R.id.vegetable_status_field)).setText(text);
    }
    catch(ParseException e) {
      e.printStackTrace();
    }
    builder.setView(dialogView);
    builder.setNeutralButton("Close",
                             new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface,
                                                   int i) {
                                 dialogInterface.dismiss();
                               }
                             });
    builder.show();
  }

  public void harvest(Context context){
    SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY, Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    int harvest = prefs.getInt(getSpecies().toString() + "_harvested", 0) + 1;
    editor.putInt(getSpecies().toString() + "_harvested", harvest);
    editor.commit();
  }
}
