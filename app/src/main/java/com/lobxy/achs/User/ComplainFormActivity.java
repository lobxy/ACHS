package com.lobxy.achs.User;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lobxy.achs.Model.Complain;
import com.lobxy.achs.Model.Supervisor;
import com.lobxy.achs.Model.UserComplaints;
import com.lobxy.achs.R;
import com.lobxy.achs.Utils.Connection;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ComplainFormActivity extends AppCompatActivity {

    private static final String TAG = "Complain Form";

    private EditText edit_description;

    private String mName, mComplaintId, mAddress, mSite, mContact, mEmail, mVisitTime,
            mComplaintInitTime, mDescription, mType, mDate, mTime, mUid, mSupervisorId,
            mSupervisorName, mHappyCode, mDownloadUrl = "";

    private long mSelectedSupervisorCount = 0;

    private FirebaseAuth mAuth;
    private DatabaseReference complaintReference;     //For Complaints
    private DatabaseReference userComplaintsDatabaseReference;     //For User's complaints section.

    private ProgressDialog dialog;

    private Button button_pickImage;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain_form);
        imageView = findViewById(R.id.form_image);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Working...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(true);

        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getCurrentUser().getUid();

        mType = getIntent().getStringExtra("Type");
        complaintReference = FirebaseDatabase.getInstance().getReference("Complaints_Unresolved");
        userComplaintsDatabaseReference = FirebaseDatabase.getInstance().getReference("User_complaints").child(mUid);

        edit_description = findViewById(R.id.form_edit_desciption);

        button_pickImage = findViewById(R.id.form_button_show_time);

        //button for attaching image.
        Button attachImage = findViewById(R.id.form_button_pickImage);

        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepare();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(ComplainFormActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(ComplainFormActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(ComplainFormActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else bringImagePicker();

                } else bringImagePicker();
            }
        });

        Button btn_submit = findViewById(R.id.form_button_submit);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Process complaint
                prepare();
                validation();
            }
        });

        button_pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDate = "";
                mTime = "";

                button_pickImage.setText("Pick");

                //hide the keyboard
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                datePicker();
            }
        });

        Button clearImage = findViewById(R.id.form_clearImage);
        clearImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mDownloadUrl.isEmpty()) {
                    mDownloadUrl = "";
                } else {
                    Toast.makeText(ComplainFormActivity.this, "Image not selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //HANDLE IMAGE
    private void prepare() {
        mComplaintId = complaintReference.push().getKey();
        mHappyCode = createHappyCode();
        complaintReference = complaintReference.child(mSite).child(mType);
    }

    private void bringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setMinCropResultSize(500, 500)
                .setMaxCropResultSize(800, 800)
                .start(this);
    }

    private void uploadImage(Uri uri) {
        dialog.show();
        Log.i(TAG, "uploadImage: called uri");

        final StorageReference ref = FirebaseStorage.getInstance().getReference("Complaints/");

        //create folder named of uid/email and then save image on the name of complaint id. save url into complaint node.
        ref.child(mComplaintId).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                dialog.dismiss();

                if (task.isSuccessful()) {
                    getDownloadUrl(ref);
                } else {
                    dialog.dismiss();
                    Log.i(TAG, "onComplete: shit happened twice");
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, String.format("onProgress: %5.2f MB transferred",
                        taskSnapshot.getBytesTransferred() / 1024.0 / 1024.0));
            }
        });

    }

    private void getDownloadUrl(StorageReference ref) {
        //get download url.
        dialog.show();
        ref.child(mComplaintId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                dialog.dismiss();
                mDownloadUrl = uri.toString();
                Log.d(TAG, "Image Uploaded " + mDownloadUrl);
                Toast.makeText(ComplainFormActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                //validation();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();

                Log.i(TAG, "Upload image error: " + e.getMessage());
                Toast.makeText(ComplainFormActivity.this, "Error occurred! Try Again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //HANDLE DATA
    private void datePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                mDate = (day + "-" + (month + 1) + "-" + year);
                Log.i(TAG, "onTimeSet: mDate:" + mDate);

                timePicker();
            }
        };

        DatePickerDialog dateDialog = new DatePickerDialog(this, dateSetListener, mYear, mMonth, mDay);
        dateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                button_pickImage.setText("Pick");
            }
        });
        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() + 43200 * 1000);

        dateDialog.show();
    }

    private void timePicker() {
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                if (hourOfDay > 12) {
                    hourOfDay -= 12;
                    mTime = (hourOfDay + ":" + minute + ":" + "PM");
                } else {
                    mTime = (hourOfDay + ":" + minute + ":" + "AM");
                }

                Log.i(TAG, "onTimeSet: mDate:" + mTime);

                mVisitTime = mDate + " @ " + mTime;

                Log.i(TAG, "onTimeSet: to be set" + mVisitTime);

                button_pickImage.setText(mVisitTime);
            }
        };

        TimePickerDialog dialog = new TimePickerDialog(this, timeSetListener, mHour, mMinute, false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //  mDate = "";
            }
        });

        dialog.show();

    }

    public void validation() {
        mDescription = edit_description.getText().toString().trim();

        if (mDescription.isEmpty()) {
            edit_description.setError("Description is empty ");
            edit_description.requestFocus();
            return;
        }
        if (button_pickImage.getText().toString().equals("Pick") || mVisitTime.isEmpty()) {
            Toast.makeText(this, "Please pick a visit time.", Toast.LENGTH_SHORT).show();
            return;
        }
        Connection connection = new Connection(this);

        //Upload complaint form data to the database
        if (connection.check()) {

            Log.i(TAG, "validation: validated");
            if (mDownloadUrl.isEmpty()) mDownloadUrl = "Not Provided";

            getSupervisor();
        }

        else showAlert("Alert", "Please connect to the internet");

    }

    private void getSupervisor() {
        dialog.show();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors").child(mSite);

        //get the supervisor with lowest complain mSelectedSupervisorCount at the moment.
        Query query = reference.orderByChild("count").limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Supervisor visor = snapshot.getValue(Supervisor.class);
                        mSelectedSupervisorCount = visor.getCount();
                        mSupervisorId = visor.getUid();
                        mSupervisorName = visor.getName();
                    }

                    Log.i(TAG, "onDataChange: name: " + mSupervisorName);
                    Log.i(TAG, "onDataChange:Got Supervisor");

                    uploadComplaintData();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();

                Log.i(TAG, "onCancelled: Query error: " + databaseError.getMessage());
                showAlert("Error", databaseError.getMessage());
            }
        });

    }

    public void uploadComplaintData() {

        //Get current mTime and mDate.
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
        mComplaintInitTime = currentDate + " @ " + currentTime;

        dialog.show();

        //create a new object of model class, feed the data to it.
        final Complain complain = new Complain(mDownloadUrl, mUid, mComplaintId, mName, mEmail, mAddress, mContact, mSite, mType, mDescription, mVisitTime, mComplaintInitTime,
                mHappyCode, "Unresolved", mSupervisorName, "No Data");

        //TODO: complaintId causes the app to crash as its null.
        //java.lang.NullPointerException: Can't pass null for argument 'pathString' in child()
        //        at com.google.firebase.database.DatabaseReference.child(com.google.firebase:firebase-database@@16.1.0:101)
        //        at com.lobxy.achs.User.ComplainFormActivity.uploadComplaintData(ComplainFormActivity.java:377)
        //        at com.lobxy.achs.User.ComplainFormActivity$13.onDataChange(ComplainFormActivity.java:346)

        //Upload data to the firebase
        complaintReference.child(mComplaintId).setValue(complain).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "onSuccess: Complaint added in UNRESOLVED");

                saveUserComplaintData(complain);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                //no rollback needed.
                Log.i(TAG, "onFailure: Form data submission error " + e.getMessage());
                showAlert("Error", e.getMessage());
            }
        });

    }

    private void saveUserComplaintData(final Complain complain) {

        //feed data on mUser complains model class.
        UserComplaints userComplaints = new UserComplaints(mType, complain.getComplaintID(), complain.getHappyCode(), mComplaintInitTime,
                "Unresolved", "No Data", mSupervisorId, mSupervisorName);

        Log.i(TAG, "saveUserComplaintData: name" + userComplaints.getSupervisorName());

        //Save data to mUser complaints database node, for mUser's MY COMPLAINTS section.
        userComplaintsDatabaseReference.child(complain.getComplaintID()).setValue(userComplaints).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    //assign supervisor;
                    updateCount(complain);

                    Log.i(TAG, "onSuccess: User's complain data saving Success");
                } else {
                    //remove mUser complains from the UNRESOLVED complain pool.
                    // complaintReference.child(id).removeValue();
                    showAlert("Error", task.getException().getMessage());
                }
            }
        });
    }

    private void updateCount(final Complain complain) {
        dialog.show();

        final DatabaseReference supervisorComplainRef = FirebaseDatabase.getInstance().getReference("Supervisors_Complaint_Slot");
        final DatabaseReference supervisorRef = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors").child(mSite);

        //update value
        supervisorRef.child(mSupervisorId).child("count").setValue(++mSelectedSupervisorCount)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "onComplete: Updated count");
                            assignComplaintToSupervisor(supervisorComplainRef, mSupervisorId, complain);

                        } else {
                            Log.i(TAG, "onComplete: couldn't update mSelectedSupervisorCount");
                        }

            }
        });

    }

    private void assignComplaintToSupervisor(DatabaseReference supervisorComplainRef,
                                             final String supervisorId, Complain complain) {

        supervisorComplainRef.child(supervisorId).child(complain.getComplaintID()).setValue(complain)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();

                        if (task.isSuccessful()) {
                            //Show HAPPY CODE to the user!

                            Log.i(TAG, "onComplete: Supervisor Assigned");

                            AlertDialog.Builder builder = new AlertDialog.Builder(ComplainFormActivity.this);
                            builder.setTitle(mHappyCode);
                            builder.setMessage("Please take a note of this HAPPY CODE or you can find it in MY COMPLAINTS.")
                                    .setCancelable(false)
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            dialogInterface.dismiss();
                                            startActivity(new Intent(ComplainFormActivity.this, UserMainScreenActivity.class));
                                            finish();

                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                        } else {
                            //complain not set to supervisor, reset the mSelectedSupervisorCount variable.
                            resetCount(supervisorId);
                            Log.i(TAG, "onComplete: Supervisor not assigned: Error: " + task.getException().getLocalizedMessage());
                            showAlert("Error", task.getException().getMessage());
                        }
                    }
                });
    }

    private void resetCount(String supervisorId) {
        final DatabaseReference resetReference = FirebaseDatabase.getInstance().getReference("User_Data/Supervisors").child(mSite).child(supervisorId);
        resetReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = 0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Supervisor visor = snapshot.getValue(Supervisor.class);
                        count = visor.getCount();
                    }
                    count--;

                    resetReference.child("mSelectedSupervisorCount").setValue(count).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "resetCount: mSelectedSupervisorCount updated ");
                            } else {
                                Log.i(TAG, "resetCount:mSelectedSupervisorCount not update ");
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showAlert("Error", databaseError.getMessage());
            }
        });
        showAlert("Error", "Couldn't process complain.\nTry again later");
    }

    private void getUserData() {
        Log.i(TAG, "getUserData: called");
        //User database reference
        //dialog.show();

        DatabaseReference userDataReference = FirebaseDatabase.getInstance().getReference().child("User_Data").child("User").child(mUid);
        userDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //    dialog.dismiss();

                mName = dataSnapshot.child("name").getValue(String.class);
                mAddress = dataSnapshot.child("address").getValue(String.class);
                mSite = dataSnapshot.child("site").getValue(String.class);
                mContact = dataSnapshot.child("contact").getValue(String.class);
                mEmail = dataSnapshot.child("email").getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //   dialog.dismiss();
                Log.i(TAG, "onCancelled: Upload Data error " + databaseError.getMessage());
                showAlert("Error", databaseError.getMessage());
            }
        });
    }

    private String createHappyCode() {
        //generate a 6 digit integer 1000 <10000
        int randomPIN = (int) (Math.random() * 90000) + 10000;
        return (String.valueOf(randomPIN));
    }

    public void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri mainImageURI = result.getUri();
                uploadImage(mainImageURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Log.i("Main", "onActivityResult: error: " + error);

            }
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

}//EOC