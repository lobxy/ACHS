package com.lobxy.achs.Supervisor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.lobxy.achs.Utils.ShowAlertDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SupervisorDetail extends AppCompatActivity {

    private static final String TAG = "Supervisor Complaint";

    TextView vName, vComplaintType, vAddress, vDes, vTimeOfComplaint, vVisitTime,
            vCommonArea, vContact, vEmail;

    String complaintId, complaint_happyCode, vSupervisorID, complaintSite, complaintType,
            userID;

    EditText inputHappyCode;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference fromPath, toPath, supervisorRef, userRef;

    ProgressDialog dialog;

    ShowAlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_detail);

        vName = findViewById(R.id.visor_request_text_name);
        vContact = findViewById(R.id.visor_request_text_phone);
        vEmail = findViewById(R.id.visor_request_text_email);
        vComplaintType = findViewById(R.id.visor_request_text_complaintType);
        vAddress = findViewById(R.id.visor_request_text_address);
        vTimeOfComplaint = findViewById(R.id.visor_request_text_complainttime);
        vVisitTime = findViewById(R.id.visor_request_text_visitTime);
        vCommonArea = findViewById(R.id.visor_request_text_commonArea);
        vDes = findViewById(R.id.visor_request_text_description);

        Button button_submit = findViewById(R.id.visor_button_submit);
        inputHappyCode = findViewById(R.id.visor_edit_happy_code);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        fromPath = database.getReference("Complaints_Unresolved");
        toPath = database.getReference("Complaints_Resolved");
        supervisorRef = database.getReference("Supervisors_Complaint_Slot");
        userRef = database.getReference("User_complaints");

        alertDialog = new ShowAlertDialog(this);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Resolving Complaint...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivity()) updateCount();
                else {
                    Toast.makeText(SupervisorDetail.this, "Not connected to internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete()) {
                            //remove the complain form the Complaint_Unconfirmed node.
                            fromPath.removeValue();

                            //remove the complain from the supervisor_complaint_node
                            supervisorRef.child(vSupervisorID).child(complaintId).removeValue();

                            updateCount();

                            Toast.makeText(SupervisorDetail.this, "Complaint Transferred", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SupervisorDetail.this, "Move Record Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Copy failed! " + task.getException().getLocalizedMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SupervisorDetail.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        fromPath.addListenerForSingleValueEvent(valueEventListener);
    }

    private void updateCount() {

        //update count value.
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors").child(complaintSite);

        ref.child(vSupervisorID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = 0;
                if (dataSnapshot.exists()) {
                    Supervisor supervisor = dataSnapshot.getValue(Supervisor.class);
                    count = supervisor.getCount();

                    ref.child("count").setValue(count--).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "onComplete: updated count");

                            } else Log.i(TAG, "onComplete: couldn't update count");
                        }
                    });
                } else {
                    Log.i(TAG, "onDataChange: supervisor data doesn't exists");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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

            userID = bundle.getString("Complaint_USERID");
            complaintId = bundle.getString("Complaint_ID");
            complaint_happyCode = bundle.getString("Complaint_HAPPYCODE");

            vName.setText(user_name);
            vComplaintType.setText(type);
            vAddress.setText(address + "\n" + site);
            vDes.setText(description);
            vTimeOfComplaint.setText(String.valueOf(timeOfComplaint));
            vCommonArea.setText(String.valueOf(commonArea));
            vVisitTime.setText(String.valueOf(visitTime));
            vContact.setText(phone);
            vEmail.setText(email);

            complaintSite = site;
            complaintType = type;

            vSupervisorID = auth.getCurrentUser().getUid();

        } else {
            Toast.makeText(this, "Bundle Error", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean connectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

}