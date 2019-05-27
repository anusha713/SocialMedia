package com.example.socialmedia;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profilepic;  //fields to be displayed on profile
    private TextView name;
    private TextView username;
    private Button followButton;

    private DatabaseReference userRef;   //DatabaseReference to access user database
    private DatabaseReference followingRef;  //DatabaseReference to access follwoing database
    private FirebaseAuth auth;
    private String senderID;  //ID to identify sender of a follow request
    private String receiverID;  //ID to identify receiver of a follow request


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initializes fields
        auth = FirebaseAuth.getInstance();
        senderID = auth.getCurrentUser().getUid();  //gets userID of sender(current user)

        receiverID = getIntent().getExtras().get("user_ID").toString();  //receiver is user whose profile is being viewed
        userRef = FirebaseDatabase.getInstance().getReference().child("Users"); //userRef is directed to access Users database
        followingRef = FirebaseDatabase.getInstance().getReference().child("Following");  //followingRef is directed to access Following database

        profilepic = (ImageView) findViewById(R.id.profile_profilepic);
        name = (TextView) findViewById(R.id.profile_name);
        username = (TextView) findViewById(R.id.profile_username);
        followButton = (Button) findViewById(R.id.follow_button);


        userRef.child(receiverID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String myProfilePic = dataSnapshot.child("profilePic").getValue().toString(); //accesses String for profile pic of user through database, stores it in String variable
                    String myName = dataSnapshot.child("name").getValue().toString();  //accesses name of current user through database, stores in String variable
                    String myUserName = dataSnapshot.child("username").getValue().toString(); //access username of current user through database, stores in String variable

                    Picasso.with(ProfileActivity.this).load(myProfilePic).placeholder(R.drawable.profilepic).into(profilepic); //adds profile pic on to field using Picasso library

                    name.setText(myName); //sets name in the name field
                    username.setText("@" + myUserName);  //sets username(adds @ in front) to username field
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!senderID.equals(receiverID)) //sender and user are different
        {
            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {     //when follow button is clicked, adds current user to following database of other user
                    followingRef.child(senderID).child(receiverID).child("status").setValue("following")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())  //no error
                                    {
                                        followButton.setText("Following"); //displays text of button to "Following"
                                        followButton.setEnabled(false);  //disables it(unfollow function is not yet implemented into this app)
                                    }
                                    else  //if there is an error, it displays a message using Toast
                                    {
                                        Toast.makeText(ProfileActivity.this, "Sorry, there was an error.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }
        else  //user is viewing his or her own profile
        {
            followButton.setVisibility(View.INVISIBLE);  //makes button invisible
            followButton.setEnabled(false);     //disables follow button so user will not be able to follow himself or herself
        }
    }
}
