package com.example.stockiest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
    private Switch newsSwitch;
    private Switch earningsSwitch;
    private SharedPreferences prefs;
    public static final String CHANNEL_ID = "stockiest_service_channel";
    public static final String CHANNEL_NAME = "My Background Service";


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch the notification permission request if it hasn't been granted
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }

        // no clue what you had going on
        configCrap();

        // creates notification channel
        createNotificationChannel();

        // create switches and their event handlers
        setupSwitches();
    }

    /**
     * Initiates switches
     */
    private void setupSwitches() {
        newsSwitch = findViewById(R.id.switch1);
        earningsSwitch = findViewById(R.id.switch2);
        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        // Set the initial switch state based on the saved preferences
        boolean isNewsSwitchOn = prefs.getBoolean("newsSwitchOn", false);
        boolean isEarningsSwitchOn = prefs.getBoolean("earningsSwitchOn", false);

        newsSwitch.setChecked(isNewsSwitchOn);
        newsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the new switch state to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("newsSwitchOn", isChecked);
            editor.apply();

            Intent serviceIntent = new Intent(MainActivity.this, StockiestService.class);
            serviceHelper(isChecked, serviceIntent);
        });

        earningsSwitch.setChecked(isEarningsSwitchOn);
        earningsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the new switch state to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("earningsSwitchOn", isChecked);
            editor.apply();

            Intent serviceIntent = new Intent(MainActivity.this, StockiestService.class);
            serviceHelper(isChecked, serviceIntent);
        });
    }

    /**
     * Sets up items (?)
     */
    private void configCrap() {
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
    }

    /**
     *  Creates a global notification channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Stockiest notification channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Callback to handle result of user accepting or denying
     * notification permissions
     *
     * @param requestCode The request code passed in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(MainActivity.this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(MainActivity.this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method to reduce repetitive code
     *
     * @param isChecked Boolean representing the state of the given switch
     * @param serviceIntent Intent Object meant to be started or stopped
     */
    private void serviceHelper(boolean isChecked, Intent serviceIntent) {
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
}


