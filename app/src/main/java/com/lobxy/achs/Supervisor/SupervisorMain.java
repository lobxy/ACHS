package com.lobxy.achs.Supervisor;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Adapters.ComplaintAdapter;
import com.lobxy.achs.Login;
import com.lobxy.achs.Model.Complain;
import com.lobxy.achs.R;

import java.util.ArrayList;
import java.util.List;

public class SupervisorMain extends AppCompatActivity {
    private static final String TAG = "SupervisorMain";
    //Supervisor ke request assigned me se request id nikalni he,then usse confirmed wale me search krna he,
    // and after that uski value using bundle send krni he dusri activity me.

    DatabaseReference databaseReference;
    List<Complain> vComplaintList;
    ListView complaint_listView;
    FirebaseAuth mAuth;
    TextView noComplaint;
    ProgressDialog dialog;
    String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_main);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Working");
        dialog.setInverseBackgroundForced(false);
        dialog.setCancelable(false);

        //setting logged in supervisor's name on Action bar.
        complaint_listView = findViewById(R.id.visor_listView);
        noComplaint = findViewById(R.id.noComplaintsSupervisorMain);

        vComplaintList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Supervisors_Complaint_Slot");
        mAuth = FirebaseAuth.getInstance();
        uId = mAuth.getCurrentUser().getUid();

        complaint_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Complain complaints = vComplaintList.get(i);
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
        if (connectivity()) {
            setData();
        } else {
            Toast.makeText(this, "Please make sure you are connected to internet!", Toast.LENGTH_LONG).show();
        }
    }

    private void setData() {
        dialog.show();
        databaseReference.child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();
                vComplaintList.clear();

                Log.i(TAG, "onDataChange: COUNT: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot reqSnap : dataSnapshot.getChildren()) {
                    Complain complaints = reqSnap.getValue(Complain.class);
                    vComplaintList.add(complaints);
                }
                ComplaintAdapter adapter = new ComplaintAdapter(SupervisorMain.this, vComplaintList);
                complaint_listView.setAdapter(adapter);

                if (adapter.isEmpty()) {
                    noComplaint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                Toast.makeText(SupervisorMain.this, "Error:" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
            startActivity(new Intent(this, Login.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}