package com.zachcotter.gardenrunner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GardenBootReceiver extends BroadcastReceiver {

  GardenUpdateReceiver receiver = new GardenUpdateReceiver();

  @Override
  public void onReceive(Context context,
                        Intent intent) {
    if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
      receiver.startAlarm(context);
    }
  }
}
