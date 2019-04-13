package com.lobxy.achs.User;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.lobxy.achs.LoginActivity;
import com.lobxy.achs.R;

public class UserMainScreenActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_screen);

        Button profile = findViewById(R.id.profile);
        Button complaints = findViewById(R.id.complaints);
        Button userComplaints = findViewById(R.id.userComplains);
        Button about = findViewById(R.id.about);

        auth = FirebaseAuth.getInstance();

        profile.setOnClickListener(this);
        complaints.setOnClickListener(this);
        about.setOnClickListener(this);
        userComplaints.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.complaints:
                startActivity(new Intent(this, ComplainTypesActivity.class));
                break;
            case R.id.userComplains:
                startActivity(new Intent(this, UserComplainActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainScreenMenuLogout:
                //signOut the user and return him to login screen
                auth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
