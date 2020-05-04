package com.example.uberclone.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.uberclone.R;
import com.example.uberclone.activities.client.Registrer;
import com.example.uberclone.activities.driver.RegisterDriver;
import com.example.uberclone.includes.Mytoolbar;


public class SelectOptionAuth extends AppCompatActivity {

    Button btn_iniciarS;
    Button btn_Registrar;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);

        mPref = getApplicationContext().getSharedPreferences("typeuser",MODE_PRIVATE);


        Mytoolbar.show(this,"Selecciona una opcion",true);

        btn_iniciarS = findViewById(R.id.btn_iniciarsesion);
        btn_Registrar = findViewById(R.id.btn_registrar);


        btn_iniciarS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                IniciarSesion();
            }
        });

        btn_Registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Registrar();
            }
        });


    }

    private void Registrar() {

        String typeuser = mPref.getString("user","");

        if(typeuser.equals("client")){

            Intent intent = new Intent(SelectOptionAuth.this, Registrer.class);
            startActivity(intent);

        }else{


            Intent intent = new Intent(SelectOptionAuth.this, RegisterDriver.class);
            startActivity(intent);


        }


    }


    private void IniciarSesion(){

        Intent intent = new Intent(SelectOptionAuth.this,login.class);
        startActivity(intent);
    }

}
