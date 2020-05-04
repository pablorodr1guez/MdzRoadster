package com.example.uberclone.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.uberclone.activities.driver.MapDriverBooking;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientBookingProvider;
import com.example.uberclone.providers.GeoFirebaseProvider;

public class AcceptReceiver extends BroadcastReceiver {
    private ClientBookingProvider mClientBookingProvider;
    private GeoFirebaseProvider mGeofireProvider;
    private AuthProvider mAuthProvider;


    @Override
    public void onReceive(Context context, Intent intent) {
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeoFirebaseProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getId());


        String idClient = intent.getExtras().getString("idClient");
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.updateStatus(idClient, "accept");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(context, MapDriverBooking.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient",idClient);

        context.startActivity(intent1);


    }
}
