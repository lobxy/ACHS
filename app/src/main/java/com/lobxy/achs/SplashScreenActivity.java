package com.lobxy.achs;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "Splash Screen";
    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference usersNodeRef;
    String uid;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        usersNodeRef = FirebaseDatabase.getInstance().getReference().child("Users");
        user = mAuth.getCurrentUser();

        if (checkAndRequestPermissions()) {
            initApp();
        }

    }

    private void initApp() {
        Log.i(TAG, "initApp: called");
        Connection connection = new Connection(this);

        if (connection.check()) {
            if (user != null) {
                uid = user.getUid();
                //getPermissions();
                checkUser();
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                finish();
            }
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
        Log.i(TAG, "checkUser: called");
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

    public boolean checkAndRequestPermissions() {
        List<String> listPErmissionsNeeded = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPErmissionsNeeded.add(perm);
            }
        }

        if (!listPErmissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPErmissionsNeeded.toArray(new String[listPErmissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if (deniedCount == 0) {
                initApp();
            } else {

                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                        builder.setMessage("Yes,allow permissions")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        checkAndRequestPermissions();
                                    }
                                }).setNegativeButton("No,Exit App", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    } else {
                        //never asked again is checked.
                        //show dialog.
                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                        builder.setMessage("Denied Permissions, Allow")
                                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        break;
                    }
                }
            }
        }


    }//EOF

}//EOC
