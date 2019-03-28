package com.lobxy.achs.User.Activities;

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
import com.lobxy.achs.Model.UserComplains;
import com.lobxy.achs.R;

import java.util.ArrayList;
import java.util.List;

public class UserComplainActivity extends AppCompatActivity {


    ListView listView;
    DatabaseReference databaseReference;
    List<UserComplains> complaintList;
    FirebaseAuth mAuth;
    String uid;

    TextView noComplaintTextView;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_complains);

        dialog = new ProgressDialog(this);
        dialog.setInverseBackgroundForced(false);
        dialog.setCancelable(false);
        dialog.setMessage("Working...");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        noComplaintTextView = findViewById(R.id.userComplaints_textview);

        listView = findViewById(R.id.myComplaintsListView);
        complaintList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("User_complaints").child(uid);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                int item = (int) adapterView.getItemAtPosition(i);

                String supervisorId = complaintList.get(item).getSupervisorId();
                String supervisorName = complaintList.get(item).getSupervisorName();
                String complaintId = complaintList.get(item).getComplaintId();

                Intent intent = new Intent(UserComplainActivity.this, FeedbackActivity.class);
                intent.putExtra("supervisorId", supervisorId);
                intent.putExtra("supervisorName", supervisorName);
                intent.putExtra("complaintId", complaintId);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        setData();
    }

    private void setData() {

        dialog.show();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                complaintList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot reqSnap : dataSnapshot.getChildren()) {
                        UserComplains complains = reqSnap.getValue(UserComplains.class);
                        complaintList.add(complains);
                    }

                    dialog.dismiss();
                    UserComplainAdapter adapter = new UserComplainAdapter(UserComplainActivity.this, complaintList);
                    listView.setAdapter(adapter);
                } else {
                    dialog.dismiss();
                    noComplaintTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserComplainActivity.this, "Error: " + databaseError.getDetails(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

}
