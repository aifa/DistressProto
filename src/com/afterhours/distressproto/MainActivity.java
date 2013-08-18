package com.afterhours.distressproto;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.afterhours.distressproto.location.GooglePlayLocationServiceActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends GooglePlayLocationServiceActivity {

	private Button tstBtn;
	private MapFragment mMapFragment;
	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tstBtn = (Button)findViewById(R.id.test_btn);
		tstBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Location location = getCurrentLocation();
				Toast.makeText(v.getContext(), "location is: "+location.getLatitude()+", Long:" + location.getLongitude(), Toast.LENGTH_LONG).show();
				mMap = mMapFragment.getMap();
				
				mMap.setMyLocationEnabled(false);
				mMap.clear();
				LatLng target = new LatLng(location.getLatitude(), location.getLongitude());
				MarkerOptions locationOptions = new MarkerOptions();
				locationOptions.position(target);
				mMap.addMarker(locationOptions);
				
				// Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
				CameraPosition cameraPosition = new CameraPosition.Builder()
				    .target(target)      // Sets the center of the map to Mountain View
				    .zoom(17)                   // Sets the zoom
				    //.bearing(90)                // Sets the orientation of the camera to east
				    //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
				    .build();                   // Creates a CameraPosition from the builder
				
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
			}
		});
		
		GoogleMapOptions options = new GoogleMapOptions();
		options.mapType(GoogleMap.MAP_TYPE_HYBRID).compassEnabled(false).rotateGesturesEnabled(false).tiltGesturesEnabled(false);
		
		//add a map fragment
		 mMapFragment = MapFragment.newInstance(options);
		 
		 FragmentTransaction fragmentTransaction =
		         getFragmentManager().beginTransaction();
		 fragmentTransaction.add(R.id.fragmentContainer, mMapFragment);
		 fragmentTransaction.commit();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
