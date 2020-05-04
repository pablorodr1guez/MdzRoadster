package com.example.uberclone.activities.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uberclone.R;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientBookingProvider;
import com.example.uberclone.providers.GeoFirebaseProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class NotificationBooking extends AppCompatActivity {

    private TextView mTextViewDestination;
    private TextView mTextViewOrigin;
    private TextView mTextViewMins;
    private TextView mTextViewDistance;
    private TextView mTextViewCounter;

    private Button mButtonAccept;
    private Button mButtonCancel;


    private ClientBookingProvider mClientBookingProvider;
    private GeoFirebaseProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    private String mExtraIdClient;
    private String mExtraIdOrigin;
    private String mExtraIdDestination;
    private String mExtraMins;
    private String mExtraIdDistance;

    private MediaPlayer mMediaPlayer;


    private Handler mHandler;
    private int mCounter = 10;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            mCounter = mCounter - 1;
            mTextViewCounter.setText(String.valueOf(mCounter));


            if(mCounter >0 ){
                initTimer();
            }else{
                cancelBooking();
            }


        }
    };
    private ValueEventListener mListener;

    private void initTimer() {

        mHandler = new Handler();
        mHandler.postDelayed(runnable,1000);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);

        mTextViewDestination = findViewById(R.id.DestinationCalification);
        mTextViewOrigin = findViewById(R.id.originCalification);
        mTextViewMins = findViewById(R.id.textViewMins);
        mTextViewDistance = findViewById(R.id.textViewkms);
        mTextViewCounter = findViewById(R.id.textViewCounter);

        mButtonAccept = findViewById(R.id.btn_acceptBooking);
        mButtonCancel = findViewById(R.id.btn_cancelBooking);

        mExtraIdClient = getIntent().getStringExtra("idClient");
        mExtraIdOrigin = getIntent().getStringExtra("origin");
        mExtraIdDestination = getIntent().getStringExtra("destination");
        mExtraMins = getIntent().getStringExtra("mins");
        mExtraIdDistance = getIntent().getStringExtra("distance");

        mMediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mMediaPlayer.setLooping(true);


        mTextViewOrigin.setText(mExtraIdOrigin);
        mTextViewDestination.setText(mExtraIdDestination);
        mTextViewMins.setText(mExtraMins);
        mTextViewDistance.setText(mExtraIdDistance);

        mClientBookingProvider.updateStatus(mExtraIdClient, "cancel");


        getWindow().addFlags(

                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );


        initTimer();

        checkIfClientCancelBooking();

        mButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                acceptBooking();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                cancelBooking();
                
            }
        });


    }

    private void checkIfClientCancelBooking(){


        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){

                    if(mHandler!= null){

                        mHandler.removeCallbacks(runnable);

                        Toast.makeText(NotificationBooking.this, "El cliente cancelo el viaje", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(NotificationBooking.this,MapDriver.class);
                        startActivity(intent);

                        finish();
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void cancelBooking() {

        if(mHandler!= null){

            mHandler.removeCallbacks(runnable);
        }

        mClientBookingProvider = new ClientBookingProvider();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent = new Intent(NotificationBooking.this,MapDriver.class);
        startActivity(intent);

        finish();



    }

    private void acceptBooking() {

        if(mHandler!= null){

            mHandler.removeCallbacks(runnable);
        }


        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeoFirebaseProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getId());


        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.updateStatus(mExtraIdClient, "accept");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(NotificationBooking.this, MapDriverBooking.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient",mExtraIdClient);

        startActivity(intent1);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mMediaPlayer!=null){

            if(mMediaPlayer.isPlaying()){

                mMediaPlayer.pause();

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mMediaPlayer!=null){

            if(mMediaPlayer.isPlaying()){

                mMediaPlayer.release();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mMediaPlayer!=null){

            if(!mMediaPlayer.isPlaying()){

                mMediaPlayer.start();

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mHandler!= null){

            mHandler.removeCallbacks(runnable);
        }

        if(mMediaPlayer!= null){

            if(mMediaPlayer.isPlaying()){

                mMediaPlayer.pause();
            }
        }


        if(mListener != null){


            mClientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListener);




        }
    }
}
