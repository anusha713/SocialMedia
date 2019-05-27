package com.example.socialmedia;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class SearchActivity extends AppCompatActivity
{
    //fields
    private Toolbar searchToolbar;
    private Button searchButton; //this will search through database and look for users
    private EditText searchBar; //bar where user can enter username of user they are searching

    private RecyclerView searchList; //list for users being searched
    private DatabaseReference searchDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //initializes fields
        searchDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users"); //searchDatabaseRef points to Users Database

        searchToolbar = (Toolbar) findViewById(R.id.search_app_bar);
        setSupportActionBar(searchToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Post");

        searchList = (RecyclerView)findViewById(R.id.search_list);
        searchList.setHasFixedSize(true);
        searchList.setLayoutManager(new LinearLayoutManager(this));

        searchButton = (Button) findViewById(R.id.search_button);
        searchBar = (EditText)findViewById(R.id.search_bar);

        //when searchButton is clicked
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                FirebaseRecyclerAdapter<Search, SearchViewHolder> firebaseRecyclerAdapter
                        =new FirebaseRecyclerAdapter<Search, SearchViewHolder>(null) {
                    @NonNull
                    @Override
                    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        return null;
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull SearchViewHolder holder, final int position, @NonNull Search model)
                    {
                        //holder is template for layout of user as they appear in the search list, model contains data of user
                        //we add information from model to holder, creating the display of a user in the search list
                        holder.setUsername(model.getUsername()); //fills in username field of holder with username of model
                        holder.setProfilePic(getApplicationContext(), model.getProfilePic());//fills in profilePic field of holder with profilePic of model

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //when a profile from the search list is selected, user is redirected to the ProfileActivity of that user
                                String visitUserID = getRef(position).getKey();

                                Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visitUserID", visitUserID);
                                startActivity(profileIntent);
                            }
                        });
                    }


                };
                searchList.setAdapter(firebaseRecyclerAdapter);  //code in firebaseRecyclerAdapter executes for all items in the searchList
            }

        });
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder
    {
        View view;

        //constructor
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        //setter methods
        public void setProfilePic(Context context, String profilepic)
        {
            ImageView image = (ImageView)view.findViewById(R.id.search_list_profilepic);
            Picasso.with(context).load(profilepic).placeholder(R.drawable.profilepic).into(image);
        }

        public void setUsername(String username)
        {
            TextView user_name = (TextView)view.findViewById(R.id.search_list_username);
            user_name.setText(username);
        }
    }
}