package com.afterhours.distressproto;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afterhours.distressproto.camera.CameraPreview;
import com.afterhours.distressproto.camera.CameraUtils;
import com.afterhours.distressproto.util.location.GooglePlayLocationServiceActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MainActivity extends GooglePlayLocationServiceActivity implements
		LoaderCallbacks<Cursor> {

	private Button tstBtn;
	private SupportMapFragment mMapFragment;
	private GoogleMap mMap;
	// contact URI
	private Uri contactURI;
	private static final int CONTACT_REQUEST_ID = 1;
	private static final int CONTACT_LOADER_ID = 1;

	private Camera mCameraFront;
	private Camera mCameraBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initCameras();

		// contact section
		Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, CONTACT_REQUEST_ID);

		tstBtn = (Button) findViewById(R.id.test_btn);
		tstBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Location location = getCurrentLocation();
				Toast.makeText(
						v.getContext(),
						"location is: " + location.getLatitude() + ", Long:"
								+ location.getLongitude(), Toast.LENGTH_LONG)
						.show();
				mMap = mMapFragment.getMap();

				mMap.setMyLocationEnabled(false);
				mMap.clear();
				LatLng target = new LatLng(location.getLatitude(), location
						.getLongitude());
				MarkerOptions locationOptions = new MarkerOptions();
				locationOptions.position(target);
				mMap.addMarker(locationOptions);

				// Construct a CameraPosition focusing on Mountain View and
				// animate the camera to that position.
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(target) // Sets the center of the map to
										// Mountain View
						.zoom(17) // Sets the zoom
						// .bearing(90) // Sets the orientation of the camera to
						// east
						// .tilt(30) // Sets the tilt of the camera to 30
						// degrees
						.build(); // Creates a CameraPosition from the builder

				mMap.animateCamera(
						CameraUpdateFactory.newCameraPosition(cameraPosition),
						1000, null);

				if (CameraUtils.checkCameraHardware(v.getContext())) {
//						CameraUtils.takePictureNoPreview( mCameraFront);
						CameraUtils.takePictureNoPreview( mCameraBack);
				}

			}
		});

		GoogleMapOptions options = new GoogleMapOptions();
		options.mapType(GoogleMap.MAP_TYPE_HYBRID).compassEnabled(false)
				.rotateGesturesEnabled(false).tiltGesturesEnabled(false);

		// add a map fragment
		mMapFragment = SupportMapFragment.newInstance(options);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.fragmentContainer, mMapFragment);
		fragmentTransaction.commit();

	}

	private void initCameras() {
		if (CameraUtils.checkCameraHardware(this)) {
			int numberOfCameras = Camera.getNumberOfCameras();

			for (int i = 0; i < numberOfCameras; i++) {
				
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
//					mCameraFront = CameraUtils.getCameraInstance(i);
//					// Create our Preview view and set it as the content of our
//					// activity.
//					Toast.makeText(this, "camera:" + i, Toast.LENGTH_LONG).show();
//					CameraPreview cameraPreview = new CameraPreview(this,
//							mCameraFront);
//
//					FrameLayout preview = (FrameLayout) findViewById(R.id.frontCamera);
//					preview.addView(cameraPreview);
				} else if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
					mCameraBack = CameraUtils.getCameraInstance(i);
					// Create our Preview view and set it as the content of our
					// activity.
					CameraPreview cameraPreview = new CameraPreview(this,
							mCameraBack);

					FrameLayout preview = (FrameLayout) findViewById(R.id.bckCamera);
					preview.addView(cameraPreview);
				}
			}
		} else {
			Toast.makeText(this, "No Cameras detected", Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONTACT_REQUEST_ID:
			switch (resultCode) {
			case Activity.RESULT_OK:
				contactURI = data.getData();
				// init content loader
				getSupportLoaderManager().initLoader(CONTACT_LOADER_ID, null,
						this);

				break;
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle Bundle) {
		String[] projection = { Phone.NUMBER, Phone.DISPLAY_NAME };
		return new CursorLoader(this, contactURI, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor != null) {
			cursor.moveToFirst();
			// Retrieve the phone number from the NUMBER column
			int column = cursor.getColumnIndex(Phone.NUMBER);
			int nameColumn = cursor.getColumnIndex(Phone.DISPLAY_NAME);
			String number = cursor.getString(column);
			String name = cursor.getString(nameColumn);
			Toast.makeText(this, "number:" + number + " name:" + name,
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Toast.makeText(this, "loader Reset", Toast.LENGTH_LONG).show();
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		CameraUtils.releaseCamera(mCameraBack);
		CameraUtils.releaseCamera(mCameraFront);
		((FrameLayout) findViewById(R.id.bckCamera)).removeAllViews();
		((FrameLayout) findViewById(R.id.frontCamera)).removeAllViews();
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		initCameras();
	}
	
}
