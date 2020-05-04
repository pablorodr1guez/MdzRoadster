package com.example.uberclone.includes;

import android.graphics.Color;

import com.example.uberclone.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Mytoolbar {



    public static void show(AppCompatActivity activity,String title, boolean upBotton){


        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        toolbar.setTitleTextColor(Color.WHITE);

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upBotton);



    }
}
