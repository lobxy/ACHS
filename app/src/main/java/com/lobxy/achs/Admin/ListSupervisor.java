package com.lobxy.achs.Admin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Adapters.SelectionAdapter;
import com.lobxy.achs.Model.Supervisor;
import com.lobxy.achs.R;

import java.util.ArrayList;
import java.util.List;

public class ListSupervisor extends AppCompatActivity {

    private static final String TAG = "SupervisorList";
    ListView listView;
    TextView noVisorsTextView, removeVisorText;
    FloatingActionButton fab;

    DatabaseReference ref;
    FirebaseAuth auth;
    ProgressDialog dialog;
    List<Supervisor> list;
    String site;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisors_list);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Working...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        list = new ArrayList<>();
        listView = findViewById(R.id.visor_main_listview);

        noVisorsTextView = findViewById(R.id.visor_main_textView);
        removeVisorText = findViewById(R.id.removeVisorText);

        fab = findViewById(R.id.visor_main_fab);
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors");

        showSites();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show the activity to add a new supervisor.

                Intent intent = new Intent(ListSupervisor.this, AddSupervisor.class);
                intent.putExtra("SiteChosen", site);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                //Show Alert Dialog to delete the supervisor.
                AlertDialog.Builder builder = new AlertDialog.Builder(ListSupervisor.this);
                builder.setTitle("ALERT!!");
                builder.setMessage("All unresolved complaints assigned will get deleted. Press Confirm to to delete the supervisor.")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Delete supervisor.
                                //get the supervisor's uid and delete the data.
                                Supervisor supervisor = list.get(position);
                                String uid = supervisor.getUid();
                                String email = supervisor.getEmail();
                                String password = supervisor.getPassword();

                                removeSupervisor(uid, email, password);

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
                return false;
            }
        });
    }

    private void showSites() {
        final String[] sites_name = getResources().getStringArray(R.array.sites_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Site");
        builder.setSingleChoiceItems(R.array.sites_name, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                site = sites_name[which];
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ref = ref.child(site);
                dialog.cancel();
                setData();
            }
        });
        builder.create();
        builder.show();
    }

    private void setData() {
        dialog.show();

        Query query = ref.orderByChild("count").limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Supervisor visor = snapshot.getValue(Supervisor.class);
                        Log.i(TAG, "onDataChange: supervisor id: " + visor.getUid());

                        list.add(visor);
                    }
                    SelectionAdapter adapter = new SelectionAdapter(list, ListSupervisor.this);
                    listView.setAdapter(adapter);
                } else {
                    noVisorsTextView.setVisibility(View.VISIBLE);
                    removeVisorText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                Toast.makeText(ListSupervisor.this, "Listing Supervisor Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeSupervisor(final String uid, final String email, final String password) {
        //remove supervisor data from User_data node, Users node,Supervisors_Complaint_Slot.

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("User_Data/Supervisors").child(site).child(uid).
                removeValue().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ref.child("Users").child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        ref.child("Supervisors_Complaint_Slot").child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
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
                                                                                AlertDialog.Builder builder = new AlertDialog.Builder(ListSupervisor.this);
                                                                                builder.setTitle("Alert");
                                                                                builder.setMessage("Supervisor Deletion Successful.")
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
                                                                                Toast.makeText(ListSupervisor.this, "onComplete: user removal error: " + task.getException(), Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                } else {
                                                    Log.i(TAG, "onComplete:Supervisors_Complaint_Slot data deletion error " + task.getException().getMessage());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.i(TAG, "onComplete:Users deletion error " + task.getException().getMessage());
                                    }
                                }
                            });

                        } else {
                            Log.i(TAG, "onComplete: Deletion Failed error: " + task.getException().getMessage());
                            Toast.makeText(ListSupervisor.this, "Deletion Failed: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }
}
