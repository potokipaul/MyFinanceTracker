package com.myfinancetracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();
            if      (id == R.id.nav_dashboard)    f = new DashboardFragment();
            else if (id == R.id.nav_transactions) f = new TransactionsFragment();
            else if (id == R.id.nav_budget)       f = new BudgetFragment();
            else if (id == R.id.nav_yearly)       f = new YearlyFragment();
            else if (id == R.id.nav_settings)     f = new SettingsFragment();
            if (f != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, f).commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }
    }
}
