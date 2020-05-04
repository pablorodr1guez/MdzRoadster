package com.example.uberclone.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uberclone.R;
import com.example.uberclone.activities.driver.CalificationClient;
import com.example.uberclone.activities.driver.MapDriver;
import com.example.uberclone.models.ClientBooking;
import com.example.uberclone.models.HistoryBooking;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientBookingProvider;
import com.example.uberclone.providers.HistoryBookingProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class CalificationDriver extends AppCompatActivity {

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private RatingBar mRatingBar;
    private Button mButtonCalification;
    private ClientBookingProvider mClienBookingProvider;

    private AuthProvider mAuthProvider;

    private String mExtraClientId;

    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;

    private float mCalification = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_driver);


        mTextViewDestination = findViewById(R.id.DestinationCalification);
        mTextViewOrigin = findViewById(R.id.originCalification);
        mRatingBar = findViewById(R.id.ratingdriver);
        mButtonCalification = findViewById(R.id.btnCalificationDriver);

        mAuthProvider = new AuthProvider();

        mClienBookingProvider = new ClientBookingProvider();

        mHistoryBookingProvider = new HistoryBookingProvider();

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calification, boolean b) {

                mCalification = calification;


            }
        });

        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calificate();
            }
        });


        getClientBooking();


    }

    private void getClientBooking() {

        mClienBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    ClientBooking clientBooking = dataSnapshot.getValue(ClientBooking.class);

                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    mTextViewDestination.setText(clientBooking.getDestination());

                    mHistoryBooking = new HistoryBooking(


                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng()


                    );

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void calificate() {


        if (mCalification > 0) {

            mHistoryBooking.setCalificationDriver(mCalification);
            mHistoryBooking.setTimeStang(new Date().getTime());

            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        mHistoryBookingProvider.updateCalificationDriver(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(CalificationDriver.this, "La calificacion se guardo correctamente", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(CalificationDriver.this, MapClient.class);

                                startActivity(intent);
                                finish();

                            }
                        });

                    } else {

                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(CalificationDriver.this, "La calificacion se guardo correctamente", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(CalificationDriver.this, MapClient.class);

                                startActivity(intent);
                                finish();
                            }
                        });


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } else {

            Toast.makeText(this, "Debe ingresar una calificacion", Toast.LENGTH_SHORT).show();
        }
    }
}

