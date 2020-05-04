package com.example.uberclone.activities.client;

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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.uberclone.R;
import com.example.uberclone.activities.MainActivity;
import com.example.uberclone.activities.driver.MapDriver;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.GeoFirebaseProvider;
import com.example.uberclone.providers.TokenProvider;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapClient extends AppCompatActivity implements OnMapReadyCallback {
    AuthProvider mAuthProvider;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    LocationRequest mlocationRequest = new LocationRequest();
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private GeoFirebaseProvider mGeoFirebaseProvider;

    private boolean mIsFirstTime = true;

    private LatLng mCurrentLatLng;

    private List<Marker> mDriversMarkers = new ArrayList<>();
    Marker mMarker;

    private AutocompleteSupportFragment mAutoComplete;
    private AutocompleteSupportFragment mAutoCompleteDestination;
    private PlacesClient mPlaces;

    private String mOrigin;
    private LatLng mOriginLatLng;
    private String mDestination;
    private LatLng mDestinationLatLng;

    private GoogleMap.OnCameraIdleListener mCameraListener;

    private Button mButtonRequestDriver;

    private TokenProvider mTokenProvider;

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){

            for(Location location: locationResult.getLocations()){

                if(getApplicationContext()!= null ){

                    mCurrentLatLng = new LatLng(location.getLatitude(),location.getLongitude());


                    //obtener la localizacion en tiempo real

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(

                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(),location.getLongitude()))
                                    .zoom(15f)
                                    .build()

                    ));

                    if(mIsFirstTime){
                        mIsFirstTime = false;
                        getActiveDrivers();

                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);

        Mytoolbar.show(this,"Mapa del cliente",false);
        mAuthProvider = new AuthProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mGeoFirebaseProvider = new GeoFirebaseProvider("active_drivers");
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_client);
        mMapFragment.getMapAsync(this);
        mTokenProvider = new TokenProvider();

        mButtonRequestDriver = findViewById(R.id.btn_requestDriver);

        if(!Places.isInitialized()){

            Places.initialize(getApplicationContext(),getResources().getString(R.string.google_maps_key));

        }

        mPlaces = Places.createClient(this);
        instanceAutoCompleteOrigin();
        instanceAutoCompleteDestination();
        onCameraMove();

        mButtonRequestDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestDriver();

            }
        });

        generateToken();

    }

    private void requestDriver(){

        if(mOriginLatLng != null && mDestinationLatLng != null){

            Intent intent = new Intent(MapClient.this,DetailRequestActivity.class);
            intent.putExtra("originLat",mOriginLatLng.latitude);
            intent.putExtra("originLng",mOriginLatLng.longitude);

            intent.putExtra("DestinationLat",mDestinationLatLng.latitude);
            intent.putExtra("DestinationLng",mDestinationLatLng.longitude);


            intent.putExtra("origin",mOrigin);
            intent.putExtra("destination",mDestination);


            startActivity(intent);



        }else{

            Toast.makeText(this, "Debe seleccionar el lugar de origen y el lugar de destino", Toast.LENGTH_SHORT).show();
        }


    }


    private void instanceAutoCompleteOrigin(){


        mAutoComplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigin);
        mAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        mAutoComplete.setHint("Origen");
        mAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {


                mOrigin = place.getName();
                mOriginLatLng = place.getLatLng();
                Log.d("PLACE","Name: "+ mOrigin);
                Log.d("PLACE","Lat: "+ mOriginLatLng.latitude);
                Log.d("PLACE","Long: "+ mOriginLatLng.longitude);


            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });



    }

    private void onCameraMove(){

        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                try{

                    Geocoder geocoder = new Geocoder(MapClient.this);

                    mOriginLatLng = mMap.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mOriginLatLng.latitude,mOriginLatLng.longitude,1);
                    String city = addressList.get(0).getLocality();
                    String country = addressList.get(0).getCountryName();
                    String address = addressList.get(0).getAddressLine(0);

                    mOrigin = address +" "+ city;
                    mAutoComplete.setText(address + " "+ city);


                }catch (Exception e){

                    Log.d("Error: ","Mensaje error: "+ e.getMessage());


                }

            }
        };
    }

    private void instanceAutoCompleteDestination(){

        mAutoCompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestination);
        mAutoCompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        mAutoCompleteDestination.setHint("Destino");
        mAutoCompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {


                mDestination = place.getName();
                mDestinationLatLng = place.getLatLng();
                Log.d("PLACE","Name: "+ mDestination);
                Log.d("PLACE","Lat: "+ mDestinationLatLng.latitude);
                Log.d("PLACE","Long: "+ mDestinationLatLng.longitude);


            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });




    }

    private void getActiveDrivers(){

        mGeoFirebaseProvider.getActiveDrivers(mCurrentLatLng,10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //Se añanden los marcadores de los conductores


                for(Marker marker: mDriversMarkers){


                    if(marker.getTag()!=null){

                        if(marker.getTag().equals(key)){

                            return;
                        }
                    }
                }

                LatLng driverLatting = new LatLng(location.latitude,location.longitude);

                Marker marker = mMap.addMarker(new MarkerOptions().position(driverLatting).title("Conductor disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                marker.setTag(key);

                mDriversMarkers.add(marker);

            }

            @Override
            public void onKeyExited(String key) {


                for(Marker marker: mDriversMarkers){
                    if(marker.getTag()!=null){
                        if(marker.getTag().equals(key)){

                            marker.remove();
                            mDriversMarkers.remove(marker);

                            return;
                        }
                    }
                }

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

                // actualiza la pocision de los conductores

                for(Marker marker: mDriversMarkers){


                    if(marker.getTag()!=null){

                        if(marker.getTag().equals(key)){

                            marker.setPosition(new LatLng(location.latitude,location.longitude));

                            return;
                        }
                    }
                }



            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(mCameraListener);

        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(5);


        startLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode== LOCATION_REQUEST_CODE){

            if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                    if(gpsActive()){

                        mFusedLocation.requestLocationUpdates(mlocationRequest,mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
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
            mMap.setMyLocationEnabled(true);
        }else if(requestCode == SETTINGS_REQUEST_CODE && !gpsActive()){

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

    private void startLocation(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                if(gpsActive()){

                    mFusedLocation.requestLocationUpdates(mlocationRequest,mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }else{

                    showAlertDialogNoGps();
                }

            }else{
                checkLocationPermissions();
            }

        }else{


            if(gpsActive()){

                mFusedLocation.requestLocationUpdates(mlocationRequest,mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
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

                                ActivityCompat.requestPermissions(MapClient.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);

                            }
                        })

                        .create()
                        .show();

            }else{

                ActivityCompat.requestPermissions(MapClient.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.clientmenu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_logout){

            logout();

        }

        if(item.getItemId() == R.id.action_update){

            Intent intent = new Intent(MapClient.this,UpdateProfile.class);
            startActivity(intent);

        }

        if(item.getItemId() == R.id.action_history){

            Intent intent = new Intent(MapClient.this,HistoryBookingClient.class);
            startActivity(intent);

        }




        return super.onOptionsItemSelected(item);
    }

    public void logout(){

        mAuthProvider.logout();
        Intent intent = new Intent(MapClient.this, MainActivity.class);
        startActivity(intent);
        finish();


    }


    public void generateToken(){

        mTokenProvider.create(mAuthProvider.getId());




    }
}



