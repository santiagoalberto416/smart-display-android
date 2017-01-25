package com.skirk.smartdisplay;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.skirk.smartdisplay.POJO.SmartLocation;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.skirk.smartdisplay.LoginActivity.readStream;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;


public class MainActivity extends AppCompatActivity{

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute

    private static final String SOCKET_URL = "https://croissant-santy-ruler.c9users.io:8081";

    TextView latitude;
    TextView longittude;
    Location location;
    LocationTask locationTask = null;

    List<SmartLocation> smartLocations = new ArrayList<>();
    private List<Integer> idSmartAlready = new ArrayList<>();

    private String firstName = "";
    private String lastName = "";
    private String imageFile = "";
    private String email = "";
    private int idUser = 0;

    private TextView txtfullName;
    private TextView txtEmail;
    private ImageView imageOfUser;
    private TextView screensNear;
    private TextView distance;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(SOCKET_URL);
        } catch (URISyntaxException e) {
            Log.d("Message", e.getMessage());
        }
    }


    private LocationManager locationManager = null;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here

            Log.d("--this location", location.getLatitude()+","+location.getLongitude());
            if(smartLocations.size()>0){
                int screensNear = 0;
                for(SmartLocation i : smartLocations){
                    float distanceInMeters = i.getLocation().distanceTo(location);
                    Log.d("--Location", i.toString());
                    Log.d("-Distance to", distanceInMeters+" ");

                    if(distanceInMeters<=10 && !idSmartAlready.contains(i.getId())){
                        idSmartAlready.add(i.getId());
                        String imageJson = "{" +
                                "\"firstname\": \""+firstName+" "+lastName+"\"," +
                                "\"imageurl\": \""+idUser+".jpg\"}";
                        mSocket.emit("showUserImage", imageJson);
                        makeNotification();
                        makeDialog();
                    }else if(distanceInMeters<=10){
                        mSocket.emit("removeAction", "{\"locationid\": "+i.getId()+"}");
                        distance.setText(distanceInMeters+"");
                        screensNear = screensNear + 1;
                    }

                }
                if(screensNear>0) {
                    MainActivity.this.screensNear.setText("Tienes " + screensNear + " SmartScreen(s) cerca");
                }else {
                    MainActivity.this.screensNear.setText("Buscando Smart Displays");
                }
                /// put screens near

            }


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){

        }

        @Override
        public void onProviderEnabled(String provider){

        }

        @Override
        public void onProviderDisabled(String provider){

        }

        public void makeNotification(){
            Notification n  = new Notification.Builder(MainActivity.this)
                    .setContentTitle("New mail from " + "test@gmail.com")
                    .setContentText("Subject")
                    .setSmallIcon(R.drawable.ic_location_on_black_48dp)
                    .setAutoCancel(true).build();
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, n);
        }

        public void makeDialog(){
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_dialog);
            dialog.setTitle("Title...");
            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSocket.connect();

        txtfullName = (TextView) findViewById(R.id.userNameComplete);
        txtEmail = (TextView) findViewById(R.id.email);
        imageOfUser = (ImageView) findViewById(R.id.imageUser);
        screensNear = (TextView) findViewById(R.id.screensNear);
        distance = (TextView) findViewById(R.id.distance);

        // get user from previous activity
        idUser = getIntent().getIntExtra("idUser", 0);
        imageFile = getIntent().getStringExtra("image");
        firstName = getIntent().getStringExtra("first");
        lastName = getIntent().getStringExtra("last");
        email = getIntent().getStringExtra("email");

        txtfullName.setText(firstName + " " +lastName);
        txtEmail.setText(email);

        Picasso.with(this).load(LoginActivity.URL_SERVER+"user-images/"+idUser+".jpg").into(imageOfUser);

        location = new Location("");
        location.setLatitude(32.462516);
        location.setLongitude(-116.821192);

        latitude = (TextView)this.findViewById(R.id.lattitude);
        longittude = (TextView)this.findViewById(R.id.longitude);

        locationTask = new LocationTask();
        locationTask.execute(LoginActivity.URL_SERVER+"classes/getLocations.php");

    }

    public class LocationTask extends AsyncTask<String, String, String> {



        LocationTask() {
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
            String server_response = "";
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.close();
                conn.connect();
                int responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    server_response = readStream(conn.getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return server_response;

        }

        @Override
        protected void onPostExecute(final String json) {
            JSONObject jsonObject;
            Log.d("json",json);
            try {
                jsonObject = new JSONObject(json);
                if(jsonObject.getInt("status")==0){
                    JSONArray locations = jsonObject.getJSONArray("locations");
                    for (int i = 0; i < locations.length(); i++) {
                        JSONObject row = locations.getJSONObject(i);
                        smartLocations.add(new SmartLocation(row));
                        Log.d("add location", "added new locations");
                    }
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                    }catch (SecurityException ex){
                        Log.d("Security Error", ex.getMessage());
                    }


                }else {
                    ///error finish activity
                }
            }catch (JSONException ex){
                ///error finish activity
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            locationManager.removeUpdates(mLocationListener);
            locationManager = null;
        }catch (SecurityException ex){
            Log.d("Security exception",ex.getMessage());
        }

    }







}
