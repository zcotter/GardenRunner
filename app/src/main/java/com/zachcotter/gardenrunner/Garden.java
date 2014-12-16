package com.zachcotter.gardenrunner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Gravity;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.ListIterator;

import edu.neu.madcourse.zachcotter.garden.Vegetable.Species;

public class Garden implements JSONSerializable {
  public static final String VEGGIES_KEY = "veggies";
  public static final String LAST_DISTANCE_KEY = "lastdistance";
  public static final String GARDEN_PREFERENCES_KEY = "GardenPrefs";
  public static final short METERS_PER_VEGETABLE = 50;
  private static final String SAVE_GARDEN_KEY = "savedgarden";
  public static final String NUMBER_OF_EXPIRED_PLANTS_KEY = "expired";
  private ArrayList<Vegetable> veggies;
  public float lastDistance;

  public Garden(Context context) {
    veggies = new ArrayList<Vegetable>();
    loadGarden(context);
    lastDistance = 0;
  }

  private void removeExpiredPlants(Context context) {
    ArrayList<Integer> dead = new ArrayList<Integer>();
    for(int i = 0; i < veggies.size(); i++) {
      if(veggies.get(i).isDead()) {
        dead.add(i);
      }
    }
    ListIterator<Integer> dIt = dead.listIterator(dead.size());
    while(dIt.hasPrevious()) {
      veggies.remove(dIt.previous().intValue());
    }
    saveGarden(context);
    SharedPreferences prefs = context.getSharedPreferences(GARDEN_PREFERENCES_KEY,
                                                           Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    int expired = prefs.getInt(NUMBER_OF_EXPIRED_PLANTS_KEY,
                               0);
    expired += dead.size();
    editor.putInt(NUMBER_OF_EXPIRED_PLANTS_KEY,
                  expired);
    editor.commit();
  }

  public static void notifyOfAnyPlantDeaths(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(GARDEN_PREFERENCES_KEY,
                                                           Context.MODE_PRIVATE);
    int expired = prefs.getInt(NUMBER_OF_EXPIRED_PLANTS_KEY,
                               0);
    if(expired > 0) {
      Editor editor = prefs.edit();
      editor.putInt(NUMBER_OF_EXPIRED_PLANTS_KEY,
                    0);
      editor.commit();
      Toast toast = Toast.makeText(context,
                                   expired + " of your plants have died since you last watered your garden. Remember " +
                                     "to water your plants at least every 3 days!",
                                   Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER,
                       0,
                       0);
      toast.show();
    }
  }

  private void waterAndHarvestPassedPlants(float newDistance,
                                           Context context) {
    if(veggies.size() == 0) {
      return;
    }
    int currentPlant = (int) Math.floor(newDistance / METERS_PER_VEGETABLE);
    for(int lastPlantWatered = (int) Math.floor(lastDistance / METERS_PER_VEGETABLE);
        lastPlantWatered < currentPlant;
        lastPlantWatered++) {
      Vegetable next = veggies.get(lastPlantWatered);
      next.water(context);
      next.harvest(context);
    }
  }

  private void updateNewPlants(float newDistance,
                               Context context) {
    float additionalMeters = newDistance - getLengthInMeters();
    short newVeggies = (short) Math.floor(additionalMeters / METERS_PER_VEGETABLE);
    for(short i = 0; i < newVeggies; i++) {
      veggies.add(new Vegetable(ActiveRunMapActivity.getCurrentSpecies(context)));
    }
  }

  public void saveGarden(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(GARDEN_PREFERENCES_KEY,
                                                           Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    String data = serialize();
    editor.putString(SAVE_GARDEN_KEY,
                     data);
    editor.commit();
  }

  private void loadGarden(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(GARDEN_PREFERENCES_KEY,
                                                           Context.MODE_PRIVATE);
    String data = prefs.getString(SAVE_GARDEN_KEY,
                                  "");
    if(!data.equals("")) {
      deserialize(data);
    }
    removeExpiredPlants(context);
  }

  public int stillNeedWater() {
    int thirsty = 0;
    for(Vegetable veggie : veggies) {
      if(veggie.isThirsty()) {
        thirsty += 1;
      }
    }
    return thirsty;
  }

  public Vegetable thirstiestPlant() {
    Vegetable thirstiest = null;
    for(Vegetable veggie : veggies) {
      if(veggie.isThirsty() && (thirstiest == null || veggie.timeOfDeath().before(thirstiest.timeOfDeath()))) {
        thirstiest = veggie;
      }
    }
    return thirstiest;
  }

  @Override
  public String serialize() {
    return new Gson().toJson(veggies);
  }

  public int getLengthInMeters() {
    return veggies.size() * METERS_PER_VEGETABLE;
  }

  public int getLengthInNumberOfVegetables() {
    return veggies.size();
  }

  @Override
  public void deserialize(String json) {
    Gson gson = new Gson();
    Type veggiesType = new TypeToken<ArrayList<Vegetable>>() {}.getType();
    veggies = gson.fromJson(json,
                            veggiesType);
  }

  public ArrayList<Vegetable> getVeggies() {
    return veggies;
  }

  public void update(float newDistance,
                     Context context) {
    updateNewPlants(newDistance,
                    context);
    waterAndHarvestPassedPlants(newDistance,
                                context);
    lastDistance = newDistance;
  }

  public void firstTimeSetup(Context context) {
    for(int i = 0; i < 5; i++){
      TutorialVegetable freebie = new TutorialVegetable(Species.TOMATO, i);
      veggies.add(freebie);
      update(0, context);
    }
  }
}