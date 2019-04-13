package com.lobxy.achs.User;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lobxy.achs.Adapters.ComplaintTypesListViewAdapter;
import com.lobxy.achs.R;

public class ComplainTypesActivity extends AppCompatActivity {
    private static final String TAG = "Complain Types";
    ListView listView;

    String[] labels = {
            "Electricity", "Plumbing",
            "HouseKeeping", "Carpenter",
            "Civil", "Fire and Security",
            "Lift", "Intercom", "Others"};

    Integer[] images = {R.drawable.elec, R.drawable.plumbing, R.drawable.house, R.drawable.carpenter, R.drawable.civil,
            R.drawable.fire, R.drawable.lift, R.drawable.intercom, R.drawable.elec};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complains);

        listView = findViewById(R.id.complainsList);

        ComplaintTypesListViewAdapter adapter = new ComplaintTypesListViewAdapter(this, labels, images);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String complainType = labels[position];
                Log.d(TAG, "onItemClick: Type: " + complainType);
                Intent intent = new Intent(ComplainTypesActivity.this, ComplainFormActivity.class);
                intent.putExtra("Type", complainType);
                startActivity(intent);
            }
        });

    }
}
