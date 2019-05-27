package com.example.socialmedia;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{
    //fields
    private EditText email;  //field to enter email
    private EditText password;  //field to enter paword
    private Button logInBtn; //button to log in
    private Button newAccountBtn;  //button to make new account(if user does not have account)

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //instantiates all fields
        auth = FirebaseAuth.getInstance();

        email = (EditText)findViewById(R.id.user_email);
        password = (EditText)findViewById(R.id.pass_word);
        logInBtn = (Button)findViewById(R.id.login_button);
        newAccountBtn = (Button)findViewById(R.id.new_account_btn);

        //when logInButton is clicked
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString();  //stores email and password in strings
                String pass_word = password.getText().toString();

                if(TextUtils.isEmpty(userEmail)) //if email field is empty, it displays Toast message telling user to enter an email
                {
                    Toast.makeText(LoginActivity.this, "Error: Please enter an email", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(pass_word)) //if password field is empty, it displays Toast message telling user to enter a password
                {
                    Toast.makeText(LoginActivity.this, "Error: Please enter a password", Toast.LENGTH_SHORT).show();
                }
                else //both fields are filled
                {
                    auth.signInWithEmailAndPassword(userEmail, pass_word)  //authenticates user using Firebase
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful())
                                    {     //if user could be authenticated, redirects them to MainActivity(feed)
                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //once user is logged in, hitting return key will not return them to LoginActivity
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                    else //user could not be authenticated
                                    {        //displays error message through Toast
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        //when newAccountBtn is clicked
        newAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  //finishes this activity
                //redirects user to SignUpActivity
                Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();

        if(currentUser!=null) //if user is already logged in, they will be redirected to MainActivity
        {
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
        }
    }
}
