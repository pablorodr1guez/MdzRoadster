package com.example.uberclone.activities.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uberclone.R;
import com.example.uberclone.activities.client.DetailRequestActivity;
import com.example.uberclone.activities.client.RequestDriver;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.models.ClientBooking;
import com.example.uberclone.models.FCMBody;
import com.example.uberclone.models.FCMResponse;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientBookingProvider;
import com.example.uberclone.providers.ClientProvider;
import com.example.uberclone.providers.GeoFirebaseProvider;
import com.example.uberclone.providers.GoogleApiProvider;
import com.example.uberclone.providers.NotificationProvider;
import com.example.uberclone.providers.TokenProvider;
import com.example.uberclone.utils.DecodePoints;
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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDriverBooking extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeoFirebaseProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private ClientProvider mClientProvider;
    private ClientBookingProvider mClientBookingProvider;

    private NotificationProvider mNotificationProvider;


    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;


    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;
    private LatLng mCurrentLatLng;

    private ImageView mImageViewBooking;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;

    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;

    private String mExtraClientId;

    private boolean mIsFirstTime = true;

    private boolean mIsCloseToClient = false;

    private Button mButtonStartBooking;
    private Button mButtonFinishBooking;




    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location: locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMarker != null) {
                        mMarker.remove();
                    }

                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                    );
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    updateLocation();


                    if(mIsFirstTime){
                        mIsFirstTime = false;
                        getClientBooking();
                    }

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);

        mButtonStartBooking.setEnabled(false);

        mNotificationProvider = new NotificationProvider();

        mImageViewBooking = findViewById(R.id.ImageViewClientBooking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeoFirebaseProvider("drivers_working");
        mTokenProvider = new TokenProvider();
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mTextViewClientBooking = findViewById(R.id.TextViewClientBooking);
        mTextViewEmailClientBooking = findViewById(R.id.TextViewEmailClientBooking);
        mTextViewOriginClientBooking = findViewById(R.id.TextViewOriginClientBooking);
        mTextViewDestinationClientBooking= findViewById(R.id.textViewDestinationClientBooking);
        mExtraClientId = getIntent().getStringExtra("idClient");

        mButtonStartBooking = findViewById(R.id.btn_startBooking);
        mButtonFinishBooking = findViewById(R.id.btn_finishBooking);

        mGoogleApiProvider = new GoogleApiProvider(MapDriverBooking.this);


        getClient();


        mButtonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(mIsCloseToClient){

                    startBooking();
                }else{

                    Toast.makeText(MapDriverBooking.this, "Debes estar mas cerca del cliente", Toast.LENGTH_SHORT).show();
                }
                

            }
        });
        
        
        mButtonFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                finishBooking();
            }
        });







    }

    private void finishBooking() {
        mClientBookingProvider.updateStatus(mExtraClientId,"finish");

        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId);
        Intent intent = new Intent(MapDriverBooking.this,CalificationClient.class);
        sendNotification("Finalizado");
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        if(mFusedLocation!= null){

            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }

        intent.putExtra("idClient", mExtraClientId);

        startActivity(intent);


        finish();
    }

    private void startBooking() {

        mClientBookingProvider.updateStatus(mExtraClientId,"start");
        mButtonStartBooking.setVisibility(View.GONE);
        mButtonFinishBooking.setVisibility(View.VISIBLE);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_red)));

        drawRoute(mDestinationLatLng);
        sendNotification("Iniciado");

    }

    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng){

        double distance = 0;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);

        distance = clientLocation.distanceTo(driverLocation);

        return distance;



    }


    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String destination = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();

                    double destinationLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat,originLng);
                    mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                    mTextViewOriginClientBooking.setText("Recoger en: "+origin);
                    mTextViewDestinationClientBooking.setText("Destino: "+destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Cliente").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_red)));

                    drawRoute(mOriginLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void drawRoute(LatLng latLng){


        mGoogleApiProvider.getDirections(mCurrentLatLng,latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try{

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolyLineList = DecodePoints.decodePoly(points);
                    mPolyLineOptions = new PolylineOptions();
                    mPolyLineOptions.color(Color.RED);
                    mPolyLineOptions.width(13f);
                    mPolyLineOptions.startCap(new SquareCap());
                    mPolyLineOptions.jointType(JointType.ROUND);
                    mPolyLineOptions.addAll(mPolyLineList);
                    mMap.addPolyline(mPolyLineOptions);


                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");

                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");


                }catch (Exception e){
                    Log.d("Error","Error encontrado: " + e.getMessage());
                }


            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });




    }


    private void getClient() {
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String email = dataSnapshot.child("email").getValue().toString();

                    String name = dataSnapshot.child("name").getValue().toString();

                    String image = "";

                    if(dataSnapshot.hasChild("image")){

                        image = dataSnapshot.child("image").getValue().toString();

                        Picasso.with(MapDriverBooking.this).load(image).into(mImageViewBooking);
                    }

                    mTextViewClientBooking.setText(name);
                    mTextViewEmailClientBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);

            if(!mIsCloseToClient){


                if(mOriginLatLng!= null && mCurrentLatLng != null){

                    double distance = getDistanceBetween(mOriginLatLng,mCurrentLatLng);

                    if(distance <= 200){

                        mButtonStartBooking.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Estas cerca del cliente", Toast.LENGTH_SHORT).show();
                    }

                }





            }


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(false);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }
                    else {
                        showAlertDialogNOGPS();
                    }
                }
                else {
                    checkLocationPermissions();
                }
            }
            else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived())  {
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void disconnect() {

        if (mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }
        }
        else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {

                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapDriverBooking.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapDriverBooking.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void sendNotification(final String status) {

        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String,String> map = new HashMap<>();
                    map.put("title","Estado de tu viaje");
                    map.put("body","Tu estado del viaje es:"+ status );

                    FCMBody fcmBody = new FCMBody(token,"high","4500s",map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                            if(response.body()!= null){

                                if(response.body().getSuccess()!=1) {

                                    Toast.makeText(MapDriverBooking.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();


                                }

                            }else{
                                Toast.makeText(MapDriverBooking.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                            Log.d("Error","error" + t.getMessage());

                        }
                    });

                }else{
                    Toast.makeText(MapDriverBooking.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }
}
