package com.zachcotter.gardenrunner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class GardenMenu extends Activity implements OnClickListener {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.garden_menu);
    findViewById(R.id.new_run_button).setOnClickListener(this);
    if(GardenApplication.gardenEnabled(this)) {
      findViewById(R.id.view_garden_button).setOnClickListener(this);
      findViewById(R.id.store_button).setOnClickListener(this);
      findViewById(R.id.view_garden_button).setVisibility(View.VISIBLE);
      findViewById(R.id.store_button).setVisibility(View.VISIBLE);
    }
    else {
      findViewById(R.id.view_garden_button).setVisibility(View.GONE);
      findViewById(R.id.store_button).setVisibility(View.GONE);
    }
    findViewById(R.id.garden_ack_button).setOnClickListener(this);

    Garden.notifyOfAnyPlantDeaths(this);
  }


  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.new_run_button:
        Intent startRun = new Intent(this,
                                     ActiveRunMapActivity.class);
        if(findViewById(R.id.view_garden_button).getVisibility() == View.GONE){
          GardenApplication.enableGarden(this);
          startRun.putExtra(ActiveRunMapActivity.FIRST_RUN_KEY, true);
        }
        startActivity(startRun);

        return;
      case R.id.view_garden_button:
        Intent viewGarden = new Intent(this,
                                       MapActivity.class);
        startActivity(viewGarden);
        return;
      case R.id.store_button:
        Intent goToStore = new Intent(this,
                                      Store.class);
        startActivity(goToStore);
        return;
      case R.id.garden_ack_button:
        new GardenAcknowledgements().createDialog(this).show();
        return;
    }
  }
}
