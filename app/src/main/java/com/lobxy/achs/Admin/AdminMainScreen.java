package com.lobxy.achs.Admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.lobxy.achs.Login;
import com.lobxy.achs.R;


public class AdminMainScreen extends AppCompatActivity {

    private static final String TAG = "AdminMainScreen";
    Button buttonSite1, buttonSite2, buttonSite3, buttonSite4;
    public static String site, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main_screen);

        buttonSite1 = findViewById(R.id.site1);
        buttonSite2 = findViewById(R.id.site2);
        buttonSite4 = findViewById(R.id.site4);
        buttonSite3 = findViewById(R.id.site3);
    }

    public void Site1(View view) {
        site = buttonSite1.getText().toString();
        getData();
    }

    public void Site2(View view) {
        site = buttonSite2.getText().toString();
        getData();
    }

    public void Site3(View view) {
        site = buttonSite3.getText().toString();
        getData();
    }

    public void Site4(View view) {
        site = buttonSite4.getText().toString();
        getData();
    }

    private void getData() {
        final String[] complaintType = getResources().getStringArray(R.array.types);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Complain Type");
        builder.setSingleChoiceItems(R.array.types, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                type = complaintType[which];
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                type = "Complaints_" + type;
                Log.i(TAG, "onClick: Type" + type);
                Log.i(TAG, "onClick: Site" + site);
                dialog.cancel();
                startActivity(new Intent(AdminMainScreen.this, FragmentsActivity.class));
            }
        });
        builder.create();
        builder.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainscreen_supervisor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addSupervisor) {
            //pass an intent to complaint Activity
            startActivity(new Intent(this, ListSupervisorActivity.class));
        }
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
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
}
