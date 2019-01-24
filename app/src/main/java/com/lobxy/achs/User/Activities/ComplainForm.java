package com.lobxy.achs.User.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Model.Complain;
import com.lobxy.achs.Model.Supervisor;
import com.lobxy.achs.Model.UserComplains;
import com.lobxy.achs.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ComplainForm extends AppCompatActivity {
    private static final String TAG = "Complain Form Activity";

    EditText edit_description;

    TextView showTime;

    public String des, type, date, time, uid;
    String name, address, site, contact, email, visitTime = "Select", complaintInitTime;

    DatabaseReference databaseReference;     //For Complaints
    DatabaseReference userComplaintsDatabaseReference;     //For User's complaints section.

    ProgressDialog dialog;

    FirebaseAuth mAuth;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain_form);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Working...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();

        type = getIntent().getStringExtra("Type");
        databaseReference = FirebaseDatabase.getInstance().getReference("Complaints_Unresolved");
        userComplaintsDatabaseReference = FirebaseDatabase.getInstance().getReference("User_complaints").child(uid);

        edit_description = findViewById(R.id.form_edit_desciption);
        showTime = findViewById(R.id.form_text_show_time);

        Button btn_submit = findViewById(R.id.form_button_submit);

        //Get current time and date.
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
        complaintInitTime = currentDate + " @ " + currentTime;

        showTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide the keyboard
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                // Get Current Date
                visitTime = null;
                showTime.setText("Select");
                getDate();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Process request
                Validation();

            }
        });

    }

    private void getDate() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        date = (dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        visitTime = date;
                        Log.i(TAG, "onTimeSet: date:" + date);
                        getTime();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void getTime() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        time = (hourOfDay + ":" + minute);
                        visitTime = visitTime + " @ " + time;
                        showTime.setText(visitTime);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void Validation() {
        des = edit_description.getText().toString().trim();
        if (des.isEmpty()) {
            edit_description.setError("Description Field is empty ");
            edit_description.requestFocus();
            return;
        }
        if (visitTime.equals("Select") || visitTime.isEmpty()) {
            Toast.makeText(this, "Please pick a visit time.", Toast.LENGTH_SHORT).show();
            return;
        }
        //Upload complaint form data to the database
        if (connectivity()) {
            uploadComplaintData(name, address, site, contact, email);
        } else {
            showAlert("Alert", "Please connect to the internet");
        }

    }

    private void getUserData() {
        //User database reference
        dialog.show();

        DatabaseReference userDataReference = FirebaseDatabase.getInstance().getReference().child("User_Data").child("User").child(uid);
        userDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();

                name = dataSnapshot.child("name").getValue(String.class);
                address = dataSnapshot.child("address").getValue(String.class);
                site = dataSnapshot.child("site").getValue(String.class);
                contact = dataSnapshot.child("contact").getValue(String.class);
                email = dataSnapshot.child("email").getValue(String.class);

                Log.i(TAG, "onDataChange: Site_" + site + "Type_: " + type);
                //Update the reference!
                databaseReference = databaseReference.child(site).child(type);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                Log.i(TAG, "onCancelled: Upload Data error " + databaseError.getMessage());
                showAlert("Error", databaseError.getMessage());
            }
        });
    }

    public void uploadComplaintData(String name, String address, final String site, String contact, String email) {

        dialog.show();

        final String id = databaseReference.push().getKey();
        final String happyCode = HappyCode();

        //create a new object of model class, feed the data to it.
        final Complain complain = new Complain(uid, id, name, email, address, contact, site, type, des, visitTime, complaintInitTime,
                happyCode, "Unresolved", "Not Assigned",
                "No Data");

        assert id != null;
        //Upload data to the firebase
        databaseReference.child(id).setValue(complain).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "onSuccess: Form Data submission successful");

                //feed data on user complains model class.
                UserComplains userComplains = new UserComplains(type, id, happyCode, complaintInitTime,
                        "Unresolved", "No Data");

                //Save data to user complaints database node, for user's MY COMPLAINTS section.
                userComplaintsDatabaseReference.child(id).setValue(userComplains).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.i(TAG, "onFailure: User's complain data saving error:" + e.getMessage());
                        showAlert("Error", e.getMessage());

                        dialog.dismiss();

                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();

                        //assign supervisor;
                        assignSupervisor(site, complain, happyCode);

                        Log.i(TAG, "onSuccess: User's complain data saving Success");
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();

                Log.i(TAG, "onFailure: Form data submission error " + e.getMessage());
                showAlert("Error", e.getMessage());
            }
        });

    }

    private void assignSupervisor(final String site, final Complain complain, final String happyCode) {
        dialog.show();
        //Supervisors ref
        final DatabaseReference supervisorRef = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors").child(site);
        final DatabaseReference supervisorComplainRef = FirebaseDatabase.getInstance().getReference("Supervisors_Complaint_Slot").child(site);

        //get the supervisor with lowest complain count right now.
        Query query = supervisorRef.orderByChild("count").limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Map<String, String> map = new HashMap<>();

                String sid = "visor";
                long count = 0;
                if (dataSnapshot.exists()) {
                    Log.i(TAG, "onDataChange: datasnapshot: " + dataSnapshot);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Supervisor visor = snapshot.getValue(Supervisor.class);
                        sid = visor.getUid();
                        count = visor.getCount();
                        count++;
                        map.put("contact", visor.getContact());
                        map.put("count", String.valueOf(count));
                        map.put("email", visor.getEmail());
                        map.put("name", visor.getName());
                        map.put("password", visor.getPassword());
                        map.put("site", visor.getSite());
                        map.put("uid", visor.getUid());
                    }

                    //add count++, after successful supervisor assignment;
                    Log.i(TAG, "onDataChange: supervisor id: " + sid);

                    final String finalSid = sid;
                    supervisorComplainRef.child(sid).child(complain.getComplaintID()).setValue(complain).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();

                            if (task.isSuccessful()) {
                                //update count here.
                                supervisorRef.child(finalSid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.i(TAG, "onComplete: Count updated");

                                            Log.i(TAG, "onComplete: Supervisor Assigned");
                                            Toast.makeText(ComplainForm.this, "Supervisor Assigned", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(ComplainForm.this, "Complain Submitted", Toast.LENGTH_SHORT).show();
                                            //Show HAPPY CODE to the user!
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ComplainForm.this);
                                            builder.setTitle(happyCode);
                                            builder.setMessage("Please take a note of this HAPPY CODE or you can find it in MY COMPLAINTS.")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                            startActivity(new Intent(ComplainForm.this, UserMainScreen.class));
                                                            finish();
                                                        }
                                                    });
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();

                                        } else {
                                            Log.i(TAG, "onComplete: count update failed: " + task.getException().getLocalizedMessage());
                                            showAlert("Error", task.getException().getMessage());

                                        }
                                    }
                                });


                            } else {
                                Log.i(TAG, "onComplete: Supervisor not assigned: Error: " + task.getException().getLocalizedMessage());
                                showAlert("Error", task.getException().getMessage());

                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();

                Log.i(TAG, "onCancelled: Query error: " + databaseError.getMessage());
                showAlert("Error", databaseError.getMessage());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserData();
    }

    private String HappyCode() {
        //generate a 6 digit integer 1000 <10000
        int randomPIN = (int) (Math.random() * 90000) + 10000;
        return (String.valueOf(randomPIN));
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public boolean connectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
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