package org.mattflynn.righthere;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.mattflynn.righthere.ShakeDetector.OnShakeListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends FragmentActivity implements 
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
	OnClickListener
	{

	private static final int LOC_ERRORDIALOG_REQUEST = 9002;
	private static final float DEFAULT_ZOOM = 15;
	public final static String SEND_URL = "org.mattflynn.righthere.SEND_URL";;
	private GoogleMap mMap;
	private LatLng sendLocation;
	private LatLng centreLocation;
	private static final String mapurl ="https://maps.google.com/?q=";
	
	// The following are used for the shake detection
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;

	
	LocationClient mLocationClient;
	Marker marker;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(servicesCheck()){
			setContentView(R.layout.activity_main);
			if (deployMap()) {
				mLocationClient = new LocationClient(this, this, this);
				mLocationClient.connect();
				Button btnhere = (Button) findViewById(R.id.iamhere);
				btnhere.setOnClickListener(this);
				mMap.setMyLocationEnabled(true);
				mMap.getUiSettings().setZoomControlsEnabled(false);				
			}
		} 
		else{
			Toast.makeText(this, "Map not available.", Toast.LENGTH_SHORT).show();
		}

		// ShakeDetector initialization
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeDetector = new ShakeDetector();
		mShakeDetector.setOnShakeListener(new OnShakeListener() {

			@Override
			public void onShake(int count) {
				
				Random rand = new Random();
				int randomNum = rand.nextInt((4 - 1) + 1) + 1;
				
				switch (randomNum) {
				case 1:
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
				case 2:
				mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
				case 3:
				mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
				case 4:
				mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				default:
					break;
			}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mapTypeNormal:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
			case R.id.mapTypeSatellite:
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
			case R.id.mapTypeTerrain:
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
			case R.id.mapTypeHybrid:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public boolean servicesCheck() {
		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		
		if (isAvailable == ConnectionResult.SUCCESS){
			return true;
		}
		else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, LOC_ERRORDIALOG_REQUEST);
			dialog.show();
		}
		else {
			Toast.makeText(this, "Unable to connect to Google Play services", Toast.LENGTH_SHORT).show();
		}
		return false;
	}
	
	public void geoLocate(View v) throws IOException {
		hideSoftKeyboard(v);
		
		EditText et = (EditText) findViewById(R.id.editText1);
		String location = et.getText().toString();
		
		Geocoder gc = new Geocoder(this);
		List<Address> list = gc.getFromLocationName(location, 1);
		Address add = list.get(0);
		String locality = add.getLocality();
		Toast.makeText(this, locality, Toast.LENGTH_LONG).show();
		
		double lat = add.getLatitude();
		double lng = add.getLongitude();
		
		gotoLocation(lat, lng, DEFAULT_ZOOM);
	
	}
	
	private void gotoLocation(double lat, double lng,
			float zoom) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		mMap.moveCamera(update);
	}
	
	private void hideSoftKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	private boolean deployMap() {
		if(mMap == null){
			MapFragment mapFrag = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
			mMap = mapFrag.getMap();
		}
		return (mMap != null);
		}
	
	protected void getCurrentLocation() {
		Location currentLocation = mLocationClient.getLastLocation();
		if (currentLocation == null) {
			Toast.makeText(this, "Cannot fetch your current location", Toast.LENGTH_SHORT).show();
		}
		else {
			LatLng latilong = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			sendLocation = latilong;
			CameraUpdate flash = CameraUpdateFactory.newLatLngZoom(latilong, DEFAULT_ZOOM);
			mMap.animateCamera(flash);
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		MapStateManager manager = new MapStateManager(this);
		manager.saveMapState(mMap);
		mLocationClient.disconnect();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		mLocationClient.disconnect();
		MapStateManager manager = new MapStateManager(this);
		CameraPosition position = manager.getSavedCameraPosition();
		if (position != null) {
			CameraUpdate flash = CameraUpdateFactory.newCameraPosition(position);
			mMap.moveCamera(flash);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mLocationClient.connect();
		MapStateManager manager = new MapStateManager(this);
		CameraPosition position = manager.getSavedCameraPosition();
		if (position != null) {
			CameraUpdate flash = CameraUpdateFactory.newCameraPosition(position);
			mMap.moveCamera(flash);
		}
		mSensorManager.registerListener(mShakeDetector, mAccelerometer,    SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	protected void onPause() {
		mLocationClient.disconnect();
		mSensorManager.unregisterListener(mShakeDetector);
		super.onPause();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		//Toast.makeText(this, "Now Connected to Location Service", Toast.LENGTH_SHORT).show();
		getCurrentLocation();		
		LocationRequest request = LocationRequest.create();
		request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		request.setInterval(75000);
		request.setFastestInterval(25000);
		mLocationClient.requestLocationUpdates(request, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location location) {
		/** Below snippet was used for testing to check location was been returned **/
		/* String message = "Location: " + location.getLatitude() + location.getLongitude();
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		*/
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, ContactMessage.class);
		centreLocation = mMap.getCameraPosition().target;
		String stringRemove = centreLocation.toString().substring(10);
		String finishLat = stringRemove.substring(0,stringRemove.length()-1);
		String fullurl = mapurl + finishLat;
		//FOR TESTING	Toast.makeText(this, fullurl, Toast.LENGTH_LONG).show();
		
		intent.putExtra("SEND_URL", fullurl);
		startActivity(intent);
	}
}
