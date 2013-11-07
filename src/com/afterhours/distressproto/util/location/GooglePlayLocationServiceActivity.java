package com.afterhours.distressproto.util.location;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

public class GooglePlayLocationServiceActivity extends FragmentActivity
		implements ConnectionCallbacks, OnConnectionFailedListener {
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private ConnectionResult connectionResult;
	private LocationClient mLocationClient; 
	/**
	 * Holds the current location
	 */
	private Location mCurrentLocation;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	//check if Google play services is available.
    	if (servicesConnected()){
    		// Display the connection status
    		Toast.makeText(this, "Google play services are available", Toast.LENGTH_SHORT).show();
    	}
    	/*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
       
    }
	
    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }
    
    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
    
	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			showErrorDialog(connectionResult.getErrorCode());
		}
		
		return false;
	}

	private void showErrorDialog(int errorCode) {
		
		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
				errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {
			// Create a new DialogFragment for the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);
			// Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(),
					"Location Updates");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Decide what to do based on the original request code
		switch (requestCode) {
		
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				/*
				 * Try the request again
				 */
				if (servicesConnected()){
					Toast.makeText(this, "Google services API Available", Toast.LENGTH_SHORT).show();
				}

				break;
			}

		}
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			showErrorDialog(connectionResult.getErrorCode());
		}

	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle arg0) {
		// Display the connection status
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();

	}

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
	
	public Location getCurrentLocation(){
		mCurrentLocation = mLocationClient.getLastLocation();
		return mCurrentLocation;
	}

}
