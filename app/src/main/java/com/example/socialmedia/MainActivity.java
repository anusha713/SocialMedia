package com.example.socialmedia;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.content.Intent;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //Note: this Main activity is the feed(where users will view posts of themselves and people they are following)

    private NavigationView navigationView;     //for navigation menu
    private DrawerLayout drawerLayout;
    private RecyclerView posts;  //for posts(feed)
    private Toolbar mainToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle; //to access navigation menu
    private FirebaseAuth auth;
    private DatabaseReference userRef;   //DatabaseReferences to access user database, post database, and like database
    private DatabaseReference postRef;
    private DatabaseReference likeRef;
    private ImageView NavProfilePic;  //variables to store profile pic, name, and username of current user to display in header of navigation menu
    private TextView NavName;
    private TextView NavUserName;
    private boolean userLiked; //determines whether or not post is liked(used with like button)
    String currentUserID; //String to identify to current user

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //in the following lines, fields are instantiated
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawerOpen, R.string.drawerClose);

        posts = (RecyclerView) findViewById(R.id.feed); //initializes posts RecyclerView
        posts.setHasFixedSize(true); //sets posts to have fixed size
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); //creates a LinearLayoutManager
        linearLayoutManager.setReverseLayout(true);  //so more recently added posts appear at the top
        linearLayoutManager.setStackFromEnd(true);   //so more recently added posts appear at the top
        posts.setLayoutManager(linearLayoutManager); //sets posts's layout manager to linearLayoutManager

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid(); //accesses currentUserID through Firebase database
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");  //initializes to user database
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");  //initializes to post database
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");  //initializes to likes database

        setSupportActionBar(mainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //when you click on the drawer toggle, the navigation menu will appear

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfilePic = (ImageView) navView.findViewById(R.id.profilepic); //instantiates field for profile pic of user
        NavName = (TextView) navView.findViewById(R.id.name);   //instantiates field to display name of user
        NavUserName = (TextView) navView.findViewById(R.id.username);  //instantiates field to display username of user

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("name"))
                    {
                        //accesses name and username of user through Firebase Database, stores them in name and username Strings respectively
                        String name = dataSnapshot.child("name").getValue().toString();
                        String username = dataSnapshot.child("username").getValue().toString();
                        NavName.setText(name); //sets text of NavName field to name
                        NavUserName.setText(username); //sets text of Username field to username
                    }
                    if (dataSnapshot.hasChild("profilePic"))  //if user has  a profile pic
                    {
                        String pic = dataSnapshot.child("profilePic").getValue().toString();  //stores ID of the profile pic through Firebase Database
                        Picasso.with(MainActivity.this).load(pic).placeholder(R.drawable.profilepic).into(NavProfilePic); //displays picture in NavProfilePic field using Picasso library
                    }
                    else { //if profile is not found, displays error message on the bottom using Toast
                        Toast.makeText(MainActivity.this, "Profile name does not exist.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override       //this is for when items in the NavigationMenu are selected
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.feed:     //when feed is selected, redirects user to MainActivity(which is the feed)
                        Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        break;
                    case R.id.profile:  //when profile is selected, redirects user to ProfileActivity(User's Profile)
                        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                        break;
                    case R.id.search:   //when search is selected, redirects user to SearchActivity(where user can search for other users)
                        Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(searchIntent);
                        break;
                    case R.id.newPost:  //when newPost is selected, redirects user to PostActivity so he or she can create a post
                        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
                        startActivity(postIntent);
                        break;
                    case R.id.settings:   //when settings is selected, redirects user to settings activity where user can change account information
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                    case R.id.logOut:   //when logOut is selected, signs out user and redirects them to login activity.
                        auth.signOut();
                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //if user hits return key, it will not return to Main Activity
                        startActivity(loginIntent);
                        finish();
                        break;
                }
                return false;
            }
        });

        DisplayAllPosts();
    }

    private void DisplayAllPosts() //display all posts to appear in feed
    {
           //AllPostsHolder class is a child class of RecyclerView made for the posts
           // AllPosts is a class with all the methods to modify post attributes
        FirebaseRecyclerAdapter<AllPosts, AllPostsHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<AllPosts, AllPostsHolder>(null)
                {
                    @NonNull
                    @Override
                    public AllPostsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        return null;   //this method and code automatically appear when using Android Studio
                    }

                    @Override
                    protected void onBindViewHolder(final @NonNull AllPostsHolder holder, int position, final @NonNull AllPosts model)
                    {                //holder is template for a post as it appears in feed, model contains information of the post
                        final String usersID = getRef(position).getKey();    //gets user ID of person user is following
                        final String postKey = getRef(position).getKey();    //gets post key of a post to appear in the feed

                        postRef.child(usersID).child(postKey).addValueEventListener(new ValueEventListener()
                        {      //in post database, it looks for the database of the user being followed and accesses the post information using the postKey
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                //we add information from model to holder, creating the display of a post
                                holder.setUsername(model.getUsername());  //fills in holder's username field with username from model
                                holder.setCaption(model.getCaption());  //fills in holder's caption field with caption from model
                                holder.setProfilepic(getApplicationContext(), model.getProfilepic()); //sets holder's profile pic to profile pic from model
                                holder.setPostPic(getApplicationContext(), model.getPostpic()); //fills in holder's postPic field to postPic from model

                                holder.setLikeButtonStatus(postKey);  //sets likes of posts of user

                                holder.commentButton.setOnClickListener(new View.OnClickListener() //sets activities for when comment button is clicked
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent commentIntent = new Intent(MainActivity.this, CommentActivity.class); //Intent to go to comment activity
                                        commentIntent.putExtra("Post Key", postKey); //adds this information so it goes to comments of post
                                        startActivity(commentIntent); //executes commentIntent(redirects user to CommentActivity of post)
                                    }
                                });

                                holder.likeButton.setOnClickListener(new View.OnClickListener() //sets activities for when like button is clicked
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        userLiked = true;  //sets userLiked to true so following functions work

                                        likeRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (userLiked)
                                                {
                                                    if (dataSnapshot.child(postKey).hasChild(currentUserID))
                                                    {    //if user is already in like database of post, then
                                                         //user has already liked in the past and is clicking to unlike
                                                        likeRef.child(postKey).child(currentUserID).removeValue();  //removes from user from like database of post
                                                        userLiked = false; //sets userLiked to false so this code executes next time user clicks
                                                    }
                                                    else {  //user is not in database and is clicking like button to like the post
                                                        likeRef.child(postKey).child(currentUserID).setValue(true); //adds user to like database of post
                                                        userLiked = false; //sets userLiked to false so this code executes next time user clicks
                                                    }
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
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };
        posts.setAdapter(firebaseRecyclerAdapter);  //code in firebaseRecyclerAdapter executes for all items in posts, creating the feed
    }

    public static class AllPostsHolder extends RecyclerView.ViewHolder
    {
        //variables
        View view;
        ImageButton likeButton;
        TextView numberOfLikes;
        Button commentButton;
        int likeCount;
        String currentUserID;
        DatabaseReference likesRef;

        public AllPostsHolder(@NonNull View itemView)
        {
            super(itemView);
            view = itemView;

            //instantiates all variables
            likeButton = (ImageButton) view.findViewById(R.id.like_button);
            numberOfLikes = (TextView) view.findViewById(R.id.number_of_likes);
            commentButton = (Button) view.findViewById(R.id.comment_button);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String postKey) //for setting likes of the posts of user
        {
            likesRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.child(postKey).hasChild(currentUserID)) //user has liked their own post
                    {
                        likeCount = (int) dataSnapshot.child(postKey).getChildrenCount(); //finds number of likes on post through database(counts how many children the post has in like database), initializes to likeCount
                        likeButton.setImageResource(R.drawable.redheart); //likeButton will be red heart since user has liked
                        numberOfLikes.setText(Integer.toString(likeCount)); //makes likeCount to String and displays it in numberOfLikes field
                    }
                    else {
                        likeCount = (int) dataSnapshot.child(postKey).getChildrenCount(); //finds number of likes on post through database(counts how many children the post has in like database), initializes to likeCount
                        likeButton.setImageResource(R.drawable.blankheart);//likeButton will be blank heart wince user has not liked the post
                        numberOfLikes.setText(Integer.toString(likeCount)); //makes likeCount to String and displays it in numberOfLikes field
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

         //the following methods are all setter methods for the different fields
        public void setUsername(String username) {
            TextView usernameOnPost = (TextView) view.findViewById(R.id.post_username);
            usernameOnPost.setText(username);
        }

        public void setProfilepic(Context context, String profilepic) {
            ImageView profilepicOnPost = (ImageView) view.findViewById(R.id.post_profile_pic);
            Picasso.with(context).load(profilepic).into(profilepicOnPost);
        }

        public void setCaption(String caption) {
            TextView postCaption = (TextView) view.findViewById(R.id.post_caption);
            postCaption.setText(caption);
        }

        public void setPostPic(Context context, String postpic) {
            ImageView picture = (ImageView) view.findViewById(R.id.post_pic);
            Picasso.with(context).load(postpic).into(picture);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) //if user is null(not logged in) directs them to Login Activity so they can log in
        {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();

        }
        else {  //user is logged in
            final String userID = auth.getCurrentUser().getUid(); //accesses user ID through firebase

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (!dataSnapshot.hasChild(userID))  //if user has made account but is not registered in database,
                    {                                    // redirects them to SettingsActivity so they can enter their information
                        Intent setUpIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(setUpIntent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }


            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) //navigation menu will appear when toggle is clicked on
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}