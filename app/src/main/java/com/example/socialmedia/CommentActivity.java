package com.example.socialmedia;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity
{
    //fields
    private RecyclerView commentList;  //RecyclerView to display list of comments
    private EditText commentBox;   //comment box where user can type comments
    private Button commentPostButton; //button to post comment

    private String postKey;
    private String currentUserID;

    private DatabaseReference userRef;
    private DatabaseReference postRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //initializes fields
        postKey = getIntent().getExtras().get("Post Key").toString(); //gets post key of post and stores it

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid(); //gets current user ID
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserID).child(postKey).child("comments");  //points to comments database of a post

        commentList = (RecyclerView)findViewById(R.id.comment_list);
        commentBox = (EditText)findViewById(R.id.comment_box);
        commentPostButton = (Button)findViewById(R.id.comment_post_button);

        commentList.setHasFixedSize(true);

        //sets activities for when commentPostButton is clicked
        commentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {  //accesses user's information in Users database
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("username").getValue().toString();  //gets username from database
                            String commentText = commentBox.getText().toString();  //gets commentText from what is typed in comment box

                            if(TextUtils.isEmpty(commentText))  //if user hits commentPostButton without entering comment, Toast message will display
                            {
                                Toast.makeText(CommentActivity.this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                //gets date and time to create a unique key for comment
                                Calendar dateCal = Calendar.getInstance();
                                SimpleDateFormat date = new SimpleDateFormat("dd-MMMM-yyyy");
                                final String dateString = date.format(dateCal.getTime());

                                Calendar timeCal = Calendar.getInstance();
                                SimpleDateFormat time = new SimpleDateFormat("HH:mm");
                                final String timeString = time.format(timeCal.getTime());

                                final String randomKey = currentUserID + dateString + timeString;  //creates random key using currentUserID, dateString and timeString

                                HashMap commentMap = new HashMap(); //creates HashMap to store comment info
                                commentMap.put("uid", currentUserID);  //puts items in HashMap
                                commentMap.put("comment", commentText);
                                commentMap.put("username", userName);
                                //adds commentMap to database
                                postRef.child(randomKey).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task)
                                    {
                                        if(!task.isSuccessful())  //if task is not successful, displays Toast message
                                        {
                                            Toast.makeText(CommentActivity.this, "Sorry, there was an error while posting your comment.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            commentBox.setText(""); //sets the text to nothing after action is completed
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comments, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(null) {
            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return null;
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comments model)
            { //holder is template for layout of comments as they are displayed, model has comment information

                //we add information from model to holder, creating the display of a comment
                holder.setUsername(model.getUsername()); //sets holder's username to model's username
                holder.setText(model.getText());  //sets holder's text(commentText) to model's text
            }
        };
        commentList.setAdapter(firebaseRecyclerAdapter); //code typed in firebaseRecyclerAdapter will execute for each item on comment list
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder
    {
        View view;
        //constructor and methods for this class are defined below
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setUsername(String username)
        {
            TextView UserName = (TextView)view.findViewById(R.id.comment_username);
            UserName.setText("@" + username);
        }

        public void setText(String text)
        {
            TextView commentText = (TextView)view.findViewById(R.id.comment_text);
            commentText.setText(text);
        }
    }

}
