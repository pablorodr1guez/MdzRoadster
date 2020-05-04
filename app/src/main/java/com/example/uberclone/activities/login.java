package com.example.uberclone.activities;

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
import com.example.uberclone.activities.driver.MapDriver;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.models.Driver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class login extends AppCompatActivity {


    TextInputEditText email;
    TextInputEditText Pass;
    Button StartSession;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    AlertDialog mDialog;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Mytoolbar.show(this,"Iniciar sesion",true);

        mPref = getApplicationContext().getSharedPreferences("typeuser",MODE_PRIVATE);

        mDialog = new SpotsDialog.Builder().setContext(login.this).setMessage("Espere un momento").build();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        email = findViewById(R.id.textInputEmail);
        Pass = findViewById(R.id.textInputPass);

        StartSession = findViewById(R.id.btn_startSesion);




        StartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                login();
            }
        });

    }

    private void login() {


        String mail = email.getText().toString();
        String pass = Pass.getText().toString();

        if( !mail.isEmpty() && !pass.isEmpty()){

            if(pass.length() >= 6){

                mDialog.show();
                mAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if(task.isSuccessful()){

                            String user = mPref.getString("user","");

                            if(user.equals("client")){

                                Intent intent = new Intent(login.this, MapClient.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(intent);


                            }else{

                                Intent intent = new Intent(login.this, MapDriver.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(intent);


                            }


                        }else{

                            Toast.makeText(login.this, "El correo o la contraseña son incorrectos",Toast.LENGTH_LONG).show();
                        }

                        mDialog.dismiss();
                    }
                });



            }else{


                Toast.makeText(login.this, "La contraseña debe tener 6 o mas caracteres ", Toast.LENGTH_SHORT).show();
            }


        }

    }
}
