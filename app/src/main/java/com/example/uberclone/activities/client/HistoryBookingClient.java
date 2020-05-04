package com.example.uberclone.activities.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.uberclone.R;
import com.example.uberclone.adapters.HistoryBookingClientAdapter;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.models.HistoryBooking;
import com.example.uberclone.providers.AuthProvider;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class HistoryBookingClient extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private HistoryBookingClientAdapter mAdapter;
    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_client);

        Mytoolbar.show(this,"Historial de Viajes",true);

        mRecyclerView = findViewById(R.id.recyclerViewHistoryBooking);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthProvider = new AuthProvider();
        Query query = FirebaseDatabase.getInstance().getReference().child("HistoryBooking").orderByChild("idClient").equalTo(mAuthProvider.getId());
        FirebaseRecyclerOptions<HistoryBooking> options = new FirebaseRecyclerOptions.Builder<HistoryBooking>()
                .setQuery(query,HistoryBooking.class).build();
        mAdapter = new HistoryBookingClientAdapter(options,HistoryBookingClient.this);


        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();

        mAdapter.stopListening();
    }
}
