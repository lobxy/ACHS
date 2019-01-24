package com.lobxy.achs.Admin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lobxy.achs.Model.Supervisor;
import com.lobxy.achs.R;

import java.util.HashMap;

public class AddSupervisor extends AppCompatActivity {

    private static final String TAG = "Supervisor Add Activity";
    EditText edit_name, edit_email, edit_contact, edit_pwd, edit_cPwd;
    Button cancel, submit;
    String name, email, password, contact;
    String site;

    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseAuth mAuth2;


    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_add);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.setMessage("Working...");

        edit_contact = findViewById(R.id.visor_edit_contact);
        edit_cPwd = findViewById(R.id.visor_edit_cPwd);
        edit_pwd = findViewById(R.id.visor_edit_pwd);
        edit_name = findViewById(R.id.visor_edit_name);
        edit_email = findViewById(R.id.visor_edit_email);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        cancel = findViewById(R.id.visor_button_cancel);
        submit = findViewById(R.id.visor_button_submit);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });

    }

    private void validation() {
        name = edit_name.getText().toString();
        contact = edit_contact.getText().toString();
        password = edit_pwd.getText().toString();
        email = edit_email.getText().toString();
        String confirmPassword = edit_cPwd.getText().toString();

        if (name.isEmpty()) {
            edit_name.requestFocus();
            edit_name.setError("Field is Empty!");
            return;
        }
        if (contact.isEmpty()) {
            edit_contact.requestFocus();
            edit_contact.setError("Field is Empty!");
            return;
        }
        if (email.isEmpty()) {
            edit_email.requestFocus();
            edit_email.setError("Field is Empty!");
            return;
        }
        if (password.isEmpty()) {
            edit_pwd.requestFocus();
            edit_pwd.setError("Field is Empty!");
            return;
        }
        if (password.length() < 6) {
            edit_pwd.requestFocus();
            edit_pwd.setError("Minimum password length is 6 characters!");
            return;
        }
        if (confirmPassword.isEmpty()) {
            edit_cPwd.requestFocus();
            edit_cPwd.setError("Field is Empty!");
            return;
        }
        if (!confirmPassword.equals(password)) {
            edit_cPwd.requestFocus();
            edit_cPwd.setError("Password's don't match!");
            return;
        }

        //get the site chosen from listSupervisor's alert dialog.
        Intent intent = getIntent();
        site = intent.getStringExtra("SiteChosen");

        if (site.isEmpty()) {
            Toast.makeText(this, "Site not selected", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //Submit the data
        createUser();

    }

    private void createUser() {
        dialog.show();


        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://achs-efd78.firebaseio.com/")
                .setApiKey("AIzaSyApIjfZw1vGCbPU0Y8Nc1GLwc88-8TEizw")
                .setApplicationId("achs-efd78").build();

        try {
            FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "ACHS");
            mAuth2 = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e) {
            mAuth2 = FirebaseAuth.getInstance(FirebaseApp.getInstance("ACHS"));
        }

        mAuth2.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    //Submit the data to the firebaseDatabase.
                    FirebaseUser user = task.getResult().getUser();
                    String uid = user.getUid();
                    Log.d(TAG, "onComplete: Created supervisor uid: " + user.getUid());
                    submitData(uid);
                    mAuth2.signOut();
                } else {
                    dialog.dismiss();
                    Toast.makeText(AddSupervisor.this, "Supervisor registration error :" + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onComplete: : Supervisor registration error: " + task.getException().getMessage());
                }
            }
        });

    }

    private void submitData(final String uid) {
        dialog.show();

        final Supervisor supervisor = new Supervisor(name, email, contact, site, uid, password, 0);
        reference.child("User_Data/Supervisors").child(site).child(uid).setValue(supervisor).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("email", email);
                    map.put("type", "Supervisor");
                    reference.child("Users").child(uid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(AddSupervisor.this, "Supervisor Created", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(AddSupervisor.this, ListSupervisor.class));
                                finish();

                            } else {
                                Toast.makeText(AddSupervisor.this, "Type Set error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                Log.i(TAG, "onComplete: Type Set error: " + task.getException().getLocalizedMessage());
                            }
                        }
                    });

                } else {
                    dialog.dismiss();
                    Log.d(TAG, "onComplete: submitData Error: " + task.getException().getMessage());
                    Toast.makeText(AddSupervisor.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();

                    //Remove the user and inform the user to register again!
                    final FirebaseUser user = auth.getCurrentUser();
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
                                                AlertDialog.Builder builder = new AlertDialog.Builder(AddSupervisor.this);
                                                builder.setTitle("Alert");
                                                builder.setMessage("Supervisor Creation Failed. Click OK and register again.")
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                recreate();
                                                            }
                                                        });
                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();
                                            } else {
                                                //Handle the exception
                                                Log.d(TAG, "onComplete: user removal error: " + task.getException());
                                                Toast.makeText(AddSupervisor.this, "onComplete: user removal error: " + task.getException(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });
                }
            }
        });
    }


}
