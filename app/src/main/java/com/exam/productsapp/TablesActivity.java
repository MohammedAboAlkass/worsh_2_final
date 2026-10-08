package com.exam.productsapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

public class TablesActivity extends AppCompatActivity {

    private ViewPager2 viewPagerTables;
    private TextView tvPageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables);

        Toolbar toolbar = findViewById(R.id.tablesToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        viewPagerTables = findViewById(R.id.viewPagerTables);
        tvPageIndicator = findViewById(R.id.tvPageIndicator);

        TablesPagerAdapter adapter = new TablesPagerAdapter();
        viewPagerTables.setAdapter(adapter);

        // Initial indicator update
        tvPageIndicator.setText(getString(R.string.page_indicator, 1, 500));

        // Update indicator on page change
        viewPagerTables.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvPageIndicator.setText(getString(R.string.page_indicator, (position + 1), 500));
            }
        });
    }
}
