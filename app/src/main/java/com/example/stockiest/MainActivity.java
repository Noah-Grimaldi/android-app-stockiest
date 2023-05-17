package com.example.stockiest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.stockiest.databinding.ActivityMainBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> pushNotificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(MainActivity.this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
                }
            });

    private Switch newsSwitch;
    private Switch earningsSwitch;
    private SharedPreferences prefs;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.stockiest.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Create the notification channel for devices running Android 8.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Request permission to show notifications
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);

        // Launch the notification permission request if it hasn't been granted
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pushNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        newsSwitch = findViewById(R.id.switch1);
        earningsSwitch = findViewById(R.id.switch2);
        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        // Set the initial switch state based on the saved preferences
        boolean isNewsSwitchOn = prefs.getBoolean("newsSwitchOn", false);
        boolean isEarningsSwitchOn = prefs.getBoolean("earningsSwitchOn", false);
        newsSwitch.setChecked(isNewsSwitchOn);
        newsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the new switch state to SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("newsSwitchOn", isChecked);
                editor.apply();

                if (isChecked) {
                    System.out.println("it's checked! Check to see if this printed!");
                    Intent serviceIntent = new Intent(MainActivity.this, StockiestService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent);
                    } else {
                        startService(serviceIntent);
                    }
                } else {
                    Intent serviceIntent = new Intent(MainActivity.this, StockiestService.class);
                    stopService(serviceIntent);
                }
            }
        });
        earningsSwitch.setChecked(isEarningsSwitchOn);
        earningsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the new switch state to SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("earningsSwitchOn", isChecked);
                editor.apply();

                if (isChecked) {
                    Intent serviceIntent = new Intent(MainActivity.this, StockiestService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent);
                    } else {
                        startService(serviceIntent);
                    }
                } else {
                    Intent serviceIntent = new Intent(MainActivity.this, StockiestService.class);
                    stopService(serviceIntent);
                }
            }
        });
    }


    CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // Save the new switch state to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            if (buttonView == newsSwitch) {
                editor.putBoolean("newsSwitchOn", isChecked);
            } else if (buttonView == earningsSwitch) {
                editor.putBoolean("earningsSwitchOn", isChecked);
            }
            editor.apply();

            // Start or stop the service based on the switch state
            Intent serviceIntent = new Intent(MainActivity.this, StockiestService.class);
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            } else {
                stopService(serviceIntent);
            }
        }
    };
}


