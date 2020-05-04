package com.example.uberclone.activities.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.uberclone.R;
import com.example.uberclone.activities.client.HistoryBookingDetailClient;
import com.example.uberclone.models.HistoryBooking;
import com.example.uberclone.providers.ClientProvider;
import com.example.uberclone.providers.DriverProvider;
import com.example.uberclone.providers.HistoryBookingProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HistoryBookingDetailDriver extends AppCompatActivity {
    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewCalification;
    private RatingBar mRatingBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageBack;

    private String mExtraid;

    private HistoryBookingProvider mHistoryBookingProvider;

    private ClientProvider mClientProvider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_driver);



        mTextViewName = findViewById(R.id.textViewNameBookingDetail);
        mTextViewOrigin = findViewById(R.id.textViewOriginHistoryBookingDetail);
        mTextViewDestination = findViewById(R.id.textViewDestinationHistoryBookingDetail);
        mTextViewCalification = findViewById(R.id.textViewCalificationHistoryBookingDetail);
        mRatingBarCalification = findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage = findViewById(R.id.circleImageHistoryBookingDetail);

        mCircleImageBack = findViewById(R.id.circleImageBack);

        mExtraid = getIntent().getStringExtra("idHistoryBooking");

        mClientProvider = new ClientProvider();

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

                    mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){


                                String name = dataSnapshot.child("name").getValue().toString();

                                mTextViewName.setText(name);


                                if(dataSnapshot.hasChild("image")){

                                    String image = dataSnapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailDriver.this).load(image).into(mCircleImage);


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
