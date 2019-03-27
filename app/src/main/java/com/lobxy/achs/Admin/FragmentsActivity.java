package com.lobxy.achs.Admin;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.lobxy.achs.Adapters.SectionPagerAdapter;
import com.lobxy.achs.R;

public class FragmentsActivity extends AppCompatActivity {

    SectionPagerAdapter pagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        tabLayout = findViewById(R.id.tabs);
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
