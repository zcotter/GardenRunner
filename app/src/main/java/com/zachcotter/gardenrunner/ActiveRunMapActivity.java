package com.zachcotter.gardenrunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.neu.madcourse.zachcotter.DistanceTrackerService;
import edu.neu.madcourse.zachcotter.GardenUpdateReceiver;
import edu.neu.madcourse.zachcotter.Tutor;
import edu.neu.madcourse.zachcotter.garden.Vegetable.Species;

public class ActiveRunMapActivity extends MapActivity implements OnClickListener {
  private static final String IN_PROGRESS_KEY = "inprograes";
  private static final String SPECIES_KEY = "specieis";
  private static final String AVAILABLE_SPECIES_KEY = "avaeialefjaawspecies";
  private static final String ALARM_SET_KEY = "alsamaset";
  private static final String START_TIME_KEY = "Start time";
  public static final String FIRST_RUN_KEY = "firstrun";
  private boolean visible;
  private boolean inProgress;
  private Time startTime;
  private Tutor tutor;
  private Button speciesButton;

  GardenUpdateReceiver receiver = new GardenUpdateReceiver();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(false);
    findViewById(R.id.back_to_menu).setVisibility(View.GONE);
    findViewById(R.id.end_run_button).setVisibility(View.VISIBLE);
    findViewById(R.id.species_selector).setVisibility(View.VISIBLE);
    findViewById(R.id.end_run_button).setOnClickListener(this);
    GardenApplication.currentActivity = this;
    speciesButton = (Button) findViewById(R.id.species_selector);
    speciesButton.setOnClickListener(this);
    speciesButton.setCompoundDrawablesWithIntrinsicBounds(null,
                                                          null,
                                                          getCurrentSpecies().getIcon(this),
                                                          null);
    tutor = new Tutor(this);
    if(getIntent().getBooleanExtra(FIRST_RUN_KEY, false)){
      firstTimeSetup();
    }
    startRun();
    startNotifications();
    tutor.show(this, Tutor.WELCOME);
    tutor.show(this, Tutor.EXPLAIN_WATER);
  }

  private void firstTimeSetup(){
    gardenView.garden.firstTimeSetup(this);
  }

  private void startNotifications() {
    receiver.startAlarm(this);
  }

  protected void initialize() {
    startTime = new Time();
    startTime.setToNow();
    distanceInCurrentTraversal = getDistance();
    inProgress = false;
    super.initialize();
  }


  @Override
  protected void reinitialize(Bundle savedInstanceState) {
    inProgress = savedInstanceState.getBoolean(IN_PROGRESS_KEY);
    startTime = new Time();
    startTime.parse(savedInstanceState.getString(START_TIME_KEY));
    super.reinitialize(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(IN_PROGRESS_KEY,
                        inProgress);
    outState.putString(START_TIME_KEY,
                       startTime.toString());
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    visible = hasFocus;
    if(visible) {
      startWatchingLocation();
    }
  }

  private void startWatchingLocation() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while(visible) {
          try {
            Thread.sleep(2000);
          }
          catch(InterruptedException e) {
            e.printStackTrace();
          }
          finally {
            final float distance = getDistance();
            distanceInCurrentTraversal = distance;
            distanceView.post(new Runnable() {
              @Override
              public void run() {
                double roundedDistance = Math.round(distance * 100.0) / 100.0;
                distanceView.setText("Distance: " + roundedDistance + " meters");
                gardenView.update(distance);
              }
            });
          }

        }
      }
    }).start();
  }

  private void startRun() {
    startTrackerService();
    inProgress = true;
  }

  private void stopRun() {
    stopTrackerService();
    if(gardenView.garden.getLengthInNumberOfVegetables() * Garden.METERS_PER_VEGETABLE <= distanceInCurrentTraversal) {
      resetDistance();
    }
    inProgress = false;
    gardenView.garden.saveGarden(this);
  }

  private void startTrackerService() {
    Intent startRun = new Intent(this,
                                 DistanceTrackerService.class);
    startRun.addCategory(DistanceTrackerService.TAG);
    startService(startRun);
  }

  private void stopTrackerService() {
    Intent stopRun = new Intent(this,
                                DistanceTrackerService.class);
    stopRun.addCategory(DistanceTrackerService.TAG);
    stopService(stopRun);
  }

  protected void onActivityResult(int requestCode,
                                  int resultCode,
                                  Intent data) {
    switch(requestCode) {
      case DistanceTrackerService.CONNECTION_FAILURE_RESOLUTION_REQUEST:
        switch(resultCode) {
          case Activity.RESULT_OK:
            stopRun();
            startRun();
            return;
        }
    }
  }

  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.end_run_button:
        showEndRunConfirmation();
        return;
      case R.id.species_selector:
        showSpeciesSelector();
        return;
    }
  }

  protected void showSpeciesSelector() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setCancelable(true);
    builder.setTitle("Choose a vegetable to plant.");
    Species[] species = getAvailableSpecies();
    CharSequence[] items = new CharSequence[species.length];
    int currentIndex = -1;
    String currentSpecies = getCurrentSpecies(this).toString();
    for(int i = 0; i < items.length; i++) {
      items[i] = species[i].toString();
      if(currentSpecies.equals(items[i])) {
        currentIndex = i;
      }
    }
    builder.setSingleChoiceItems(items,
                                 currentIndex,
                                 new ActiveRunMapActivityAlertDialogSpeciesSelectorClickListener());
    builder.show();
  }

  private void showEndRunConfirmation() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle("End run?");
    builder.setMessage("Are you sure you are done with your run?");
    builder.setNegativeButton("I'm Done.",
                              new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface,
                                                    int i) {
                                  int money = Math.round(Store.MONEY_PER_METER * distanceInCurrentTraversal);
                                  Store.addMoney(money,
                                                 getApplicationContext());
                                  String savedDistance = distanceView.getText().toString() + " earned you $" + money;
                                  stopRun();
                                  dialogInterface.dismiss();
                                  showResultsDialog(savedDistance);
                                }
                              });
    builder.setPositiveButton("Not yet.",
                              new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface,
                                                    int i) {
                                  dialogInterface.dismiss();
                                }
                              });
    builder.show();
  }

  private String elapsedTime() {
    Time now = new Time();
    now.setToNow();
    long diff = now.toMillis(true) - startTime.toMillis(true);
    long totalMinutes = diff / 1000 / 60;
    long hours = totalMinutes / 60;
    long minutes = totalMinutes % 60;
    return hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
  }

  private void showResultsDialog(String savedDistance) {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle("Results.");
    String message = savedDistance + "\n";
    message += "Time: " + elapsedTime() + "\n";
    message += collectHarvest();
    int thirsty = gardenView.garden.stillNeedWater();
    double percent = thirsty / ((double) gardenView.garden.getLengthInNumberOfVegetables());
    percent = Math.round(percent * 100.0) / 100.0;
    message += thirsty + " plants (" + percent + "%) still need to be watered.";
    builder.setMessage(message);
    builder.setNeutralButton("Back To Main Menu",
                             new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface,
                                                   int i) {
                                 dialogInterface.dismiss();
                                 startActivity(new Intent(getApplicationContext(),
                                                          GardenMenu.class));
                                 finish();
                               }
                             });
    builder.show();
    tutor.show(this, Tutor.END_RUN);
    tutor.show(this, Tutor.END_RUN_2);
    tutor.show(this, Tutor.END_RUN_3);
  }

  public String collectHarvest() {
    SharedPreferences prefs = getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                   MODE_PRIVATE);
    Editor editor = prefs.edit();
    String result = "Harvested:\n";
    for(Species species : Species.values()) {
      int harvest = prefs.getInt(species.toString() + "_harvested",
                                 0);
      if(harvest > 0) {
        int cash = harvest * species.getSellingPrice();
        result += "   " + harvest + "x " + species.toString() + " earned you $" + cash + "\n";
        Store.addMoney(cash,
                       this);
        editor.putInt(species.toString() + "_harvested",
                      0);
      }
    }
    editor.commit();
    return result;
  }

  private class ActiveRunMapActivityAlertDialogSpeciesSelectorClickListener implements DialogInterface.OnClickListener {

    private Species[] species;

    public ActiveRunMapActivityAlertDialogSpeciesSelectorClickListener() {
      species = getAvailableSpecies();
    }

    @Override
    public void onClick(DialogInterface dialogInterface,
                        int i) {
      updateCurrentSpecies(species[i]);
      dialogInterface.dismiss();
    }
  }

  private void updateCurrentSpecies(Species species) {
    SharedPreferences prefs = getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                   MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.putString(SPECIES_KEY,
                     species.toString());
    editor.commit();
    speciesButton.setCompoundDrawablesWithIntrinsicBounds(null,
                                                          null,
                                                          species.getIcon(this),
                                                          null);
  }

  private Species getCurrentSpecies() {
    return getCurrentSpecies(this);
  }

  public static Species getCurrentSpecies(Context c) {
    SharedPreferences prefs = c.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                     MODE_PRIVATE);
    String species = prefs.getString(SPECIES_KEY,
                                     "TOMATO");
    return Species.valueOf(species);
  }

  private Species[] getAvailableSpecies() {
    return getAvailableSpecies(this);
  }

  public static Set<String> getUnavailableSpecies(Context context) {
    Species[] allSpecies = Species.values();
    List<Species> availableSpecies = Arrays.asList(getAvailableSpecies(context));
    HashSet<String> unavailableSpecies = new HashSet<String>();
    for(Species a : allSpecies) {
      if(!availableSpecies.contains(a)) {
        unavailableSpecies.add(a.name());
      }
    }
    return unavailableSpecies;
  }

  public static Species[] getAvailableSpecies(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                           MODE_PRIVATE);

    Set<String> species = prefs.getStringSet(AVAILABLE_SPECIES_KEY,
                                             getDefaultAvailableSpeciesAsSet());
    ArrayList<Species> availableSpecies = new ArrayList<Species>();
    for(String s : species) {
      availableSpecies.add(Species.valueOf(s));
    }
    return availableSpecies.toArray(new Species[0]);
  }

  public static void addAvailableSpecies(Context context,
                                         Species newSpecies) {
    SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                           MODE_PRIVATE);
    Set<String> species = prefs.getStringSet(AVAILABLE_SPECIES_KEY,
                                             getDefaultAvailableSpeciesAsSet());
    species.add(newSpecies.toString());
    Editor editor = prefs.edit();
    editor.putStringSet(AVAILABLE_SPECIES_KEY,
                        species);
    editor.commit();
  }

  private static final Species[] getDefaultAvailableSpecies() {
    Species[] species = {Species.TOMATO};
    return species;
  }

  private static Set<String> getDefaultAvailableSpeciesAsSet() {
    Species[] demSpeciesYo = getDefaultAvailableSpecies();
    Set<String> defaultSpecies = new HashSet<String>();
    for(Species species : demSpeciesYo) {
      defaultSpecies.add(species.toString());
    }
    return defaultSpecies;
  }
}
