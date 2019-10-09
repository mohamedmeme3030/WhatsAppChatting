package com.example.khaled_sa2r.whatsappchatting.MainProject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.khaled_sa2r.whatsappchatting.Base.MyBaseActivity;
import com.example.khaled_sa2r.whatsappchatting.LoginAndRegistration.LoginActivity;
import com.example.khaled_sa2r.whatsappchatting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URL;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends MyBaseActivity {
    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfilImage;
    String currentUserID;
    private ProgressDialog loadingBar;
    private static final int GalleryPick = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    //    private StorageReference UserProfilImagesRef;
    String imagelink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();

//        userName.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
//        UserProfilImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfilImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });

    }

    private void UpdateSettings() {

        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {

            Toast.makeText(this, "Please Wrire Your UserName First...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {

            Toast.makeText(this, "Please Wrire Your Status...", Toast.LENGTH_SHORT).show();

        } else {


            ShowProgressBar();
            HashMap<String, String> profilMap = new HashMap<>();
            profilMap.put("uid", currentUserID);
            profilMap.put("name", setUserName);
            profilMap.put("status", setStatus);
            profilMap.put("image", imagelink);

            RootRef.child("Users").child(currentUserID).setValue(profilMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                HideProgressBar();
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profil Updated Successfully...", Toast.LENGTH_SHORT).show();
                            } else {
                                HideProgressBar();
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        updateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profil_status);
        userProfilImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profil Image");
                loadingBar.setMessage("Please wait, while we are updating your profil image...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                final StorageReference ref = storageReference.child(currentUserID + ".jpg");

                ref.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {


                            @Override
                            public void onSuccess(Uri uri) {

                                final Uri downloadUrl = uri;
                                imagelink = uri.toString();
                                RootRef.child("Users").child(currentUserID).child("image")
                                        .setValue(downloadUrl.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {


                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingsActivity.this, "Image saved in Database Successfully...", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();


                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            imagelink = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(imagelink).into(userProfilImage);

                        } else if ((dataSnapshot.exists()) && dataSnapshot.hasChild("name")) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        } else {
//                               userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & update your profil information...", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(SettingsActivity.this, MainActivity.class);
        mainintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        SendUserToMainActivity();

    }
}
