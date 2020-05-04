package com.example.uberclone.activities.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.uberclone.R;
import com.example.uberclone.activities.client.HistoryBookingClient;
import com.example.uberclone.adapters.HistoryBookingClientAdapter;
import com.example.uberclone.adapters.HistoryBookingDriverAdapter;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.models.HistoryBooking;
import com.example.uberclone.providers.AuthProvider;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class HistoryBookingDriver extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private HistoryBookingDriverAdapter mAdapter;
    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_driver);

        Mytoolbar.show(this,"Historial de Viajes",true);

        mRecyclerView = findViewById(R.id.recyclerViewHistoryBooking);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthProvider = new AuthProvider();
        Query query = FirebaseDatabase.getInstance().getReference().child("HistoryBooking").orderByChild("idDriver").equalTo(mAuthProvider.getId());
        FirebaseRecyclerOptions<HistoryBooking> options = new FirebaseRecyclerOptions.Builder<HistoryBooking>()
                .setQuery(query,HistoryBooking.class).build();
        mAdapter = new HistoryBookingDriverAdapter(options, HistoryBookingDriver.this);


        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();

        mAdapter.stopListening();
    }
}
