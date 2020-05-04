package com.example.uberclone.activities.client;

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
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.models.Client;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registrer extends AppCompatActivity {




    AuthProvider mAuthProvider;
    ClientProvider mClientProvider;

    Button mbuttonRegistrer;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPass;

    AlertDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer);

        Mytoolbar.show(this,"Registrar usuario",true);


        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();

        mDialog = new SpotsDialog.Builder().setContext(Registrer.this).setMessage("Espere un momento").build();

        mTextInputEmail = findViewById(R.id.inputEmail);
        mTextInputPass = findViewById(R.id.inputPass);
        mTextInputName = findViewById(R.id.inputName);
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
        String pass = mTextInputPass.getText().toString();


        if(!name.isEmpty() && !email.isEmpty() && !pass.isEmpty()){

            if(pass.length() >= 6){

                mDialog.show();

                register(name,email,pass);

            }else{

                Toast.makeText(this, "La contraseña debe tener 6 o mas caracteres", Toast.LENGTH_SHORT).show();

            }


        }else {


        }


    }

    void register(final String name, final String email, String pass){

        mAuthProvider.register(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                mDialog.dismiss();

                if(task.isSuccessful()){

                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Client client = new Client(name,email,id);
                    create(client,id);

                }else{

                    Toast.makeText(Registrer.this, "Fallo al registrar", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    void create (Client client,String id){

        mClientProvider.create(client,id).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                
                
                if(task.isSuccessful()){


                    Toast.makeText(Registrer.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Registrer.this,MapClient.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);

                }else{


                    Toast.makeText(Registrer.this, "No se pudo crear el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }





}
