package com.lobxy.achs.Admin.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Adapters.ComplaintHolder;
import com.lobxy.achs.Admin.AdminMainScreen;
import com.lobxy.achs.Model.Complain;
import com.lobxy.achs.R;

import static android.content.ContentValues.TAG;

public class Electricity extends Fragment {

    public Electricity() {
        // Required empty public constructor
    }

    View view;
    TextView noComplaints;

    DatabaseReference ref;
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Complain, ComplaintHolder> adapter;
    ProgressDialog dialog;

    String site = AdminMainScreen.site;
    String complaintType = AdminMainScreen.type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_electricity, container, false);
        noComplaints = view.findViewById(R.id.elec_text);

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Working...");
        dialog.setInverseBackgroundForced(false);
        dialog.setCancelable(false);

        recyclerView = view.findViewById(R.id.re_electricity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Log.i(TAG, "onCreateView: Site Value : " + AdminMainScreen.site);
        Log.i(TAG, "onCreateView: ComplaintType Value : " + AdminMainScreen.type);

        populateData();

        return view;
    }

    public void populateData() {
        dialog.show();
        ref = FirebaseDatabase.getInstance().getReference().child(complaintType).child(site).child("Electricity");

        Query query = ref.orderByChild("complaintInitTime");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();

                if (!dataSnapshot.exists()) {
                    noComplaints.setVisibility(View.VISIBLE);
                } else {
                    Log.i(TAG, "onDataChange: Data Exists");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();

                Log.i(TAG, "onCancelled: Database Error to check complaints" + databaseError.getMessage());
            }
        });
        FirebaseRecyclerOptions<Complain> options = new FirebaseRecyclerOptions.Builder<Complain>()
                .setQuery(query, Complain.class).build();
        adapter = new FirebaseRecyclerAdapter<Complain, ComplaintHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ComplaintHolder holder, final int position, @NonNull final Complain model) {
                holder.setStatus(model.getCompletionStatus());
                holder.setInit(model.getComplaintInitTime());
                holder.setId(model.getComplaintID());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {/*
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Complaint_UID", model.getUserId());
                        bundle.putString("Complaint_CONTACT", model.getContact());
                        bundle.putString("Complaint_ID", model.getComplaintID());
                        bundle.putString("Complaint_EMAIL", model.getName());
                        bundle.putString("Complaint_NAME", model.getName());
                        bundle.putString("Complaint_DES", model.getDescription());
                        bundle.putString("Complaint_STATUS", model.getCompletionStatus());
                        bundle.putString("Complaint_SITE", model.getSite());
                        bundle.putString("Complaint_INIT", model.getComplaintInitTime());
                        bundle.putString("Complaint_HAPPY_CODE", model.getHappyCode());
                        bundle.putString("Complaint_VISIT_TIME", model.getVisitTime());
                        bundle.putString("Complaint_ADDRESS", model.getAddress());
                        bundle.putString("Complaint_TYPE", model.getType());
                        bundle.putString("Complaint_SUPERVISOR_ASSIGNED",model.getSupervisorAssigned());
                        bundle.putBoolean("Complaint_COMMON_AREA", model.getCommonArea());
                        intent.putExtras(bundle);
                        startActivity(intent);*/
                    }
                });
            }

            @NonNull
            @Override
            public ComplaintHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.complaint_listitem, parent, false);
                return new ComplaintHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
