package com.example.uberclone.providers;


import com.example.uberclone.models.Client;
import com.example.uberclone.models.Driver;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DriverProvider {

    DatabaseReference mDatabase;


    public DriverProvider(){

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");


    }



    public Task<Void> create(Driver driver,String id){

        return mDatabase.child(id).setValue(driver);

    }

    public DatabaseReference getDriver(String idDriver){

        return mDatabase.child(idDriver);


    }

    public Task<Void> update(Driver driver){

        Map<String,Object> map = new HashMap<>();
        map.put("name",driver.getName());
        map.put("image",driver.getImage());

        return mDatabase.child(driver.getId()).updateChildren(map);
    }
}
