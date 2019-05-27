package com.example.socialmedia;

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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity
{
    //fields
    private ImageView ProfilePic;  //to store Profile Pic of user
    private EditText Name;    //to store name of user
    private EditText Username;  //to store username of user
    private Button saveChangesButton; //button to update changes
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private String currentUserID;
    private StorageReference ProfilePicSto;
    final static int Gallery_Pic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //instantiates fields
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID); //points to database of user(with user's info)
        ProfilePicSto = FirebaseStorage.getInstance().getReference().child("Profile Pics");


        ProfilePic = (ImageView)findViewById(R.id.imageView);
        Name = (EditText)findViewById(R.id.name_text);
        Username = (EditText)findViewById(R.id.username_text);
        saveChangesButton = (Button)findViewById(R.id.save_changes_button);

        //when saveChangesButton is clicked
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = Name.getText().toString();  //gets text in Name field, stores field in String
                String username = Username.getText().toString(); //gets text in Username field, stores in String

                if(TextUtils.isEmpty(name)) //if Name box is empty, displays Toast message asking user to enter a name
                {
                    Toast.makeText(SettingsActivity.this, "Please fill in the name field", Toast.LENGTH_SHORT);
                }
                else if(TextUtils.isEmpty(username)) //fi Username box is empty, displays Toast message asking User to enter a username
                {
                    Toast.makeText(SettingsActivity.this, "Please fill in the username field", Toast.LENGTH_SHORT);
                }
                else //both fields are filled
                {
                    HashMap userMap = new HashMap(); //HashMap to store new user info
                    userMap.put("name", name); //puts name and username into userMap
                    userMap.put("username", username);
                    //adds items in userMap into user database
                    userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()) //successfully updated
                            {   //displays Toast message and directs user to MainActivity
                                Toast.makeText(SettingsActivity.this, "Your Information has been changed", Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //once user is on MainActivity, return key will not take them back to SettingsActivity
                                startActivity(mainIntent);
                                finish();
                            }
                            else {  //if task wasn't successful, displays error message using Toast
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        //when user clicks on ProfilePic(to change it)
        ProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(); //redirects user to phone Gallery so he or she can choose an image to use
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pic);
            }
        });

        //to change profile pic
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profilePic")) //this child is where profile pic is stored in database
                    {
                        String image = dataSnapshot.child("profilePic").getValue().toString();  //gets string that represents the existing image
                        Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.profilepic).into(ProfilePic); //replaces with new profile pic using Picasso library
                    }
                    else //image is not selected, displays Toast message asking user to select a picture
                    {
                        Toast.makeText(SettingsActivity.this, "Please select Profile Pic first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //for image cropping
        if(requestCode == Gallery_Pic && resultCode == RESULT_OK && data!=null)
        {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(requestCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                StorageReference filePath = ProfilePicSto.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Your Profile Image was stored successfully", Toast.LENGTH_SHORT).show();

                            final String downloadURL = task.getResult().toString();

                            userRef.child("Profile Pic").setValue(downloadURL)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(SettingsActivity.this, "Profile Pic stored successfully", Toast.LENGTH_SHORT).show();

                                                Intent setUpIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(setUpIntent);
                                            }
                                            else
                                            {
                                                String errorMessage = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Image cannot be cropped. Try a different one.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}