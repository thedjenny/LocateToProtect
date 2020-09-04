package test.tuto_passport;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.JsonArray;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import test.tuto_passport.entities.AccessToken;
import test.tuto_passport.entities.User;
import test.tuto_passport.entities.UserResponse;
import test.tuto_passport.entities.UuidResponse;
import test.tuto_passport.network.ApiService;
import test.tuto_passport.network.RetrofitBuilder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , EasyPermissions.PermissionCallbacks {
    private static final String[] LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int RC_LOCATION_CONTACTS_PERM = 124;
    private static final String TAG = "MainActivity";

    ImageView display;
    private String username;
    private String myPrivateKey;
    private List<BeaconTransmitter> beaconList  = new ArrayList<>();
    private List<Long> list = new ArrayList<>();

    TextView uuid;
    static  String uuid2;
    ApiService service;
    TokenManager tokenManager;
    Call<AccessToken> call;
    Call<Boolean> logoutCall;
    Call<UuidResponse> UuidGenerator;
    ImageView user_photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        if(tokenManager.getToken() == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        ImageView findme = (ImageView) findViewById(R.id.findme);
        ImageView findUser = (ImageView) findViewById(R.id.finduser);
        ImageView setting = (ImageView) findViewById(R.id.setting);
        ImageView contact = (ImageView) findViewById(R.id.contact);
        ImageView review = (ImageView) findViewById(R.id.review);
        ImageView logout = (ImageView) findViewById(R.id.logout);
        TextView username = (TextView) findViewById(R.id.userName);
        user_photo = (ImageView) findViewById(R.id.user_photo);
        findme.setOnClickListener(this);
        findUser.setOnClickListener(this);
        setting.setOnClickListener(this);
        contact.setOnClickListener(this);
        review.setOnClickListener(this);
        logout.setOnClickListener(this);

        /*
        Starting the backend Beacons code
         */

        if(!hasLocationAndContactsPermissions()){
            locationAndContactsTask();
        }

        if(!isBluetoothEnabled()){
                enableBluetooth();
                Toast.makeText(getApplicationContext(), "Enabling Bluetooth", Toast.LENGTH_SHORT).show();

            }

        if (checkLocationServices()){
            //uuid
            UuidGenerator = service.getUUID();
            UuidGenerator.enqueue(new Callback<UuidResponse>() {

                @Override
                public void onResponse(Call<UuidResponse> call, Response<UuidResponse> response) {

                    Log.w(TAG, "onResponse: " + response );
                    uuid2 = response.body().getData().get(0).toString();
                    startTransmit(uuid2);

                    Log.i("UUID2" , uuid2.toString());


                    //Username
                    Call<String> callForUsername = service.getCurrentUser(uuid2);
                    callForUsername.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            Log.i("username haho",response.body());


                            if(response.isSuccessful()){
                                username.setText("Welcome "+ response.body());
                            }



                            //username.setText();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.i("makanch ",t.toString());
                            Toast.makeText(MainActivity.this, "Error occured for username", Toast.LENGTH_SHORT).show();

                        }
                    });
                    //Image
                    Call<String> callForFirebaseImg = service.getCurrentFirebaseImg(uuid2);
                    callForFirebaseImg.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.isSuccessful()){
                                Log.i("img recu",response.body());
                                LoadImage loadImage = new LoadImage();
                                loadImage.execute(response.body());

                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                                Log.i("img failure",t.toString());
                        }
                    });
                    }

                @Override
                public void onFailure(Call<UuidResponse> call, Throwable t) {
                    System.out.println("erreur comm");
                    Log.w(TAG, "onResponse: " + t );
                }
            });

                }else{
                    // print msg error
                }


        }



    private void startTransmit(String uuid) {

        beaconList.clear();

        generateBeacon(uuid, "99" , "1");
    }





    private void generateBeacon(String UUID, String major, String minor) {
        Beacon beacon = new Beacon.Builder()
                .setId1(UUID)
                .setId2(major)
                .setId3(minor)
                .setManufacturer(0x004C)
                .build();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        beaconTransmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);



        if(beaconList.size()<1){
            beaconList.add(beaconTransmitter);
            beaconTransmitter.startAdvertising(beacon);}

    }



/*
private void generateBeacon2(String UUID, String major , String minor){

    BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(),new BeaconParser().setBeaconLayout());

    Beacon beacon = new Beacon.Builder()
            .setId1(UUID)
            .build();

   Log.i("beacon type : ", beacon.getId1().toString()) ;
    beaconTransmitter.startAdvertising(beacon);


}*/
    private boolean hasLocationAndContactsPermissions() {
        return EasyPermissions.hasPermissions(this, LOCATION);
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable :)
            return false;
        } else {
            return true;
        }

    }

    public static void enableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    public Boolean checkLocationServices() {
        try {

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                settingsRequest();
                return false;
            } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void settingsRequest() {

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this, 99);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.findme:{

                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);

                System.out.println("find me button");
            }break;
            case R.id.finduser:{
                System.out.println("find user button");
            }break;
            case R.id.setting:{
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
               
            }break;
            case R.id.review:{
                System.out.println("review button");
            }break;
            case R.id.contact:{
                System.out.println("contact button");
            }break;
            case R.id.logout:{
                this.confirmLogOutDecision();
            }break;

        }

    }


   /* @OnClick(R.id.btn_posts)
    void getPosts(){

        call = service.posts();
        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                Log.w(TAG, "onResponse: " + response );

                if(response.isSuccessful()){
                    title.setText(response.body().getData().get(0).getTitle());
                }else {
                    tokenManager.deleteToken();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();

                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });

    }*/


    @AfterPermissionGranted(RC_LOCATION_CONTACTS_PERM)
    public void locationAndContactsTask() {
        if (!hasLocationAndContactsPermissions()) {
            EasyPermissions.requestPermissions(this, "the app need to access to you location", RC_LOCATION_CONTACTS_PERM, LOCATION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(call != null){
            call.cancel();
            call = null;
        }
        beaconList.get(0).stopAdvertising();
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @OnClick
    public void confirmLogOutDecision(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm logout !");
        builder.setMessage("You are about to logout . Do you really want to proceed ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                logoutCall =  service.logout(tokenManager.getToken().getAccessToken());
                tokenManager.deleteToken();
                Toast.makeText(MainActivity.this, "logout button", Toast.LENGTH_SHORT).show();

                System.out.println("=====================================");
                System.out.println(tokenManager.getToken().getAccessToken());

                beaconList.get(0).stopAdvertising();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Action cancelled !", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    private class LoadImage  extends AsyncTask<String , Void , Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];
            Bitmap bitmap = null;
            try {
                InputStream inputStream = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        return  bitmap;}

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            user_photo.setImageBitmap(bitmap);
        }
    }
}






