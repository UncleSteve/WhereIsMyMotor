package kwhung.example.whereismymotor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {
	private LocationManager mgr;
	private GoogleMap mMap;
	private TextView tv;
	private String bestProvider;
	private MyLocationListener mll;
	private Location location;
	private Marker parkPos;
	private static boolean STATUS_PARKED = true;
	private static boolean STATUS_NOT_PARKED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("GG", "onCreate");
        
		tv = (TextView)findViewById(R.id.location_status);
		mgr = (LocationManager)getSystemService(LOCATION_SERVICE);
		mll = new MyLocationListener();
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_LOW);
		bestProvider = mgr.getBestProvider(criteria, true);
		location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(bestProvider != null) {
			location = mgr.getLastKnownLocation(bestProvider);
		}
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("GG", "onResume");

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		bestProvider = mgr.getBestProvider(criteria, true);
		if(bestProvider != null) {
			mgr.requestLocationUpdates(bestProvider, 1000, 1, mll);
		} else {
			mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mll);
		}
		initMap(location, bestProvider);
	}
    
	@Override
	protected void onPause() {
		super.onPause();
		Log.i("GG", "onPause");
		
		mgr.removeUpdates(mll);
	}
    
	private void initMap(Location location, String provider){
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				while (location == null) {
					tv.setText(R.string.location_get);
					mgr.requestLocationUpdates(bestProvider, 1000, 1, mll);
				}
				// The Map is verified. It is now safe to manipulate the map.
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				Log.i("GG", "initMaping" + location.toString());
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				
				LatLng currentPosition = new LatLng(lat, lng);
				//Log.i("GG", location.toString());
				mMap.addMarker(new MarkerOptions()
				.position(currentPosition)
				.title("Motor")
				.snippet("Here is your motor")
				.draggable(false)
				.visible(true)
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation)));
				
				CameraPosition cameraPosition = new CameraPosition.Builder()
			    .target(currentPosition)    // Sets the center of the map
			    .zoom(15)                   // Sets the zoom
			    .build();                   // Creates a CameraPosition from the builder
				
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				
				Log.i("GG", "addMarker Good");
				// Show preference motor marker
				SharedPreferences prefPosition = getSharedPreferences("PREF_MOTOR_POS", MODE_PRIVATE);
				if( prefPosition.getBoolean("PREF_PARK_STATUS", STATUS_PARKED) ) {
					// PARKED show it on screen!
					Double defaultLat = location.getLatitude();
					Double defaultLng = location.getLongitude();
					
					String latStr = prefPosition.getString("PREF_LAT", defaultLat.toString());
					String lngStr = prefPosition.getString("PREF_LNG", defaultLng.toString());

					Toast.makeText(this, "Your motor was parked at Lat="+latStr+" Lng="+lngStr, Toast.LENGTH_SHORT).show();

					LatLng parkPosition = new LatLng(Double.valueOf(latStr), Double.valueOf(lngStr));
					
					parkPos = mMap.addMarker(new MarkerOptions()
						.position(parkPosition)
						.title("Motor")
						.draggable(false)
						.visible(true)
						.anchor(0.5f, 0.5f)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.motor_64px)));
				}
			}
		}
	}

	public void animToMeOnClick(View v) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
	    .target(new LatLng(location.getLatitude(), location.getLongitude()))      		// Sets the center of the map to ZINTUN
	    .zoom(15)                   // Sets the zoom
	    .build();                   // Creates a CameraPosition from the builder
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	public void saveMotorPositon(View v) {
		SharedPreferences prefPosition = getSharedPreferences("PREF_MOTOR_POS", MODE_PRIVATE);
		SharedPreferences.Editor preEdt = prefPosition.edit();
		
		Double Lat = location.getLatitude();
		Double Lng = location.getLongitude();
		
		preEdt.putBoolean("PREF_PARK_STATUS", STATUS_PARKED);
		preEdt.putString("PREF_LAT", Lat.toString());
		preEdt.putString("PREF_LNG", Lng.toString());
		preEdt.commit();
		
		// Toast.makeText(this, "Park at Lat="+Lat.toString()+" Lng="+Lng.toString()+"\nSave succesfully!", Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "Save succesfully!", Toast.LENGTH_SHORT).show();
		
		LatLng currentPosition = new LatLng(Lat, Lng);

		parkPos = mMap.addMarker(new MarkerOptions()
		.position(currentPosition)
		.title("Motor")
		.draggable(false)
		.visible(true)
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.parking_icon_64px)));
	}
	
	public void pickUpOnClick(View v) {
		// Get preferences
		SharedPreferences prefPosition = getSharedPreferences("PREF_MOTOR_POS", MODE_PRIVATE);
		
		if(prefPosition.getBoolean("PREF_PARK_STATUS", STATUS_PARKED) == true) {
			// PARKED
			parkPos.remove();
			// Double defaultLat = location.getLatitude();
			// Double defaultLng = location.getLongitude();
			
			// String latStr = prefPosition.getString("PREF_LAT", defaultLat.toString());
			// String lngStr = prefPosition.getString("PREF_LNG", defaultLng.toString());
			
			// Toast.makeText(this, "Your motor was parked at Lat="+latStr+" Lng="+lngStr+"\nPick up successfully!", Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "Pick up successfully!", Toast.LENGTH_SHORT).show();
			
			SharedPreferences.Editor preEdt = prefPosition.edit();
			preEdt.putBoolean("PREF_PARK_STATUS", STATUS_NOT_PARKED);
			preEdt.commit();
		}
		else {
			// NOT PARKED
			Toast.makeText(this, "You haven't parked yet!", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void zoomToMotor(View v) {
		// Get preferences
		SharedPreferences prefPosition = getSharedPreferences("PREF_MOTOR_POS", MODE_PRIVATE);
		
		if( prefPosition.getBoolean("PREF_PARK_STATUS", STATUS_PARKED)==true ) {
			// PARKED
			Double defaultLat = location.getLatitude();
			Double defaultLng = location.getLongitude();
			
			String latStr = prefPosition.getString("PREF_LAT", defaultLat.toString());
			String lngStr = prefPosition.getString("PREF_LNG", defaultLng.toString());
			
			CameraPosition cameraPosition = new CameraPosition.Builder()
		    .target(new LatLng(Double.valueOf(Double.valueOf(latStr)), Double.valueOf(Double.valueOf(lngStr))))      		// Sets the center of the map
		    .zoom(15)                   // Sets the zoom
		    .build();                   // Creates a CameraPosition from the builder
			
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}
		else {
			// NOT PARKED
			Toast.makeText(this, "You haven't parked yet!", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static boolean isGPSEnabled(Context context){
		 LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		 return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
    
	// Add 2 bar to Menu
	protected static final int MENU_ABOUT = Menu.FIRST;
	protected static final int MENU_Quit = Menu.FIRST+1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0, MENU_ABOUT, 0, R.string.about_me);
		menu.add(0, MENU_Quit, 0, R.string.quit_txt);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    super.onOptionsItemSelected(item);
	    switch(item.getItemId()) {
	    	case MENU_ABOUT:
	    		AboutDialog about = new AboutDialog(this);
				about.setTitle(R.string.about_me_title);
				about.show();
				
				break;
	    	case MENU_Quit:
	    		finish();
	    		break;
	    }
	    return true;
	}
	
	// Show Menu->About me
	/*private void openOptionsDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setTitle(R.string.about_me_title);
		dialog.setMessage(R.string.about_me_content);
		dialog.show();
	}*/
	
	public static String showLocation(Location location) {
		StringBuffer msg = new StringBuffer();
		msg.append("Provider: \n");
		msg.append(location.getProvider());
		msg.append("\n Latitude: \n");
		msg.append(Double.toString(location.getLatitude()));
		msg.append("\b Altitude: \n");
		msg.append(Double.toString(location.getAltitude()));
		
		return msg.toString();
	}
    
	class MyLocationListener implements android.location.LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Log.i("GG", "Location changed!");
			if(location != null) {
				tv.setText(R.string.location_changed);
			} else {
				tv.setText(R.string.location_error);
			}
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Log.i("GG", "onProviderDisabled!");
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			Log.i("GG", "onProviderEnabled!");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			Log.i("GG", "onStatusChanged!");
		}

	}
}