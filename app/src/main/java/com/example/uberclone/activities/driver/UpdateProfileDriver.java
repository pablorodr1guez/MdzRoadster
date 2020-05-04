package com.example.uberclone.activities.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uberclone.R;
import com.example.uberclone.activities.client.UpdateProfile;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.models.Client;
import com.example.uberclone.models.Driver;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientProvider;
import com.example.uberclone.providers.DriverProvider;
import com.example.uberclone.providers.ImageProvider;
import com.example.uberclone.utils.CompressorBitmapImage;
import com.example.uberclone.utils.FileUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

public class UpdateProfileDriver extends AppCompatActivity {
    private ImageView mImageViewProfile;
    private Button mButtonUpdate;
    private TextView mTextViewName;
    private TextView mTextViewBrand;
    private TextView mTextViewPlate;


    private DriverProvider mDriverProvider;
    private AuthProvider mAuthProvider;
    private ImageProvider mImageProvider;

    private File mImageFile;
    private String mImage;

    private String mName;
    private String mPlate;
    private String mBrand;

    private final int GALLERY_REQUEST =1;


    private ProgressDialog mProgressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_driver);

        Mytoolbar.show(this,"Actualizar perfil",true);



        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mButtonUpdate = findViewById(R.id.btn_updateProfile);
        mTextViewName = findViewById(R.id.inputNameProfile);
        mTextViewBrand = findViewById(R.id.inputVehicleBrand);
        mTextViewPlate = findViewById(R.id.inputVehiclePlate);


        mDriverProvider = new DriverProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImageProvider("driver_image");

        mProgressDialog = new ProgressDialog(this);


        getDriverInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                openGallery();



            }
        });



        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateProfile();
            }
        });



    }

    private void openGallery() {


        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){


            try{

                mImageFile = FileUtil.from(this,data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));


            }catch (Exception e){

                Log.d("ERROR","Mensaje:"+ e.getMessage());


            }

        }
    }

    private void getDriverInfo(){

        mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String name = dataSnapshot.child("name").getValue().toString();
                    String brand = dataSnapshot.child("vehicleBrand").getValue().toString();
                    String plate = dataSnapshot.child("vehiclePlate").getValue().toString();
                    String image = "";
                    if(dataSnapshot.hasChild("image")){

                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfileDriver.this).load(image).into(mImageViewProfile);
                    }

                    mTextViewName.setText(name);
                    mTextViewBrand.setText(brand);
                    mTextViewPlate.setText(plate);




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void updateProfile() {


        mName = mTextViewName.getText().toString();
        mBrand = mTextViewBrand.getText().toString();
        mPlate = mTextViewPlate.getText().toString();


        if(!mName.equals("")&& mImageFile != null){

            mProgressDialog.setMessage("Espere un momento...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            saveImage();




        }else{


            Toast.makeText(this, "Ingrese imagen y nombre", Toast.LENGTH_SHORT).show();
        }




    }

    private void saveImage() {

        mImageProvider.saveImage(UpdateProfileDriver.this,mImageFile,mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                if(task.isSuccessful()){

                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String image = uri.toString();
                            Driver driver = new Driver();
                            driver.setImage(image);
                            driver.setId(mAuthProvider.getId());
                            driver.setName(mName);
                            driver.setVehiclePlate(mPlate);
                            driver.setVehicleBrand(mBrand);

                            mDriverProvider.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UpdateProfileDriver.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();

                                }
                            });


                        }
                    });


                }else{

                    Toast.makeText(UpdateProfileDriver.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
