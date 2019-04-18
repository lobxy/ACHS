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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.achs.Model.Rating;
import com.lobxy.achs.R;
import com.lobxy.achs.Utils.Connection;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "Feedback";
    private String mComplaintId, mUserId, mFeedback, mSupervisorId, mSupervisorName, mHappyCode, mSite;
    private long mRating;

    private FirebaseAuth mAuth;
    private DatabaseReference mRatingReference;

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

        mUserId = mAuth.getCurrentUser().getUid();
        mRatingReference = FirebaseDatabase.getInstance().getReference("Ratings").child(mUserId);

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
        getSite();
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

    private void getSite() {
        dialog.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(mSupervisorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();
                mSite = dataSnapshot.child("site").getValue(String.class);
                Log.i(TAG, "onDataChange: site: " + mSite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: error: " + databaseError.getMessage());
            }
        });
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
            if (connection.check()) giveRating();
            else Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
        }
    }

    //------------------------------------------------------------------------------------------------------------

    private void giveRating() {
        dialog.show();
        Rating rating = new Rating(mFeedback, mRating);

        mRatingReference.child(mComplaintId).setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(FeedbackActivity.this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                    reEvaluateRating();
                } else {
                    Toast.makeText(FeedbackActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onComplete: error: " + task.getException().getMessage());
                }
            }
        });

    }

    private void reEvaluateRating() {
        //get all ratings of the visor.
        //add them and take avg by no. of ratings.
        //update it into supervisor's avg rating.

        //Get Ratings.
        dialog.show();

        Query query = mRatingReference.orderByChild("rating");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dialog.dismiss();

                long noOfRatings = dataSnapshot.getChildrenCount();
                Log.i(TAG, "onDataChange: noOfRatings:" + noOfRatings);

                long value = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Rating rating = snapshot.getValue(Rating.class);
                    value += rating.getRating();
                }

                int newRating = (int) (value / noOfRatings);

                setSupervisorRating(newRating);

                Log.i(TAG, "onDataChange: avg :" + value);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                Log.i(TAG, "evaluateRating error :" + databaseError.getMessage());
            }
        });

    }

    private void setSupervisorRating(int newRating) {
        dialog.show();
        DatabaseReference mFeedbackReference = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors");
        mFeedbackReference.child(mSite).child(mSupervisorId).child("rating").setValue(newRating).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(FeedbackActivity.this, "Rating changed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FeedbackActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onComplete: error: " + task.getException().getMessage());
                }
            }
        });
    }

    //EOC
}
