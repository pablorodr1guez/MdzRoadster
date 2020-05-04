package com.example.uberclone.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.uberclone.R;
import com.example.uberclone.activities.driver.HistoryBookingDriver;
import com.example.uberclone.models.HistoryBooking;
import com.example.uberclone.providers.DriverProvider;
import com.example.uberclone.providers.HistoryBookingProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HistoryBookingDetailClient extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewCalification;
    private RatingBar mRatingBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageBack;

    private String mExtraid;

    private HistoryBookingProvider mHistoryBookingProvider;

    private DriverProvider mDriverProvider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_client);



        mTextViewName = findViewById(R.id.textViewNameBookingDetail);
        mTextViewOrigin = findViewById(R.id.textViewOriginHistoryBookingDetail);
        mTextViewDestination = findViewById(R.id.textViewDestinationHistoryBookingDetail);
        mTextViewCalification = findViewById(R.id.textViewCalificationHistoryBookingDetail);
        mRatingBarCalification = findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage = findViewById(R.id.circleImageHistoryBookingDetail);

        mCircleImageBack = findViewById(R.id.circleImageBack);

        mExtraid = getIntent().getStringExtra("idHistoryBooking");

        mDriverProvider = new DriverProvider();

        mHistoryBookingProvider = new HistoryBookingProvider();
        
        getHistoryBooking();


        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });




    }

    private void getHistoryBooking() {

        mHistoryBookingProvider.getHistoryBooking(mExtraid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    HistoryBooking historyBooking = dataSnapshot.getValue(HistoryBooking.class);

                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewCalification.setText("Tu calificacion: "+ historyBooking.getCalificationDriver());

                    if(dataSnapshot.hasChild("calificationClient")){
                        mRatingBarCalification.setRating((float) historyBooking.getCalificationClient());

                    }

                    mDriverProvider.getDriver(historyBooking.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){


                                String name = dataSnapshot.child("name").getValue().toString();

                                mTextViewName.setText(name);


                                if(dataSnapshot.hasChild("image")){

                                    String image = dataSnapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailClient.this).load(image).into(mCircleImage);


                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
