package com.example.socialmedia;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity
{
    //fields
    private EditText userEmail; //field for email
    private EditText userPassword; //field for password
    private EditText confirmUserPassword; //field for confirm password
    private Button signUpButton; //button to sign up
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //instantiates fields
        auth = FirebaseAuth.getInstance();

        userEmail = (EditText) findViewById(R.id.email);
        userPassword = (EditText) findViewById(R.id.pass_word);
        confirmUserPassword = (EditText) findViewById(R.id.confirm_password);
        signUpButton = (Button) findViewById(R.id.create_new_account_button);

        //when signUpButton is clicked
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString(); //reads email and stores in variable
                String password = userPassword.getText().toString(); //reads password and stores in variable
                String confirmPassword = confirmUserPassword.getText().toString(); //reads text in confirm password field and stores in variable

                if (TextUtils.isEmpty(email)) //if email field is empty, displays Toast message asking user to fill that field
                {
                    Toast.makeText(SignUpActivity.this, "Please fill in the email field", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(password)) //if password field is empty, displays Toast message asking user to fill that field
                {
                    Toast.makeText(SignUpActivity.this, "Please fill in the password field.", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(confirmPassword)) //if confirm password is empty, displays Toast message asking user to fill that field
                {
                    Toast.makeText(SignUpActivity.this, "Please fill in the confirm password field.", Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals(confirmPassword)) //if password and confirmPassword are not equal, it asks user to try again
                {
                    Toast.makeText(SignUpActivity.this, "Your passwords do not match. Please try again", Toast.LENGTH_SHORT).show();
                }
                else  //all fields are filled and password & confirmPassword match
                {
                    auth.createUserWithEmailAndPassword(email, password)  //firebase creates user
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if(task.isSuccessful()) //if user is created, it displays Toast message and directs user to MainActivity
                                    {
                                        Toast.makeText(SignUpActivity.this, "Your account was created successfully.", Toast.LENGTH_SHORT).show();

                                        Intent setUpIntent = new Intent(SignUpActivity.this, SettingsActivity.class);
                                        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(setUpIntent);
                                        finish(); //finishes SignUpActivity
                                    }
                                    else //if there was an error, it displays error message using Toast
                                    {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();

        if(currentUser!=null) //if user isn't null(user is logged on), redirects them to MainActivity
        {
            Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
        }
    }
}