package com.lobxy.achs.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lobxy.achs.Model.Feedback;
import com.lobxy.achs.R;
import com.lobxy.achs.Utils.Connection;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "Feedback";
    private String mComplaintId, mUserId, mFeedback, mSupervisorId, mSupervisorName, mHappyCode;
    private long mRating;

    private FirebaseAuth mAuth;
    private DatabaseReference mFeedbackReference;

    private EditText edit_feedback;

    private TextView text_happyCode;

    private RatingBar ratingBar;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        dialog = new ProgressDialog(this);
        dialog.setInverseBackgroundForced(false);
        dialog.setCancelable(false);
        dialog.setMessage("Working...");

        mAuth = FirebaseAuth.getInstance();
        mFeedbackReference = FirebaseDatabase.getInstance().getReference("Feedback");
        mUserId = mAuth.getCurrentUser().getUid();

        text_happyCode = findViewById(R.id.feedback_happyCode);

        edit_feedback = findViewById(R.id.feedback_feedback);
        ratingBar = findViewById(R.id.feedback_ratingBar);

        Button submit = findViewById(R.id.feedback_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: clicked");
                validation();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            mSupervisorName = intent.getStringExtra("supervisorName");
            mSupervisorId = intent.getStringExtra("supervisorId");
            mComplaintId = intent.getStringExtra("complaintId");
            mHappyCode = intent.getStringExtra("happyCode");

            text_happyCode.setText(mHappyCode);
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            Log.i("Feedback", "getData: no data in intent found");
        }
    }

    private void validation() {
        //get rating from rating bar.
        mFeedback = edit_feedback.getText().toString().trim();
        mRating = (long) ratingBar.getRating();

        Log.i(TAG, "validation: bar : " + mRating);

        if (mFeedback.isEmpty()) {
            mFeedback = "Not Provided";
        }

        if (mRating == 0) {
            Toast.makeText(this, "Rating not given", Toast.LENGTH_SHORT).show();
        } else {
            Connection connection = new Connection(this);
            if (connection.check()) saveFeedback();
            else Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFeedback() {
        //get supervisor id,complaint id.
        dialog.show();
        Feedback feedback = new Feedback(mUserId, mComplaintId, mFeedback, mRating, mSupervisorId, mSupervisorName);

        mFeedbackReference.child(mSupervisorId).setValue(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(FeedbackActivity.this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(FeedbackActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onComplete: error: " + task.getException().getMessage());
                }
            }
        });

    }

    //EOC
}
