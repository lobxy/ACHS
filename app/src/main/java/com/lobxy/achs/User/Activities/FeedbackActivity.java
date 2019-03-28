package com.lobxy.achs.User.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lobxy.achs.Model.Feedback;
import com.lobxy.achs.R;

public class FeedbackActivity extends AppCompatActivity {

    private String mComplaintId, mUserId, mFeedback, mRating, mSupervisorId, mSupervisorName;

    private FirebaseAuth mAuth;
    private DatabaseReference mFeedbackReference;

    private EditText edit_feedback;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mAuth = FirebaseAuth.getInstance();
        mFeedbackReference = FirebaseDatabase.getInstance().getReference("Feedback");
        mUserId = mAuth.getCurrentUser().getUid();

        edit_feedback = findViewById(R.id.feedback_feedback);
        ratingBar = findViewById(R.id.feedback_ratingBar);

        Button submit = findViewById(R.id.feedback_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validation();
            }
        });

        getData();

    }

    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            mSupervisorName = intent.getStringExtra("supervisorName");
            mSupervisorId = intent.getStringExtra("supervisorId");
            mComplaintId = intent.getStringExtra("complaintId");
        } else {
            Log.i("Feedback", "getData: no data in intent found");
        }
    }

    private void validation() {
        //get rating from rating bar.
        mFeedback = edit_feedback.getText().toString().trim();
        mRating = String.valueOf(ratingBar.getRating());

        if (mFeedback.isEmpty()) mFeedback = "Not Provided";
        else if (mRating.isEmpty()) {
            Toast.makeText(this, "Rating not given", Toast.LENGTH_SHORT).show();
        } else saveFeedback();

    }

    private void saveFeedback() {
        //get supervisor id,complaint id.
        Feedback feedback = new Feedback(mUserId, mComplaintId, mFeedback, mRating, mSupervisorId, mSupervisorName);

        mFeedbackReference.child(mSupervisorId).setValue(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(FeedbackActivity.this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(FeedbackActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                    Log.i("Feedback", "onComplete: error: " + task.getException().getMessage());
                }
            }
        });

    }

    //EOC
}
