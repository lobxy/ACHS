package com.lobxy.achs.Supervisor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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
import com.lobxy.achs.Adapters.ComplaintAdapter;
import com.lobxy.achs.LoginActivity;
import com.lobxy.achs.Model.Complain;
import com.lobxy.achs.R;
import com.lobxy.achs.Utils.Connection;
import com.lobxy.achs.Utils.ShowAlertDialog;

import java.util.ArrayList;
import java.util.List;

public class SupervisorMain extends AppCompatActivity {
    private static final String TAG = "SupervisorMain";
    //Supervisor ke request assigned me se request id nikalni he,then usse confirmed wale me search krna he,
    // and after that uski value using bundle send krni he dusri activity me.

    private List<Complain> mComplaintList;
    private ListView listView_complaints;

    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    private TextView text_noComplaint;
    private ProgressDialog progressDialog;

    private String mUid, mSite;

    private ShowAlertDialog alertDialog;

    private Button buttonAvailability;

    private boolean availability = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_main);

        alertDialog = new ShowAlertDialog(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Working");
        progressDialog.setInverseBackgroundForced(false);
        progressDialog.setCancelable(false);

        listView_complaints = findViewById(R.id.visor_listView);
        text_noComplaint = findViewById(R.id.noComplaintsSupervisorMain);

        buttonAvailability = findViewById(R.id.visor_changeAvailability);
        buttonAvailability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeAvailabilityStatus();
            }
        });

        mComplaintList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Supervisors_Complaint_Slot");
        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getCurrentUser().getUid();

        listView_complaints.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Complain complaints = mComplaintList.get(i);

                Intent intent = new Intent(SupervisorMain.this, SupervisorDetail.class);
                Bundle bundle = new Bundle();

                bundle.putString("Complaint_CONTACT", complaints.getContact());
                bundle.putString("Complaint_EMAIL", complaints.getEmail());
                bundle.putString("Complaint_TYPE", complaints.getType());
                bundle.putString("Complaint_NAME", complaints.getName());
                bundle.putString("Complaint_DESC", complaints.getDescription());
                bundle.putString("Complaint_ADDRESS", complaints.getAddress());
                bundle.putString("Complaint_INIT", complaints.getComplaintInitTime());
                bundle.putString("Complaint_ID", complaints.getComplaintID());
                bundle.putString("Complaint_SITE", complaints.getSite());
                bundle.putString("Complaint_VISITTIME", complaints.getVisitTime());
                bundle.putString("Complaint_USERID", complaints.getUserId());
                bundle.putString("Complaint_HAPPYCODE", complaints.getHappyCode());

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Connection connection = new Connection(this);

        if (connection.check()) {
            getData();
            getSite();
        } else {
            Toast.makeText(this, "Please connect to internet", Toast.LENGTH_LONG).show();
        }
    }

    private void getData() {
        progressDialog.show();
        reference.child(mUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                mComplaintList.clear();

                for (DataSnapshot reqSnap : dataSnapshot.getChildren()) {
                    Complain complaints = reqSnap.getValue(Complain.class);
                    mComplaintList.add(complaints);
                }
                ComplaintAdapter adapter = new ComplaintAdapter(SupervisorMain.this, mComplaintList);
                listView_complaints.setAdapter(adapter);

                if (adapter.isEmpty()) {
                    text_noComplaint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                alertDialog.showAlertDialog("Error", databaseError.getMessage());
            }
        });
    }

    //-----------------------------------------------------------------------------------

    private void getSite() {
        progressDialog.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                mSite = dataSnapshot.child("site").getValue(String.class);
                getAvailabilityStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();

                Toast.makeText(SupervisorMain.this, "Error occured", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onCancelled: error: " + databaseError.getMessage());
            }
        });
    }

    private void getAvailabilityStatus() {
        progressDialog.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors")
                .child(mSite).child(mUid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                boolean avail = dataSnapshot.child("availability").getValue(Boolean.class);

                availability = avail;
                if (availability) {
                    buttonAvailability.setText("Available");
                    buttonAvailability.setBackgroundColor(Color.GREEN);
                } else {
                    buttonAvailability.setText("Not Available");
                    buttonAvailability.setBackgroundColor(Color.RED);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();

                Log.i(TAG, "onCancelled: getAvailability: " + databaseError.getMessage());
            }
        });
    }

    private void changeAvailabilityStatus() {
        progressDialog.show();

        availability = !availability;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors")
                .child(mSite).child(mUid);

        reference.child("availability").setValue(availability).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(SupervisorMain.this, "Updated", Toast.LENGTH_SHORT).show();
                    if (availability) {
                        buttonAvailability.setText("Available");
                        buttonAvailability.setBackgroundColor(Color.GREEN);
                    } else {
                        buttonAvailability.setText("Not Available");
                        buttonAvailability.setBackgroundColor(Color.RED);
                    }
                } else {
                    Toast.makeText(SupervisorMain.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Application ?");
        builder.setMessage("Click yes to exit!")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mainScreenMenuLogout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}