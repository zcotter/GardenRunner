package com.zachcotter.gardenrunner;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MapActivity extends Activity implements OnClickListener {

  protected TextView distanceView;
  protected GardenView gardenView;
  protected float distanceInCurrentTraversal;
  private static final String TRAVERSAL_DISTANCE_KEY = "traveraldistance";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GardenApplication.currentActivity = this;
    setContentView(R.layout.map);
    distanceView = (TextView) findViewById(R.id.distance_field);
    gardenView = (GardenView) findViewById(R.id.garden);
    findViewById(R.id.end_run_button).setVisibility(View.GONE);
    findViewById(R.id.species_selector).setVisibility(View.GONE);
    findViewById(R.id.back_to_menu).setOnClickListener(this);
    if(savedInstanceState != null) {
      reinitialize(savedInstanceState);
    }
    else {
      initialize();
    }
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    Garden.notifyOfAnyPlantDeaths(this);
  }

  protected void initialize() {
    //nothing needed here, but can override
  }

  protected void reinitialize(Bundle savedInstanceState) {
    distanceInCurrentTraversal = savedInstanceState.getFloat(TRAVERSAL_DISTANCE_KEY);
    gardenView.garden.lastDistance = savedInstanceState.getFloat(Garden.LAST_DISTANCE_KEY);
    gardenView.garden.deserialize(savedInstanceState.getString(Garden.VEGGIES_KEY));
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(Garden.VEGGIES_KEY,
                       gardenView.garden.serialize());
    outState.putFloat(Garden.LAST_DISTANCE_KEY,
                      gardenView.garden.lastDistance);
    outState.putFloat(TRAVERSAL_DISTANCE_KEY,
                      distanceInCurrentTraversal);
  }

  protected float getDistance() {
    SharedPreferences prefs = getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                   MODE_PRIVATE);
    return prefs.getFloat(DistanceTrackerService.DISTANCE_TRAVELLED_KEY,
                          0);
  }

  protected void resetDistance() {
    SharedPreferences prefs = getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                   MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.putFloat(DistanceTrackerService.DISTANCE_TRAVELLED_KEY,
                    0);
    editor.commit();
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    finish();
    return true;
  }

  @Override
  public void onClick(View view) {
    switch(view.getId()){
      case R.id.back_to_menu:
        startActivity(new Intent(this, GardenMenu.class));
        finish();
        return;
    }
  }
}
