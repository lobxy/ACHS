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
import java.util.Locale;

public class ComplainForm extends AppCompatActivity {
    private static final String TAG = "Complain Form Activity";

    private EditText edit_description;

    private TextView showTime;

    private String mName, mAddress, mSite, mContact, mEmail, mVisitTime,
            mComplaintInitTime, mDescription, mType, mDate, mTime, mUid, mSupervisorId,
            mSuperivisorName;

    DatabaseReference complaintReference;     //For Complaints
    DatabaseReference userComplaintsDatabaseReference;     //For User's complaints section.

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain_form);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Working...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getCurrentUser().getUid();

        mType = getIntent().getStringExtra("Type");
        complaintReference = FirebaseDatabase.getInstance().getReference("Complaints_Unresolved");
        userComplaintsDatabaseReference = FirebaseDatabase.getInstance().getReference("User_complaints").child(mUid);

        edit_description = findViewById(R.id.form_edit_desciption);
        showTime = findViewById(R.id.form_text_show_time);

        Button btn_submit = findViewById(R.id.form_button_submit);

        showTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDate = "";
                mTime = "";

                showTime.setText("Pick");

                //hide the keyboard
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                newDatePicker();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Process complaint
                validation();
            }
        });

    }

    private void newDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                //get date stuff done here.
                mDate = (day + "-" + (month + 1) + "-" + year);
                Log.i(TAG, "onTimeSet: mDate:" + mDate);

                newTimePicker();
            }
        };

        DatePickerDialog dateDialog = new DatePickerDialog(this, dateSetListener, mYear, mMonth, mDay);
        dateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                showTime.setText("Pick");
            }
        });
        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() + 43200 * 1000);

        dateDialog.show();
    }

    private void newTimePicker() {
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                if (hourOfDay > 12) {
                    hourOfDay -= 12;
                    mTime = (hourOfDay + ":" + minute + ":" + "PM");
                } else {
                    mTime = (hourOfDay + ":" + minute + ":" + "AM");
                }

                Log.i(TAG, "onTimeSet: mDate:" + mTime);

                mVisitTime = mDate + " @ " + mTime;

                showTime.setText(mVisitTime);
            }
        };

        TimePickerDialog dialog = new TimePickerDialog(this, timeSetListener, mHour, mMinute, false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mDate = "";
                showTime.setText("Pick");
            }
        });

        dialog.show();

    }

    public void validation() {
        mDescription = edit_description.getText().toString().trim();

        if (mDescription.isEmpty()) {
            edit_description.setError("Description is empty ");
            edit_description.requestFocus();
            return;
        }
        if (showTime.getText().toString().equals("Pick") || mDate.isEmpty() || mTime.isEmpty()) {
            Toast.makeText(this, "Please pick a visit time.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Upload complaint form data to the database
        if (connectivity()) uploadComplaintData(mName, mAddress, mSite, mContact, mEmail);

        else showAlert("Alert", "Please connect to the internet");

    }

    private void getUserData() {
        //User database reference
        dialog.show();

        DatabaseReference userDataReference = FirebaseDatabase.getInstance().getReference().child("User_Data").child("User").child(mUid);
        userDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();

                mName = dataSnapshot.child("name").getValue(String.class);
                mAddress = dataSnapshot.child("address").getValue(String.class);
                mSite = dataSnapshot.child("site").getValue(String.class);
                mContact = dataSnapshot.child("contact").getValue(String.class);
                mEmail = dataSnapshot.child("email").getValue(String.class);

                Log.i(TAG, "onDataChange: Site_" + mSite + "Type_: " + mType);
                //Update the reference!
                complaintReference = complaintReference.child(mSite).child(mType);

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
        //Get current mTime and mDate.
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
        mComplaintInitTime = currentDate + " @ " + currentTime;

        dialog.show();

        final String id = complaintReference.push().getKey();
        final String happyCode = HappyCode();

        //create a new object of model class, feed the data to it.
        final Complain complain = new Complain(mUid, id, name, email, address, contact, site, mType, mDescription, mVisitTime, mComplaintInitTime,
                happyCode, "Unresolved", "Not Assigned",
                "No Data");

        assert id != null;
        //Upload data to the firebase
        complaintReference.child(id).setValue(complain).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "onSuccess: Form Data submission successful");

                //feed data on mUser complains model class.
                UserComplains userComplains = new UserComplains(mType, id, happyCode, mComplaintInitTime,
                        "Unresolved", "No Data", mSupervisorId, mSuperivisorName);

                //Save data to mUser complaints database node, for mUser's MY COMPLAINTS section.
                userComplaintsDatabaseReference.child(id).setValue(userComplains).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {

                            //assign supervisor;
                            updateCount(site, complain, happyCode);

                            Log.i(TAG, "onSuccess: User's complain data saving Success");
                        } else {
                            //remove mUser complains from the UNRESOLVED complain pool.
                            // complaintReference.child(id).removeValue();
                            showAlert("Error", task.getException().getMessage());
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                //no rollback needed.
                Log.i(TAG, "onFailure: Form data submission error " + e.getMessage());
                showAlert("Error", e.getMessage());
            }
        });

    }

    private void updateCount(final String site, final Complain complain, final String happyCode) {
        dialog.show();
        //Supervisors ref
        final DatabaseReference supervisorRef = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors").child(site);
        final DatabaseReference supervisorComplainRef = FirebaseDatabase.getInstance().getReference("Supervisors_Complaint_Slot");

        //get the supervisor with lowest complain count right now.
        Query query = supervisorRef.orderByChild("count").limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mSupervisorId = "visor";
                long count = 0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Supervisor visor = snapshot.getValue(Supervisor.class);
                        count = visor.getCount();
                        mSupervisorId = visor.getUid();
                        mSuperivisorName = visor.getName();
                    }
                    count++;

                    supervisorRef.child(mSupervisorId).child("count").setValue(count).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "onComplete: count updated ");

                                assignComplainToSupervisor(supervisorComplainRef, mSupervisorId, complain, happyCode);

                            } else {
                                Log.i(TAG, "onComplete: couldn't update count");
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

    private void assignComplainToSupervisor(DatabaseReference supervisorComplainRef, final String finalSid, Complain complain, final String happyCode) {

        supervisorComplainRef.child(finalSid).child(complain.getComplaintID()).setValue(complain)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();

                        if (task.isSuccessful()) {
                            //Show HAPPY CODE to the mUser!

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
                            //complain not set to supervisor, reset the count variable.
                            resetCount(finalSid);
                            Log.i(TAG, "onComplete: Supervisor not assigned: Error: " + task.getException().getLocalizedMessage());
                            showAlert("Error", task.getException().getMessage());
                        }
                    }
                });
    }

    private void resetCount(String finalSid) {
        final DatabaseReference resetReference = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors").child(mSite).child(finalSid);
        resetReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = 0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Supervisor visor = snapshot.getValue(Supervisor.class);
                        count = visor.getCount();
                    }
                    count--;

                    resetReference.child("count").setValue(count).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "resetCount: count updated ");
                            } else {
                                Log.i(TAG, "resetCount:count not update ");
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showAlert("Error", databaseError.getMessage());
            }
        });
        showAlert("Error", "Couldn't process complain.\nTry again later");
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