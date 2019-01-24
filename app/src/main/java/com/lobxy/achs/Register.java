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
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lobxy.achs.User.Activities.UserMainScreen;
import com.lobxy.achs.Model.User;
import com.lobxy.achs.Model.UserReg;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register Activity";
    EditText edit_password_confirm, edit_password, edit_email, edit_name, edit_contact, edit_address;

    Spinner siteSpinner;
    Button but_submit, but_login;

    String password, email, confirmPass, contact, name, type = "User", id, address, site = "Null";
    String uid;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference ref;
    DatabaseReference userRef;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dialog = new ProgressDialog(this);
        dialog.setInverseBackgroundForced(false);
        dialog.setCancelable(false);
        dialog.setMessage("Working...");

        edit_password = findViewById(R.id.register_edit_password);
        edit_password_confirm = findViewById(R.id.register_edit_confirm_password);
        edit_email = findViewById(R.id.register_edit_email);
        edit_contact = findViewById(R.id.register_edit_contact);
        edit_name = findViewById(R.id.register_edit_name);
        edit_address = findViewById(R.id.register_edit_address);
        siteSpinner = findViewById(R.id.register_spinner);

        but_submit = findViewById(R.id.register_button_submit);

        but_login = findViewById(R.id.register_button_login);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("User_Data");
        userRef = database.getReference("Users");
        mAuth = FirebaseAuth.getInstance();

        String[] sites = getResources().getStringArray(R.array.sites_name);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sites);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        siteSpinner.setAdapter(dataAdapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Working...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        but_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        but_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //validate whether the data is true and is in correct form.
    private void validateData() {
        //Check Data and Create Users.

        password = edit_password.getText().toString().trim();
        email = edit_email.getText().toString().trim();
        confirmPass = edit_password_confirm.getText().toString().trim();
        name = edit_name.getText().toString().trim();
        contact = edit_contact.getText().toString().trim();
        address = edit_address.getText().toString().trim();

        if (name.isEmpty()) {
            edit_name.setError("Field is empty");
            edit_name.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            edit_email.setError("Field is empty");
            edit_email.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            edit_address.setError("Field is empty");
            edit_address.requestFocus();
            return;
        }

        if (siteSpinner.getSelectedItem().toString().trim().equals("Please Select A Site")) {
            Toast.makeText(this, "Please select a Site", Toast.LENGTH_LONG).show();
            return;
        }
        if (contact.isEmpty()) {
            edit_contact.setError("Field is empty");
            edit_contact.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edit_password.setError("Field is empty");
            edit_password.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edit_email.setError("Please enter a valid email");
            edit_email.requestFocus();
            return;
        }
        if (!confirmPass.equalsIgnoreCase(password)) {
            edit_password_confirm.setError("Passwords do not match");
            edit_password_confirm.requestFocus();
            return;
        }
        if (contact.length() < 10) {
            edit_contact.setError("Phone Number is less than 10 characters.");
            edit_contact.requestFocus();
            return;
        }

        dialog.show();
        if (connectivity()) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        Log.i(TAG, "onComplete: User Created");

                        uid = mAuth.getCurrentUser().getUid();
                        submitData(name, password, email, contact, address);
                    } else {
                        dialog.dismiss();
                        //If the Email Already exists
                        if (task.getException() instanceof FirebaseAuthActionCodeException) {
                            Toast.makeText(Register.this, "You are already registered!", Toast.LENGTH_LONG).show();
                        }
                        Toast.makeText(Register.this, "User Creation Failed, Try Again!", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onComplete: User Creation Error" + task.getException().getMessage());
                    }
                }
            });

        } else {
            dialog.dismiss();
            Toast.makeText(Register.this, "Please check your Internet Connection.", Toast.LENGTH_LONG).show();
        }
    }

    //submit data over database and create a new user.
    private void submitData(String name, final String password, final String email, String contact, String address) {
        dialog.show();
        id = uid;
        site = (String) siteSpinner.getSelectedItem();

        //Creating user's submitData data on USER_DATA node as user.
        final UserReg user = new UserReg(email, password, uid, name, contact, address, site);
        ref.child("User").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Setting TYPE of USER data on USERS NODE
                    User users = new User(email, "User");
                    userRef.child(id).setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Register.this, "Welcome", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    //remove the user and register him again.
                    //Remove the user and inform the user to register again!
                    final FirebaseUser user = mAuth.getCurrentUser();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Calling delete to remove the user and wait for a result.
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //User removed!
                                                //Inform the user to register again.
                                                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                                                builder.setTitle("Alert");
                                                builder.setMessage("User Registration Failed. Click OK and register again.")
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                recreate();
                                                            }
                                                        });
                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();
                                                recreate();
                                            } else {
                                                //Handle the exception
                                                Log.d(TAG, "onComplete: user removal error: " + task.getException());
                                                Toast.makeText(Register.this, "onComplete: user removal error: " + task.getException(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });
                }
            }
        });


        dialog.dismiss();
        //send user to the Main Screen
        startActivity(new Intent(this, UserMainScreen.class));
        finish();
    }

    public boolean connectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}