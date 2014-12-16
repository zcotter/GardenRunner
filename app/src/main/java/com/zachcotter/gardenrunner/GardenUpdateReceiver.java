package com.zachcotter.gardenrunner;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GardenUpdateReceiver extends WakefulBroadcastReceiver {


  public static int THIRSTY_NOTIFIER_INTERVAL = 21600000; //6 hours

  @Override
  public void onReceive(Context context,
                        Intent intent) {
    Garden garden = new Garden(context);
    Vegetable thirstiest = garden.thirstiestPlant();

    if(thirstiest != null) {
      try {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context
                                                                                                   .NOTIFICATION_SERVICE);
        Intent nextIntent = new Intent(context.getApplicationContext(),
                                       GardenMenu.class);

        nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(),
                                                                1,
                                                                nextIntent,
                                                                PendingIntent.FLAG_UPDATE_CURRENT
                                                                  | PendingIntent.FLAG_ONE_SHOT);
        Builder builder = new Builder(context.getApplicationContext());
        builder.setContentTitle("GardenRunner");
        String message = "Your plants need to be watered! Some of your plants will die if you don't go for a run by ";
        Date tOD = null;
        tOD = new SimpleDateFormat("yyyyMMdd-HHmmss").parse(thirstiest.timeOfDeath().format2445().replace('T',
                                                                                                          '-'));
        message += new SimpleDateFormat("h:mm a").format(tOD);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setAutoCancel(true);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.tomato); //unbelievable that this won't work w/out icon and that fact was not
        // documented
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(1,
                                   builder.build());
      }
      catch(ParseException e) {
        e.printStackTrace();
      }
    }
  }

  public void startAlarm(Context context) {
    Intent receiveAlarm = new Intent(context,
                                     GardenUpdateReceiver.class);
    PendingIntent pending = PendingIntent.getBroadcast(context,
                                                       0,
                                                       receiveAlarm,
                                                       0);
    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    manager.setRepeating(AlarmManager.RTC_WAKEUP,
                         System.currentTimeMillis() + THIRSTY_NOTIFIER_INTERVAL,
                         THIRSTY_NOTIFIER_INTERVAL,
                         pending);
    ComponentName receiver = new ComponentName(context,
                                               GardenBootReceiver.class);
    PackageManager pm = context.getPackageManager();

    pm.setComponentEnabledSetting(receiver,
                                  PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                  PackageManager.DONT_KILL_APP);
  }
}
