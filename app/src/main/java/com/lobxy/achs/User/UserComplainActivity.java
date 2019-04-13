package com.lobxy.achs.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.lobxy.achs.Adapters.UserComplainAdapter;
import com.lobxy.achs.Model.UserComplaints;
import com.lobxy.achs.R;
import com.lobxy.achs.Utils.Connection;

import java.util.ArrayList;
import java.util.List;

public class UserComplainActivity extends AppCompatActivity {

    private ListView listView;
    private List<UserComplaints> complaintList;

    private DatabaseReference reference;

    private TextView text_noComplaint;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_complains);

        progressDialog = new ProgressDialog(this);
        progressDialog.setInverseBackgroundForced(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Working...");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String mUid = mAuth.getCurrentUser().getUid();

        text_noComplaint = findViewById(R.id.userComplaints_textview);

        listView = findViewById(R.id.myComplaintsListView);
        complaintList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("User_complaints").child(mUid);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String supervisorId = complaintList.get(i).getSupervisorId();
                String supervisorName = complaintList.get(i).getSupervisorName();
                String complaintId = complaintList.get(i).getComplaintId();
                String happyCode = complaintList.get(i).getHappyCode();

                Intent intent = new Intent(UserComplainActivity.this, FeedbackActivity.class);
                intent.putExtra("supervisorId", supervisorId);
                intent.putExtra("supervisorName", supervisorName);
                intent.putExtra("complaintId", complaintId);
                intent.putExtra("happyCode", happyCode);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Connection connection = new Connection(this);
        if (connection.check()) setData();
        else {
            Toast.makeText(this, "Connect to internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void setData() {

        progressDialog.show();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                complaintList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot reqSnap : dataSnapshot.getChildren()) {
                        UserComplaints complains = reqSnap.getValue(UserComplaints.class);
                        complaintList.add(complains);
                    }

                    progressDialog.dismiss();
                    UserComplainAdapter adapter = new UserComplainAdapter(UserComplainActivity.this, complaintList);
                    listView.setAdapter(adapter);
                } else {
                    progressDialog.dismiss();
                    text_noComplaint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserComplainActivity.this, "Error: " + databaseError.getDetails(), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

}
