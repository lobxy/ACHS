package com.lobxy.achs.User;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Model.UserReg;
import com.lobxy.achs.R;
import com.lobxy.achs.RegisterActivity;
import com.lobxy.achs.Utils.Connection;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity Activity";
    ProgressDialog dialog;

    DatabaseReference ref;
    FirebaseAuth auth;
    String uid;
    String name, address, contact, site, password;
    Button cancel, edit, save;
    EditText addressField, contactField, nameField;
    Spinner siteSpinner;

    ArrayAdapter<String> dataAdapter;

    Connection connection = new Connection(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        connection = new Connection(this);

        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("User_Data").child("User").child(uid);

        siteSpinner = findViewById(R.id.profile_spinner_site);

        addressField = findViewById(R.id.profile_edit_address);
        contactField = findViewById(R.id.profile_edit_contact);
        nameField = findViewById(R.id.profile_edit_name);

        nameField.setBackground(null);
        cancel = findViewById(R.id.profile_button_cancel);
        edit = findViewById(R.id.profile_button_edit);
        save = findViewById(R.id.profile_button_save);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Working...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);

        //Spinner setup
        String[] sites = getResources().getStringArray(R.array.sites_name);
        dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sites);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        siteSpinner.setAdapter(dataAdapter);
        siteSpinner.setEnabled(false);

        //Show loading screen and get the users data from the Database;

        if (connection.check()) {
            LoadData();
        } else {
            Toast.makeText(this, "Please check your Internet Connection.", Toast.LENGTH_SHORT).show();
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMode();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void viewMode() {
        cancel.setVisibility(View.GONE);
        save.setVisibility(View.GONE);
        edit.setVisibility(View.VISIBLE);

        nameField.setFocusableInTouchMode(false);
        addressField.setFocusableInTouchMode(false);
        contactField.setFocusableInTouchMode(false);
        addressField.setEnabled(false);
        contactField.setEnabled(false);
        nameField.setEnabled(false);
        siteSpinner.setClickable(false);
        siteSpinner.setEnabled(false);
    }

    private void editMode() {
        cancel.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        edit.setVisibility(View.GONE);

        addressField.setFocusableInTouchMode(true);
        contactField.setFocusableInTouchMode(true);
        nameField.setFocusableInTouchMode(true);
        addressField.setEnabled(true);
        contactField.setEnabled(true);
        nameField.setEnabled(true);
        siteSpinner.setClickable(true);
        siteSpinner.setEnabled(true);
    }

    private void LoadData() {
        dialog.show();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    name = dataSnapshot.child("name").getValue(String.class);
                    address = dataSnapshot.child("address").getValue(String.class);
                    contact = dataSnapshot.child("contact").getValue(String.class);
                    site = dataSnapshot.child("site").getValue(String.class);
                    password = dataSnapshot.child("password").getValue(String.class);
                    //Set the data on the UI.
                    nameField.setText(name);
                    addressField.setText(address);
                    contactField.setText(contact);
                    siteSpinner.setSelection(dataAdapter.getPosition(site));
                    dialog.dismiss();

                } else {
                    Toast.makeText(ProfileActivity.this, "User Data doesn't exists! Contact Support.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    //Ask the user to enter registration details again.
                    startActivity(new Intent(ProfileActivity.this, RegisterActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showAlert("Error", databaseError.getMessage());
                dialog.dismiss();
            }
        });
    }

    private void validateData() {
        name = nameField.getText().toString();
        address = addressField.getText().toString();
        contact = contactField.getText().toString();

        if (name.isEmpty()) {
            nameField.requestFocus();
            nameField.setError("Field is empty.No data change will occur!");
            return;
        }
        if (address.isEmpty()) {
            addressField.requestFocus();
            addressField.setError("Field is empty.No data change will occur!");
            return;

        }
        if (contact.isEmpty()) {
            contactField.requestFocus();
            contactField.setError("Field is empty.No data change will occur!");
            return;

        }
        if (contact.length() < 10) {
            contactField.requestFocus();
            contactField.setError("Contact is less than 10 characters.");
            return;
        }
        if (siteSpinner.getSelectedItem().toString().trim().equals("Please Select A Site")) {
            Toast.makeText(this, "Please select a Site", Toast.LENGTH_LONG).show();
            return;
        }
        site = siteSpinner.getSelectedItem().toString();

        saveData(auth.getCurrentUser().getEmail(), uid, name, contact, address, site);
    }


    private void saveData(final String email, final String uid, final String name, final String contact, final String address, final String site) {
        //Show an alert dialog first, if pressed confirm, save the data.

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Alert!!");
        builder.setMessage("Click confirm to update your information.")
                .setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Check internet connectivity and Update the data on user data node reference!

                        if (connection.check()) {
                            //Update the data.Use Hash-Map if required.
                            dialog.show();
                            UserReg userRegister = new UserReg(email, password, uid, name, contact, address, site);
                            ref.setValue(userRegister).addOnSuccessListener(ProfileActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "onSuccess: Data Update Success!");
                                    Toast.makeText(ProfileActivity.this, "Information Updated!", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(ProfileActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "onFailure: Data Update failure: " + e.getMessage());
                                    showAlert("Error", e.getMessage());

                                }
                            });
                            dialog.dismiss();
                        } else {
                            showAlert("Alert", "Please connect to the internet");

                        }
                        //set the mode to view mode
                        viewMode();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

}
