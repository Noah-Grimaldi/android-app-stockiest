package com.example.stockiest;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.stockiest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID = "stockiest_service_channel";
    public static final String CHANNEL_NAME = "My Background Service";
    private StockQueryService stockQueryService;
    private boolean isServiceBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Service connected
            StockQueryService.StockQueryServiceBinder binder = (StockQueryService.StockQueryServiceBinder) service;
            stockQueryService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Service disconnected
            isServiceBound = false;
            stockQueryService = null;
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind service
        Intent serviceIntent = new Intent(this, StockQueryService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);

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
    public void setupSwitches() {
        Switch newsSwitch = findViewById(R.id.switch1);
        Switch earningsSwitch = findViewById(R.id.switch2);

        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        // Set the initial switch state based on the saved preferences
        boolean isNewsSwitchOn = prefs.getBoolean("newsSwitchOn", false);
        boolean isEarningsSwitchOn = prefs.getBoolean("earningsSwitchOn", false);

        newsSwitch.setChecked(isNewsSwitchOn);

        newsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefsLocal = getSharedPreferences("myPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefsLocal.edit();
            editor.putBoolean("newsSwitchOn", isChecked);
            editor.apply();

            if (isChecked) {
                queryStockSite();
            }
            else {
                stopQuery();
            }
        });

        earningsSwitch.setChecked(isEarningsSwitchOn);
        earningsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefsLocal = getSharedPreferences("myPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefsLocal.edit();
            editor.putBoolean("earningsSwitchOn", isChecked);
            editor.apply();

            if (isChecked) {
                queryStockSite();
            }
            else {
                stopQuery();
            }
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
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            channel.setLightColor(Color.BLUE);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
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
     * Helper to start querying website
     */
    public void queryStockSite() {
        if (isServiceBound && stockQueryService != null && !stockQueryService.getRunning()) {
            stockQueryService.startWebsiteQuery();
        }
    }

    /**
     * Helper to stop querying
     */
    public void stopQuery() {
        if (isServiceBound && stockQueryService != null && stockQueryService.getRunning()) {
            stockQueryService.stopWebsiteQuery();
        }
    }
}


