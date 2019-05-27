package com.example.socialmedia;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity
{
    //fields
    private Toolbar postToolbar;
    private ImageButton postImage;
    private EditText captionBox;
    private Button postButton;

    private static final int Gallery_Pic = 1;
    private Uri ImageUri;  //Uri for image
    private String captionText;  //String for caption
    private StorageReference postImageReference;

    private String dateString;  //Strings for time and date
    private String timeString;
    private String postRandomName;
    private String currentUserID;
    private String downloadURL;
    private DatabaseReference userRef;
    private DatabaseReference postRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //instantiates fields
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        postImageReference = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");  //usersRef accesses Users database
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserID);  //postRef accesses Post database of current user

        postImage = (ImageButton) findViewById(R.id.post_picture);
        captionBox = (EditText) findViewById(R.id.caption_box);
        postButton = (Button) findViewById(R.id.post_button);

        postToolbar = (Toolbar) findViewById(R.id.post_toolbar); //toolbar is enabled on this activity
        setSupportActionBar(postToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post");

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();   //if postImage button is clicked, app directs user to phone gallery to select photo
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pic);
            }
        });

        //when postButton is clicked, image and caption will be turned into post
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                captionText = captionBox.getText().toString();  //stores caption text from captionBox into variable(this is the caption
                if(ImageUri == null)//user has not selected image, will display message
                {
                    Toast.makeText(PostActivity.this, "Please select an image.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //collects date and time of post to create a unique post key to store image by
                    Calendar dateCal = Calendar.getInstance();
                    SimpleDateFormat date = new SimpleDateFormat("dd-MMMM-yyyy");
                    dateString = date.format(dateCal.getTime());

                    Calendar timeCal = Calendar.getInstance();
                    SimpleDateFormat time = new SimpleDateFormat("HH:mm");
                    timeString = time.format(timeCal.getTime());

                    postRandomName = dateString + timeString; //combines date and time to create a random name for post

                    //stores post using unique key
                    StorageReference filepath = postImageReference.child("post images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

                    filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if(task.isSuccessful())
                            {
                                downloadURL = task.getResult().getMetadata().getReference().toString();

                                userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(dataSnapshot.exists())
                                        {
                                            String username = dataSnapshot.child("username").getValue().toString();  //gets string for username of user
                                            String profilePic = dataSnapshot.child("profilePic").getValue().toString(); //gets string for profile pic of user

                                            HashMap postMap = new HashMap(); //map to store information that will be displayed in post
                                            postMap.put("uid", currentUserID);  //stores all data using keys and corresponding values
                                            postMap.put("caption", captionText);
                                            postMap.put("postPic", downloadURL);
                                            postMap.put("profilePic", profilePic);
                                            postMap.put("username", username);

                                            //creates space in Posts database under currentUserID for unique post name and information
                                            postRef.child(currentUserID).child(postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful())  //if post could be added successfully, redirects user to MainActivity(feed)
                                                    {
                                                        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(mainIntent);
                                                    }
                                                    else  //if there is error, displays Toast message
                                                    {
                                                        Toast.makeText(PostActivity.this, "Sorry, there was an error while making post.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else //if there is an error, displays error message
                            {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(PostActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pic && resultCode==RESULT_OK)
        {
            ImageUri = data.getData();  //when user selects an image, it sets the image on postImage
            postImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)  //if user selects home, redirects to feed or MainActivity
        {
            Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
            startActivity(mainIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}
