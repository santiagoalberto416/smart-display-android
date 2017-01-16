package com.skirk.smartdisplay;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity{

    TextView latitude;
    TextView longittude;
    Location location;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            Log.d("Location", location.getLatitude()+ ", "+location.getLongitude());
            latitude.setText(location.getLatitude()+" ");
            longittude.setText(location.getLongitude()+" ");
            float distanceInMeters = location.distanceTo(MainActivity.this.location);
            Log.d("Distance", distanceInMeters+" ");

            Location location1=new Location("locationA");
            location1.setLatitude(17.372102);
            location1.setLongitude(78.484196);
            Location location2=new Location("locationA");
            location2.setLatitude(17.375775);
            location2.setLongitude(78.469218);
            double distance=location2.distanceTo(location1);
            Log.d("testDistance", distance+" ");
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }


        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

    private LocationManager locationManager = null;


    //32.52239916
    //32.52239699
    //32.5223972


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        location = new Location("");
        //32.52239863, -117.01884061
        location.setLatitude(32.52239863);
        location.setLongitude(-117.01884061);

        latitude = (TextView)this.findViewById(R.id.lattitude);
        longittude = (TextView)this.findViewById(R.id.longitude);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
        }catch (SecurityException ex){
            Log.d("Security Error", ex.getMessage());
        }
    }






}
