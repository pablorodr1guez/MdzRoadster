package com.example.uberclone.activities.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.uberclone.R;
import com.example.uberclone.activities.client.MapClient;
import com.example.uberclone.activities.client.Registrer;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.models.Client;
import com.example.uberclone.models.Driver;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientProvider;
import com.example.uberclone.providers.DriverProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterDriver extends AppCompatActivity {




    AuthProvider mAuthProvider;
    DriverProvider mDriverProvider;

    Button mbuttonRegistrer;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPass;
    TextInputEditText mTextInputBrand;
    TextInputEditText mTextInputPlate;

    AlertDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
        Mytoolbar.show(this,"Registrar conductor",true);


        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();

        mDialog = new SpotsDialog.Builder().setContext(RegisterDriver.this).setMessage("Espere un momento").build();

        mTextInputEmail = findViewById(R.id.inputEmail);
        mTextInputPass = findViewById(R.id.inputPass);
        mTextInputName = findViewById(R.id.inputName);
        mTextInputPlate = findViewById(R.id.inputVehiclePlate);
        mTextInputBrand = findViewById(R.id.inputVehicleBrand);
        mbuttonRegistrer = findViewById(R.id.button_regitrer);





        mbuttonRegistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                clickRegister();
            }
        });
    }

    private void clickRegister() {

        final String name = mTextInputName.getText().toString();
        final String email = mTextInputEmail.getText().toString();
        final String brand = mTextInputBrand.getText().toString();
        final String plate = mTextInputPlate.getText().toString();
        String pass = mTextInputPass.getText().toString();


        if(!name.isEmpty() && !email.isEmpty() && !pass.isEmpty() && !brand.isEmpty() && !plate.isEmpty()){

            if(pass.length() >= 6){

                mDialog.show();

                register(name,email,pass,brand,plate);

            }else{

                Toast.makeText(this, "La contraseña debe tener 6 o mas caracteres", Toast.LENGTH_SHORT).show();

            }


        }else {


        }


    }

    void register(final String name, final String email, String pass, final String brand, final String plate){

        mAuthProvider.register(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                mDialog.dismiss();

                if(task.isSuccessful()){

                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Driver driver = new Driver(name,email,brand,plate);
                    create(driver,id);

                }else{

                    Toast.makeText(RegisterDriver.this, "Fallo al registrar", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    void create (Driver driver, String id){

        mDriverProvider.create(driver,id).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                if(task.isSuccessful()){


                    Intent intent = new Intent(RegisterDriver.this, MapDriver.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);

                }else{


                    Toast.makeText(RegisterDriver.this, "No se pudo crear el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
}
