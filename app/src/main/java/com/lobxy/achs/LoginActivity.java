package com.lobxy.achs;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Admin.AdminMainScreen;
import com.lobxy.achs.Supervisor.SupervisorMain;
import com.lobxy.achs.User.UserMainScreenActivity;
import com.lobxy.achs.Utils.Connection;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText edit_email, edit_pwd;

    public Button btn_login, btn_signUp, btn_forgotPassword;

    private FirebaseAuth mAuth;

    DatabaseReference usersNodeRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setInverseBackgroundForced(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Working...");

        edit_pwd = findViewById(R.id.login_edit_password);
        edit_email = findViewById(R.id.login_edit_email);

        btn_login = findViewById(R.id.login_button_submit);
        btn_signUp = findViewById(R.id.login_button_signup);
        btn_forgotPassword = findViewById(R.id.login_button_forgotPassword);

        mAuth = FirebaseAuth.getInstance();
        usersNodeRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btn_forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Alert!");
                builder.setMessage("Enter your email address");
                final EditText editText = new EditText(LoginActivity.this);
                builder.setView(editText);

                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        String email = editText.getText().toString().trim();
                        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            sendEmail(email);
                        } else {
                            Toast.makeText(LoginActivity.this, "Email is Invalid", Toast.LENGTH_LONG).show();
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

    private void validation() {
        String email = edit_email.getText().toString().trim();
        String password = edit_pwd.getText().toString().trim();

        if (email.isEmpty()) {
            edit_email.setError("Email is Empty");
            edit_email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edit_email.setError("Email is Invalid");
            edit_email.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edit_pwd.setError("Password is empty");
            edit_pwd.requestFocus();
            return;
        }

        progressDialog.show();

        Connection connection = new Connection(LoginActivity.this);

        if (connection.check()) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        checkUser(task.getResult().getUser().getUid());

                    } else {
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this, "Please connect to Internet", Toast.LENGTH_LONG).show();
        }

    }

    //Check the type of user, and redirect them to their respective Activities.
    public void checkUser(String uid) {
        progressDialog.show();

        usersNodeRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                if (dataSnapshot.exists()) {
                    String type = dataSnapshot.child("type").getValue(String.class);

                    Log.i(TAG, "onDataChange: type: " + type);

                    if (type != null) {
                        if (type.equalsIgnoreCase("Supervisor")) {
                            startActivity(new Intent(LoginActivity.this, SupervisorMain.class));
                        } else if (type.equalsIgnoreCase("User")) {
                            startActivity(new Intent(LoginActivity.this, UserMainScreenActivity.class));
                        } else if (type.equalsIgnoreCase("Admin")) {
                            startActivity(new Intent(LoginActivity.this, AdminMainScreen.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Error:LoginActivity-submit-task-Success-userType", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(LoginActivity.this, "Contact Support: User type doesn't exists", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User not found. Please login Again or RegisterActivity", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();

                Log.i(TAG, "onCancelled: DatabaseError Get uid reference " + databaseError.getMessage());
                Toast.makeText(LoginActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //send reset password email to the user
    private void sendEmail(String email) {
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                    Toast.makeText(LoginActivity.this, "Failed to send Email, try again!", Toast.LENGTH_LONG).show();
                    recreate();
                }
            }
        });

    }

}//EOC
