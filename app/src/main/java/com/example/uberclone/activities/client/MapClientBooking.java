package com.example.uberclone.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.uberclone.R;
import com.example.uberclone.activities.driver.CalificationClient;
import com.example.uberclone.activities.driver.MapDriverBooking;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientBookingProvider;
import com.example.uberclone.providers.DriverProvider;
import com.example.uberclone.providers.GeoFirebaseProvider;
import com.example.uberclone.providers.GoogleApiProvider;
import com.example.uberclone.providers.TokenProvider;
import com.example.uberclone.utils.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapClientBooking extends AppCompatActivity implements OnMapReadyCallback {

    AuthProvider mAuthProvider;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private GeoFirebaseProvider mGeoFirebaseProvider;

    private boolean mIsFirstTime = true;

    Marker mMarkerDriver;

    private PlacesClient mPlaces;

    private String mOrigin;
    private LatLng mOriginLatLng;
    private String mDestination;
    private LatLng mDestinationLatLng;

    private LatLng mDriverLatLng;
    private String mIdDriver;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;

    private ImageView mImageViewBooking;

    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;
    private TextView mTextViewStatusBooking;

    private TokenProvider mTokenProvider;


    private ClientBookingProvider mClientBookingProvider;
    private DriverProvider mDriverProvider;
    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private ValueEventListener mListener;
    private ValueEventListener mListenerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);


        mImageViewBooking = findViewById(R.id.ImageViewClientBooking);

        mAuthProvider = new AuthProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientBooking.this);
        mDriverProvider = new DriverProvider();
        mGeoFirebaseProvider = new GeoFirebaseProvider("drivers_working");
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_client);
        mMapFragment.getMapAsync(this);
        mTokenProvider = new TokenProvider();




        if(!Places.isInitialized()){

            Places.initialize(getApplicationContext(),getResources().getString(R.string.google_maps_key));

        }


        mTextViewClientBooking = findViewById(R.id.TextViewDriverBooking);
        mTextViewEmailClientBooking = findViewById(R.id.TextViewEmailDriverBooking);
        mTextViewOriginClientBooking = findViewById(R.id.TextViewOriginClientBooking);
        mTextViewDestinationClientBooking= findViewById(R.id.textViewDestinationClientBooking);
        mTextViewStatusBooking = findViewById(R.id.textViewStatusBooking);
        
        
        getStatus();
        getClientBooking();
    }

    private void getStatus() {

        ValueEventListener mListenerStatus = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue().toString();

                    if(status.equals("accept")){
                        mTextViewStatusBooking.setText("Aceptado");

                    }

                    if (status.equals("start")) {
                        mTextViewStatusBooking.setText("Iniciado");

                        startBooking();
                    } else if (status.equals("finish")) {
                        mTextViewStatusBooking.setText("finalizado");

                        finishBooking();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startBooking(){

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_red)));
        drawRoute(mDestinationLatLng);
    }

    private  void finishBooking(){

        Intent intent = new Intent(MapClientBooking.this, CalificationClient.class);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener!=null){

            mGeoFirebaseProvider.getDriverLocation(mIdDriver).removeEventListener(mListener);

        }

        if(mListenerStatus != null){

            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListenerStatus);

        }
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String destination = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    String idDriver =dataSnapshot.child("idDriver").getValue().toString();
                    double destinationLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());
                    mIdDriver = idDriver;
                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat,originLng);
                    mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                    mTextViewOriginClientBooking.setText("Recoger en: "+origin);
                    mTextViewDestinationClientBooking.setText("Destino: "+destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Cliente").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_red)));

                    getDriver(idDriver);
                    getDriverLocation(idDriver);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDriver(String idDriver) {

        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String image = "";

                    if(dataSnapshot.hasChild("image")){

                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(MapClientBooking.this).load(image).into(mImageViewBooking);
                    }


                    mTextViewEmailClientBooking.setText(email);
                    mTextViewClientBooking.setText(name);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDriverLocation(String idDriver) {

        mListener = mGeoFirebaseProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    double lat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                    mDriverLatLng = new LatLng(lat,lng);
                    if(mMarkerDriver!=null){

                        mMarkerDriver.remove();
                    }
                    mMarkerDriver = mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng))
                                    .title("Tu conductor")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));


                    if(mIsFirstTime){
                        mIsFirstTime = false;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(

                                new CameraPosition.Builder().target(mDriverLatLng).zoom(14f).build()

                        ));
                        drawRoute(mOriginLatLng);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void drawRoute(LatLng latLng){


        mGoogleApiProvider.getDirections(mDriverLatLng,latLng).enqueue(new Callback<String>() {
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




    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);



    }
}
