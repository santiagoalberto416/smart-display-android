package com.skirk.smartdisplay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.skirk.smartdisplay.POJO.UserResponse;
import com.skirk.smartdisplay.interfaces.UserResponseInterface;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterUserActivity extends AppCompatActivity {

    ProgressDialog prgDialog;
    String encodedString;
    RequestParams params = new RequestParams();
    String imgPath, fileName;
    Bitmap bitmap;
    private static int RESULT_LOAD_IMG = 1;
    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1212;



    public AutoCompleteTextView editFirstName;
    public AutoCompleteTextView editLastName;
    public AutoCompleteTextView editEmail;
    public AutoCompleteTextView editPassword;

    public String firstname;
    public String lastname;
    public String email;
    public String image;
    public String password;
    public int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        prgDialog = new ProgressDialog(this);
        insertFlowEditText();
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(RegisterUserActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {

            }
        }

    }

    public void insertFlowEditText(){
        editFirstName = (AutoCompleteTextView)findViewById(R.id.firstNameText);
        editLastName = (AutoCompleteTextView)findViewById(R.id.lastNameText);
        editEmail = (AutoCompleteTextView)findViewById(R.id.email);
        editPassword = (AutoCompleteTextView)findViewById(R.id.passwordText);

        editFirstName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_NEXT) {
                    editLastName.requestFocus();
                    return false;
                }
                return false;
            }
        });
        editLastName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_NEXT) {
                    editEmail.requestFocus();
                    return false;
                }
                return false;
            }
        });
        editEmail.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_NEXT) {
                    editPassword.requestFocus();
                    return false;
                }
                return false;
            }
        });
        editPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_NEXT) {
                    return false;
                }
                return false;
            }
        });
    }

    public void checkValues(View view){
        Boolean success = true;
        if(editFirstName.getText().length() == 0){
            editFirstName.setError(getString(R.string.error_invalid_first));
            editFirstName.requestFocus();
            success = false;
        }
        if(editLastName.getText().length() == 0){
            editLastName.setError(getString(R.string.error_invalid_last));
            editLastName.requestFocus();
            success = false;
        }
        if(editEmail.getText().length() == 0 && isEmailValid(editEmail.getText().toString())){
            editEmail.setError(getString(R.string.error_invalid_email));
            editEmail.requestFocus();
            success = false;
        }
        if(editPassword.getText().length() == 0){
            editPassword.setError(getString(R.string.error_incorrect_password));
            editPassword.requestFocus();
            success = false;
        }
        if(success){
            firstname = editFirstName.getText().toString();
            lastname = editLastName.getText().toString();
            email = editEmail.getText().toString();
            password = editPassword.getText().toString();
            createUser();
        }


    }

    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public void createUser(){
        prgDialog.setMessage("Registering user...");
        prgDialog.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LoginActivity.URL_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserResponseInterface apiService =
                retrofit.create(UserResponseInterface.class);
        Call<UserResponse> call = apiService.createUser(firstname, lastname, firstname+".png", password, email);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.body().getStatus() == 0) {
                    id = response.body().getId();
                    image = response.body().getId() + ".jpg";
                    params.put("iduser", image);
                    RegisterUserActivity.this.uploadImage(null);
                }else if(response.body().getStatus() == 1){
                    RegisterUserActivity.this.editEmail.setError(getString(R.string.error_user_invalid));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.d("response", "fail");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        // Here, thisActivity is the current activity

    }

    // When Image is selected from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.imgView);
                // Set the Image in ImageView
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgPath));
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");

                fileName = fileNameSegments[fileNameSegments.length - 1];
                //fileName = image;


                // Put file name in Async Http Post Param which will used in Php web app
                params.put("filename", fileName);

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    // When Upload button is clicked
    public void uploadImage(View v) {
        // When Image is selected from Gallery
        if (imgPath != null && !imgPath.isEmpty()) {
            prgDialog.setMessage("Converting Image to Binary Data");



            // Convert image to String using Base64
            encodeImagetoString();
            // When Image is not selected from Gallery
        } else {
            prgDialog.dismiss();
            goToMain();
        }
    }

    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = BitmapFactory.decodeFile(imgPath,
                        options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.setMessage("Calling Upload");
                // Put converted Image string into Async Http Post param
                params.put("image", encodedString);
                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload() {
        makeHTTPCall();
    }

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        prgDialog.setMessage("Invoking Php");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(LoginActivity.URL_SERVER+ "upload_image.php",
                params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                        prgDialog.hide();
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(),
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error Occured n Most Common Error: n1. Device not connected to Internetn2. Web App is not deployed in App servern3. App server is not runningn HTTP Status code : "
                                            + statusCode, Toast.LENGTH_LONG)
                                    .show();
                            String response = new String(responseBody, StandardCharsets.UTF_8);
                            Log.d("Message", responseBody + "");
                        }
                        goToMain();
                    }
                    // When the response returned by REST has Http
                    // response code '200'

                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                        String response = new String(responseBody, StandardCharsets.UTF_8);
                        prgDialog.hide();
                        Toast.makeText(getApplicationContext(), response,
                                Toast.LENGTH_LONG).show();
                        goToMain();
                    }

                });
    }

    public void goToMain(){
        Intent intent= new Intent(RegisterUserActivity.this, MainActivity.class);
        intent.putExtra("idUser", id);
        intent.putExtra("first", firstname);
        intent.putExtra("last", lastname);
        intent.putExtra("image", image);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }
}
