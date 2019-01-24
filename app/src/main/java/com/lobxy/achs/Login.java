package com.lobxy.achs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Admin.AdminMainScreen;
import com.lobxy.achs.Supervisor.SupervisorMain;
import com.lobxy.achs.User.Activities.UserMainScreen;
import com.lobxy.achs.User.Utils.mAlertDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    private EditText edit_email, edit_pwd;

    public Button btn_login, btn_signUp, forgotPassword;

    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authStateListener;

    DatabaseReference usersNodeRef;
    FirebaseUser user;

    String uid;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dialog = new ProgressDialog(this);
        dialog.setInverseBackgroundForced(false);
        dialog.setCancelable(false);
        dialog.setMessage("Working...");

        edit_pwd = findViewById(R.id.login_edit_password);
        edit_email = findViewById(R.id.login_edit_email);

        btn_login = findViewById(R.id.login_button_submit);
        btn_signUp = findViewById(R.id.login_button_signup);
        forgotPassword = findViewById(R.id.login_button_forgotPassword);

        mAuth = FirebaseAuth.getInstance();
        usersNodeRef = FirebaseDatabase.getInstance().getReference().child("Users");

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = mAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                    checkUser();
                }
            }
        };

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edit_email.getText().toString();
                String password = edit_pwd.getText().toString();

                if (email.isEmpty()) {
                    edit_email.setError("Email is Empty");
                    edit_email.requestFocus();
                    return;
                }
                if (!isEmailValid(email)) {
                    edit_email.setError("Email is Invalid");
                    edit_email.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    edit_pwd.setError("Password is empty");
                    edit_pwd.requestFocus();
                    return;
                }

                dialog.show();
                if (connectivity()) {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                checkUser();
                            } else {
                                Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    dialog.dismiss();
                    Toast.makeText(Login.this, "Please connect to Internet", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle("Alert!");
                builder.setMessage("Enter your email address");
                final EditText editText = new EditText(Login.this);
                builder.setView(editText);

                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        String email = editText.getText().toString().trim();
                        if (isEmailValid(email)) {
                            sendEmail(email);
                        } else {
                            Toast.makeText(Login.this, "Email is Invalid", Toast.LENGTH_LONG).show();
                        }

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }

    //send reset password email to the user
    private void sendEmail(String email) {
        dialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setTitle("Reset Password!");
                    builder.setMessage("Reset password Email is sent to given Email Account.\nPlease follow the link and login again.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    Toast.makeText(Login.this, "Failed to send Email, try again!", Toast.LENGTH_LONG).show();
                    recreate();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //Check the type of user, and redirect them to their respective Activities.
    public void checkUser() {
        dialog.show();


        usersNodeRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();

                if (dataSnapshot.exists()) {
                    String type = dataSnapshot.child("type").getValue(String.class);
                    Log.i(TAG, "onDataChange: type" + type);
                    if (type != null) {
                        if (type.equalsIgnoreCase("Supervisor")) {
                            startActivity(new Intent(Login.this, SupervisorMain.class));
                        } else if (type.equalsIgnoreCase("User")) {
                            startActivity(new Intent(Login.this, UserMainScreen.class));
                        } else if (type.equalsIgnoreCase("Admin")) {
                            startActivity(new Intent(Login.this, AdminMainScreen.class));
                        } else {
                            Toast.makeText(Login.this, "Error:Login-submit-task-Success-userType", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(Login.this, "Contact Support: User type doesn't exists", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login.this, "User not found. Please login Again or Register", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();

                Log.i(TAG, "onCancelled: DatabaseError Get uid reference " + databaseError.getMessage());
                Toast.makeText(Login.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean connectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        mAlertDialog dialog = new mAlertDialog(this);
        dialog.alertDialog();
    }
}
