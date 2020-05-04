package com.example.uberclone.activities.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.uberclone.R;
import com.example.uberclone.activities.MainActivity;
import com.example.uberclone.activities.client.HistoryBookingClient;
import com.example.uberclone.activities.client.MapClient;
import com.example.uberclone.activities.client.UpdateProfile;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.GeoFirebaseProvider;
import com.example.uberclone.providers.TokenProvider;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MapDriver extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    AuthProvider mAuthProvider;
    private GeoFirebaseProvider mGeofireProvider;
    private TokenProvider mTokenProvider;

    LocationRequest mlocationRequest = new LocationRequest();
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Button mButtonConnectD;
    boolean isConnect = false;

    private LatLng mCurrentLatLng;


    private ValueEventListener mListener;


    Marker mMarker;

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){

            for(Location location: locationResult.getLocations()){

                if(getApplicationContext()!= null ){


                    mCurrentLatLng = new LatLng(location.getLatitude(),location.getLongitude());

                    if(mMarker != null){

                        mMarker.remove();

                    }


                    mMarker = mMap.addMarker(new MarkerOptions().position(

                            new LatLng(location.getLatitude(),location.getLongitude())
                            )
                            .title("Tu posicion")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    );

                    //obtener la localizacion en tiempo real

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(

                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(),location.getLongitude()))
                                    .zoom(15f)
                                    .build()

                    ));


                    updateLocation();


                }

            }


        }
    };


    private void updateLocation(){


        if(mAuthProvider.existSession() && mCurrentLatLng != null){

            mGeofireProvider.saveLocation(mAuthProvider.getId(),mCurrentLatLng);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);

        Mytoolbar.show(this,"Mapa del conductor",false);
        mAuthProvider = new AuthProvider();

        mGeofireProvider = new GeoFirebaseProvider("active_drivers");
        mButtonConnectD = findViewById(R.id.btn_connectD);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);


        mTokenProvider = new TokenProvider();
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mButtonConnectD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isConnect == true){

                    Toast.makeText(MapDriver.this, "GPS DESCONECTADO", Toast.LENGTH_SHORT).show();

                    disconnect();
                }else{


                    //Toast.makeText(MapDriver.this, "GPS CONECTADO", Toast.LENGTH_SHORT).show();


                    startLocation();
                }



            }
        });


        generateToken();
        
        isDriverWorking();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mListener != null){

            mGeofireProvider.isDriverWorking(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }

    private void isDriverWorking() {

         mListener = mGeofireProvider.isDriverWorking(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    disconnect();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(false);

        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(5);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode== LOCATION_REQUEST_CODE){

            if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                    if(gpsActive()){

                        mFusedLocation.requestLocationUpdates(mlocationRequest,mLocationCallback, Looper.myLooper());

                    }else{

                        showAlertDialogNoGps();
                    }


                }else{

                    checkLocationPermissions();
                }

            }else{

                checkLocationPermissions();
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SETTINGS_REQUEST_CODE && gpsActive()){

            mFusedLocation.requestLocationUpdates(mlocationRequest,mLocationCallback, Looper.myLooper());

        }else{

            showAlertDialogNoGps();
        }
    }

    private void showAlertDialogNoGps(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setMessage("Por activa tu GPS para continuar")
        .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);

            }
        }).create().show();


    }

    private boolean gpsActive(){

        boolean isActive = false;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            isActive = true;

        }

        return isActive;

    }




    private void disconnect(){

        if(mFusedLocation != null){

            mButtonConnectD.setText("Conectarse");
            isConnect = false;
            mFusedLocation.removeLocationUpdates(mLocationCallback);

            if(mAuthProvider.existSession()){

                mGeofireProvider.removeLocation(mAuthProvider.getId());

            }

        }else{


            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }





    }

    private void startLocation(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                if(gpsActive()){


                    mButtonConnectD.setText("Desconectarse");
                    isConnect = true;

                    mFusedLocation.requestLocationUpdates(mlocationRequest,mLocationCallback, Looper.myLooper());

                }else{

                    showAlertDialogNoGps();
                }

            }else{
                checkLocationPermissions();
            }

        }else{


            if(gpsActive()){

                mButtonConnectD.setText("Desconectarse");
                isConnect = true;

                mFusedLocation.requestLocationUpdates(mlocationRequest,mLocationCallback, Looper.myLooper());
            }else{

                showAlertDialogNoGps();
            }

        }

    }

    private void checkLocationPermissions(){


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                ActivityCompat.requestPermissions(MapDriver.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);

                            }
                        })

                        .create()
                        .show();

            }else{

                ActivityCompat.requestPermissions(MapDriver.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }


        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.drivermenu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_logout){

            logout();

        }

        if(item.getItemId() == R.id.action_update){

            Intent intent = new Intent(MapDriver.this, UpdateProfileDriver.class);
            startActivity(intent);

        }

        if(item.getItemId() == R.id.action_history){

            Intent intent = new Intent(MapDriver.this, HistoryBookingDriver.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    public void logout(){

        disconnect();

        mAuthProvider.logout();

        Intent intent = new Intent(MapDriver.this, MainActivity.class);
        startActivity(intent);
        finish();


    }

    public void generateToken(){

        mTokenProvider.create(mAuthProvider.getId());




    }


}
