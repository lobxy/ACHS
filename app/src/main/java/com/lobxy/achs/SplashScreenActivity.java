package com.lobxy.achs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Admin.AdminMainScreen;
import com.lobxy.achs.Supervisor.SupervisorMain;
import com.lobxy.achs.User.UserMainScreenActivity;
import com.lobxy.achs.Utils.Connection;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "Splash Screen";
    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference usersNodeRef;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        usersNodeRef = FirebaseDatabase.getInstance().getReference().child("Users");
        user = mAuth.getCurrentUser();

        Connection connection = new Connection(this);

        if (connection.check()) {
            int splashScreenTimeout = 2000;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (user != null) {
                        uid = user.getUid();
                        checkUser();
                    } else {
                        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }, splashScreenTimeout);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert!");
            builder.setMessage("Please check your internet connection!")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    //Check the type of user, and redirect them to their respective Activities.
    public void checkUser() {

        usersNodeRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String type = dataSnapshot.child("type").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    Log.i(TAG, "onDataChange: type: " + type);
                    Log.i(TAG, "onDataChange: email: " + email);

                    if (type != null) {
                        if (type.equalsIgnoreCase("User")) {
                            startActivity(new Intent(SplashScreenActivity.this, UserMainScreenActivity.class));
                            finish();
                        } else if (type.equalsIgnoreCase("Admin")) {
                            startActivity(new Intent(SplashScreenActivity.this, AdminMainScreen.class));
                            finish();
                        } else if (type.equalsIgnoreCase("Supervisor")) {
                            startActivity(new Intent(SplashScreenActivity.this, SupervisorMain.class));
                            finish();
                        } else {
                            Toast.makeText(SplashScreenActivity.this, "Contact Support! Type not found", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(SplashScreenActivity.this, "User not found! Please register again", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SplashScreenActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onCancelled: Database Error: " + databaseError.getMessage());
            }
        });
    }

}
