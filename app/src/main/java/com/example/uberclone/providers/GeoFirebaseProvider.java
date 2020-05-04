package com.example.uberclone.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeoFirebaseProvider {


    private DatabaseReference mDataBase;
    private GeoFire mGeoFire;



    public GeoFirebaseProvider(String reference){

        mDataBase = FirebaseDatabase.getInstance().getReference().child(reference);
        mGeoFire = new GeoFire(mDataBase);

    }

    public void saveLocation(String idDraver, LatLng latlng){

        mGeoFire.setLocation(idDraver,new GeoLocation(latlng.latitude,latlng.longitude));

    }

    public void removeLocation(String idDriver){

        mGeoFire.removeLocation(idDriver);

    }

    public GeoQuery getActiveDrivers(LatLng latLng,double radius){


        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),radius);

        geoQuery.removeAllListeners();

        return geoQuery;
    }

    public DatabaseReference getDriverLocation(String idDriver){
        return mDataBase.child(idDriver).child("l");
    }


    public DatabaseReference isDriverWorking( String idDriver){

        return  FirebaseDatabase.getInstance().getReference().child("drivers_working").child(idDriver);
    }


}
