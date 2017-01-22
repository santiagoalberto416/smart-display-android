package com.skirk.smartdisplay;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity{

    TextView latitude;
    TextView longittude;
    Location location;

    private String firstName = "";
    private String lastName = "";
    private String imageFile = "";
    private int idUser = 0;

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

            if(distanceInMeters<10){
                Log.d("notification", "Enter into notification");
                Notification n  = new Notification.Builder(MainActivity.this)
                        .setContentTitle("New mail from " + "test@gmail.com")
                        .setContentText("Subject")
                        .setSmallIcon(R.drawable.new_business)
                        .setAutoCancel(true).build();
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, n);
            }
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

        // get user from previous activity
        idUser = getIntent().getIntExtra("id", 0);
        imageFile = getIntent().getStringExtra("image");
        firstName = getIntent().getStringExtra("first");
        lastName = getIntent().getStringExtra("last");


        location = new Location("");
        //32.52239863, -117.01884061
        location.setLatitude(32.462516);
        location.setLongitude(-116.821192);

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
