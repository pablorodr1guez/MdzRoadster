package com.example.uberclone.activities.client;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.uberclone.R;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.providers.GoogleApiProvider;
import com.example.uberclone.utils.DecodePoints;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;


    private List<LatLng> mPolyLineList;

    private PolylineOptions mPolyLineOptions;


    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewTime;
    private TextView mTextViewDistance;


    private String mExtraOrigin;
    private String mExtraDestination;

    private Button mButtonRequest;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);

        Mytoolbar.show(this,"Solicitar viaje",false);


        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_client);
        mMapFragment.getMapAsync(this);
        mExtraOriginLat = getIntent().getDoubleExtra("originLat",0);
        mExtraOriginLng = getIntent().getDoubleExtra("originLng",0);
        mExtraDestinationLat = getIntent().getDoubleExtra("DestinationLat",0);
        mExtraDestinationLng = getIntent().getDoubleExtra("DestinationLng",0);
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination= getIntent().getStringExtra("destination");



        mOriginLatLng = new LatLng(mExtraOriginLat,mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat,mExtraDestinationLng);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);


        mTextViewOrigin = findViewById(R.id.textViewOrigin);
        mTextViewDestination = findViewById(R.id.textViewDestination);
        mTextViewTime = findViewById(R.id.textViewTime);
        mTextViewDistance = findViewById(R.id.textViewDistance);
        mButtonRequest = findViewById(R.id.btnRequestNow);


        mTextViewOrigin.setText(mExtraOrigin);
        mTextViewDestination.setText(mExtraDestination);


        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToRequestDriver();
            }
        });



    }

    private void goToRequestDriver() {


        Intent intent = new Intent(DetailRequestActivity.this,RequestDriver.class);
        intent.putExtra("origin_lat",mOriginLatLng.latitude);
        intent.putExtra("origin_lng",mOriginLatLng.longitude);
        intent.putExtra("origin", mExtraOrigin);
        intent.putExtra("destination", mExtraDestination);
        intent.putExtra("destination_lat", mDestinationLatLng.latitude);
        intent.putExtra("destination_lng", mDestinationLatLng.longitude);
        startActivity(intent);
        finish();

    }


    private void drawRoute(){


        mGoogleApiProvider.getDirections(mOriginLatLng,mDestinationLatLng).enqueue(new Callback<String>() {
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


                    mTextViewTime.setText(durationText);
                    mTextViewDistance.setText(distanceText);




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


        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_red)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(

                new CameraPosition.Builder().target(mOriginLatLng).zoom(14f).build()

        ));

        drawRoute();

    }
}

