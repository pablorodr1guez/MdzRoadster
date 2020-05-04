package com.example.uberclone.activities.client;

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
import android.widget.Toolbar;

import com.example.uberclone.R;
import com.example.uberclone.includes.Mytoolbar;
import com.example.uberclone.models.Client;
import com.example.uberclone.providers.AuthProvider;
import com.example.uberclone.providers.ClientProvider;
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

public class UpdateProfile extends AppCompatActivity {


    private ImageView mImageViewProfile;
    private Button mButtonUpdate;
    private TextView mTextViewName;


    private ClientProvider mClientProvider;
    private AuthProvider mAuthProvider;
    private ImageProvider mImageProvider;




    private File mImageFile;
    private String mImage;

    private String mName;

    private final int GALLERY_REQUEST =1;


    private ProgressDialog mProgressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        Mytoolbar.show(this,"Actualizar perfil",true);



        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mButtonUpdate = findViewById(R.id.btn_updateProfile);
        mTextViewName = findViewById(R.id.inputNameProfile);


        mClientProvider = new ClientProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImageProvider("client_image");


        mProgressDialog = new ProgressDialog(this);


        getClientInfo();

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

    private void getClientInfo(){

        mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String name = dataSnapshot.child("name").getValue().toString();

                    String image = "";
                    if(dataSnapshot.hasChild("image")){

                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfile.this).load(image).into(mImageViewProfile);
                    }

                    mTextViewName.setText(name);



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void updateProfile() {


        mName = mTextViewName.getText().toString();

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

        mImageProvider.saveImage(UpdateProfile.this,mImageFile,mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                if(task.isSuccessful()){

                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String image = uri.toString();
                            Client client = new Client();
                            client.setImage(image);
                            client.setId(mAuthProvider.getId());
                            client.setName(mName);

                            mClientProvider.update(client).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UpdateProfile.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();

                                }
                            });


                        }
                    });


                }else{

                    Toast.makeText(UpdateProfile.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
