package com.lobxy.achs.Supervisor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Model.Supervisor;
import com.lobxy.achs.R;
import com.lobxy.achs.Utils.Connection;
import com.lobxy.achs.Utils.ShowAlertDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SupervisorDetail extends AppCompatActivity {

//    Set the status in unresolved and user 's complaints to RESOLVED.
//    Set the complaintCompletionTime in both user's complaints and UNRESOLVED node of complaint.
//    Move the complaint from UNRESOLVED to RESOLVED.
//    Remove the complaint from UNRESOLVED.

    private static final String TAG = "Supervisor Complaint";

    private TextView text_name, text_complaintType, text_address, text_description, text_complaintTime, text_visitTime,
            text_commonArea, text_contact, text_email;

    private String mComplaintId, mComplaintHappyCode, mSupervisorID, mComplaintSite, mComplaintType, mUserId;

    private EditText edit_happyCode;

    private FirebaseAuth auth;
    private DatabaseReference fromPath, toPath, supervisorRef, userRef;

    private ProgressDialog progressDialog;

    private ShowAlertDialog alertDialog;

    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_detail);
        connection = new Connection(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Working...");
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);

        text_name = findViewById(R.id.visor_request_text_name);
        text_contact = findViewById(R.id.visor_request_text_phone);
        text_email = findViewById(R.id.visor_request_text_email);
        text_complaintType = findViewById(R.id.visor_request_text_complaintType);
        text_address = findViewById(R.id.visor_request_text_address);
        text_complaintTime = findViewById(R.id.visor_request_text_complainttime);
        text_visitTime = findViewById(R.id.visor_request_text_visitTime);
        text_commonArea = findViewById(R.id.visor_request_text_commonArea);
        text_description = findViewById(R.id.visor_request_text_description);

        edit_happyCode = findViewById(R.id.visor_edit_happy_code);

        auth = FirebaseAuth.getInstance();
        fromPath = FirebaseDatabase.getInstance().getReference("Complaints_Unresolved");
        toPath = FirebaseDatabase.getInstance().getReference("Complaints_Resolved");
        supervisorRef = FirebaseDatabase.getInstance().getReference("Supervisors_Complaint_Slot");
        userRef = FirebaseDatabase.getInstance().getReference("User_complaints");

        alertDialog = new ShowAlertDialog(this);

        Button button_submit = findViewById(R.id.visor_button_submit);
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValues();
            }
        });

    }

    private void updateValues() {
        String visor_code = edit_happyCode.getText().toString().trim();
        Log.i(TAG, "onClick: visor_code" + visor_code);
        Log.i(TAG, "onClick: happyCode " + mComplaintHappyCode);

        if (visor_code.equals(mComplaintHappyCode)) {
            progressDialog.show();
            if (connection.check()) {

                fromPath = fromPath.child(mComplaintSite).child(mComplaintType).child(mComplaintId);
                toPath = toPath.child(mComplaintSite).child(mComplaintType).child(mComplaintId);
                userRef = userRef.child(mUserId).child(mComplaintId);

                //get Current time
                String time = getCurrentTime();

                Log.i(TAG, "onClick: time" + time);

                userRef.child("completionStatus").setValue("Resolved");
                userRef.child("complaintCompletionTime").setValue(time);

                fromPath.child("completionStatus").setValue("Resolved");
                fromPath.child("complaintCompletionTime").setValue(time);

                progressDialog.dismiss();
                Log.i(TAG, "updateValues: values set");

                //move to complain from unresolved node to resolved node
                moveRecord(fromPath, toPath);

            } else {
                alertDialog.showAlertDialog("Alert", "Please make sure you are connected to internet!");
            }
        } else {
            Toast.makeText(SupervisorDetail.this, "Happy Code is not correct.", Toast.LENGTH_LONG).show();
        }

    }

    public String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        Date time = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = dateFormat.format(time);
        formattedDate = android.text.format.DateFormat.format("yyyy-MM-dd", new Date()) + ":" + formattedDate;
        return formattedDate;
    }

    private void moveRecord(final DatabaseReference fromPath, final DatabaseReference toPath) {
        progressDialog.show();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isComplete()) {
                            //remove the complain form the Complaint_Unconfirmed node.
                            fromPath.removeValue();

                            //remove the complain from the supervisor_complaint_node
                            supervisorRef.child(mSupervisorID).child(mComplaintId).removeValue();
                            Log.i(TAG, "onComplete: complaint moved");
                            updateCount();

                        } else {
                            Toast.makeText(SupervisorDetail.this, "Move Record Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Copy failed! " + task.getException().getLocalizedMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(SupervisorDetail.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        fromPath.addListenerForSingleValueEvent(valueEventListener);
    }

    private void updateCount() {
        progressDialog.show();

        //update count value.
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors").child(mComplaintSite).child(mSupervisorID);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = 0;
                if (dataSnapshot.exists()) {
                    Supervisor supervisor = dataSnapshot.getValue(Supervisor.class);
                    count = supervisor.getCount();

                    ref.child("count").setValue(--count).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();

                            if (task.isSuccessful()) {
                                Log.i(TAG, "onComplete: count updated");
                                Log.i(TAG, "onComplete: complaint resolved");

                                Toast.makeText(SupervisorDetail.this, "Complaint Resolved", Toast.LENGTH_SHORT).show();

                                finish();
                            } else Log.i(TAG, "onComplete: couldn't update count");
                        }
                    });
                } else {
                    progressDialog.dismiss();

                    Log.i(TAG, "onDataChange: supervisor data doesn't exists");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();

                alertDialog.showAlertDialog("Error", databaseError.getMessage());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        setData();
    }

    private void setData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {

            String user_name = bundle.getString("Complaint_NAME");
            String phone = bundle.getString("Complaint_CONTACT");
            String email = bundle.getString("Complaint_EMAIL");
            String type = bundle.getString("Complaint_TYPE");
            String address = bundle.getString("Complaint_ADDRESS");
            String timeOfComplaint = bundle.getString("Complaint_INIT");
            String visitTime = bundle.getString("Complaint_VISITTIME");
            String commonArea = String.valueOf(bundle.getBoolean("Complaint_COMMONAREA"));
            String description = bundle.getString("Complaint_DESC");
            String site = bundle.getString("Complaint_SITE");

            mUserId = bundle.getString("Complaint_USERID");
            mComplaintId = bundle.getString("Complaint_ID");
            mComplaintHappyCode = bundle.getString("Complaint_HAPPYCODE");

            text_name.setText(user_name);
            text_complaintType.setText(type);
            text_address.setText(address + "\n" + site);
            text_description.setText(description);
            text_complaintTime.setText(String.valueOf(timeOfComplaint));
            text_commonArea.setText(String.valueOf(commonArea));
            text_visitTime.setText(String.valueOf(visitTime));
            text_contact.setText(phone);
            text_email.setText(email);

            mComplaintSite = site;
            mComplaintType = type;

            mSupervisorID = auth.getCurrentUser().getUid();

        } else {
            Toast.makeText(this, "Bundle Error", Toast.LENGTH_SHORT).show();
        }
    }

    //completed//transfered/////crash.

}