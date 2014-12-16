package com.zachcotter.gardenrunner;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class DistanceTrackerService extends Service implements ConnectionCallbacks,
                                                               OnConnectionFailedListener,
                                                               LocationListener {

  public static final String TAG = "DistanceTrackerService";

  public static final String DISTANCE_TRAVELLED_KEY = "DistanceTrav";

  private boolean connected;
  private LocationRequest locationRequest;
  private GoogleApiClient locationClient;
  private static final long UPDATE_INTERVAL = 2000;
  private static final int ACCURACY_PERCENT = 10;

  private static final int TIME_THRESHOLD = 30000; // get a location at least every 30 secs, no matter how bad it is
  private static final int VELOCITY_THRESHOLD = 24; //meters per second (~=twice the world record)

  private float distance;
  private Location lastLocation;

  public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    distance = 0;
    lastLocation = null;
    locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(UPDATE_INTERVAL);
    locationRequest.setFastestInterval(UPDATE_INTERVAL);
  }

  @Override
  public int onStartCommand(Intent intent,
                            int flags,
                            int startId) {
    locationClient = new GoogleApiClient.Builder(this)
                                        .addApi(LocationServices.API)
                                        .addConnectionCallbacks(this)
                                        .addOnConnectionFailedListener(this)
                                        .build();
    locationClient.connect();
    return super.onStartCommand(intent,
                                flags,
                                startId);
  }

  @Override
  public void onDestroy() {
    if(connected) {
      locationClient.disconnect();
    }
    super.onDestroy();
  }

  @Override
  public void onConnected(Bundle bundle) {
    connected = true;
    LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, this);
  }

  @Override
  public void onConnectionSuspended(int i) {
    connected = false;
  }

  private boolean isConnected() {
    return connected;
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    if(connectionResult.hasResolution()) {
      try {
        connectionResult.startResolutionForResult(GardenApplication.currentActivity,
                                                  CONNECTION_FAILURE_RESOLUTION_REQUEST);
      }
      catch(IntentSender.SendIntentException e) {
        e.printStackTrace();
      }
    }
    else {
      showErrorDialog(connectionResult.getErrorCode());
    }
  }

  private boolean showErrorDialog(int errorCode) {
    int resultCode =
      GooglePlayServicesUtil.
        isGooglePlayServicesAvailable(this);
    // If Google Play services is available
    if(ConnectionResult.SUCCESS == resultCode) {
      return true;
    }
    else {
      Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                                                                 GardenApplication.currentActivity,
                                                                 CONNECTION_FAILURE_RESOLUTION_REQUEST);
      // If Google Play services can provide an error dialog
      if(errorDialog != null) {
        // Create a new DialogFragment for the error dialog
        ErrorDialogFragment errorFragment = new ErrorDialogFragment();
        // Set the dialog in the DialogFragment
        errorFragment.setDialog(errorDialog);
        // Show the error dialog in the DialogFragment
        errorFragment.show(GardenApplication.currentActivity.getFragmentManager(),
                           "Location Updates");

      }
      return false;
    }
  }

  public static class ErrorDialogFragment extends DialogFragment {
    private Dialog mDialog;

    public ErrorDialogFragment() {
      super();
      mDialog = null;

    }

    public void setDialog(Dialog dialog) {
      mDialog = dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return mDialog;
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    if(lastLocation == null) {
      onFilteredLocationChanged(location);
    }
    else {
      float currentAccuracy = location.getAccuracy();
      float lastAccuracy = lastLocation.getAccuracy();
      float accuracyDiff = Math.abs(lastAccuracy - currentAccuracy);
      boolean lowerAccuracyAcceptable = currentAccuracy > lastAccuracy &&
        lastLocation.getProvider().equals(location.getProvider()) &&
        accuracyDiff <= lastAccuracy / ACCURACY_PERCENT;
      float distance = lastLocation.distanceTo(location);
      float velocity = distance / ((location.getTime() - lastLocation.getTime()) / 1000);
      if(velocity <= VELOCITY_THRESHOLD &&
        (currentAccuracy < lastAccuracy ||
          location.getTime() - lastLocation.getTime() > TIME_THRESHOLD ||
          lowerAccuracyAcceptable)) {
        onFilteredLocationChanged(location);
      }
    }
  }

  public void onFilteredLocationChanged(Location location) {
    float distanceChange = 0;
    if(lastLocation != null) {
      distanceChange = location.distanceTo(lastLocation);
    }
    lastLocation = location;
    distance += distanceChange;
    storeDistance();
  }

  private void storeDistance() {
    SharedPreferences prefs = getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                   MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.putFloat(DISTANCE_TRAVELLED_KEY,
                    distance);
    editor.commit();
  }
}
