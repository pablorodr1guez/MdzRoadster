package com.example.uberclone.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.uberclone.R;
import com.example.uberclone.activities.client.MapClient;
import com.example.uberclone.activities.client.Registrer;
import com.example.uberclone.activities.driver.MapDriver;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {


    Button btn_Driver;
    Button btn_Client;


    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mPref = getApplicationContext().getSharedPreferences("typeuser",MODE_PRIVATE);

        final SharedPreferences.Editor editor = mPref.edit();



        btn_Driver = findViewById(R.id.btn_driver);
        btn_Client = findViewById(R.id.btn_client);


        btn_Client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                editor.putString("user","client");
                editor.apply();

                goToSelectAuth();
            }
        });


        btn_Driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                editor.putString("user","driver");
                editor.apply();
                goToSelectAuth();
            }
        });

    }


    @Override
    protected void onStart(){

        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){

            String user = mPref.getString("user","");

            if(user.equals("client")){

                Intent intent = new Intent(MainActivity.this, MapClient.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else {


                Intent intent = new Intent(MainActivity.this, MapDriver.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }

        }
    }

    private void goToSelectAuth() {


        Intent intent = new Intent(MainActivity.this,SelectOptionAuth.class);

        startActivity(intent);
    }
}
