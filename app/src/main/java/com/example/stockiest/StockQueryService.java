package com.example.stockiest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StockQueryService extends Service {
    private Timer timer;
    private static List<String> tickerBeats = new ArrayList<>();
    private boolean isRunning = false;
    public static final String CHANNEL_ID = "stockiest_service_channel";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean getRunning() {
        return isRunning;
    }

    public void sendNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Stockiest Service")
                .setContentText("Stockiest service is running in the background.")
                .setSmallIcon(R.drawable.stockiesticon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    public void startWebsiteQuery() {
        isRunning = true;
        timer = new Timer();

        sendNotification();
        System.out.println("sent notif");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("query made");
                    Document doc = Jsoup.connect("https://www.earningswhispers.com/calendar").get();
                    String tickers = doc.getElementsByClass("ticker").text();
                    List<String> tickerList = new ArrayList<>(Arrays.asList(tickers.split(" ")));

                    for (String i : tickerList) {
                        String tickerClass = String.format("T-%s", i);
                        String tickerID = String.valueOf(doc.getElementById(tickerClass));
                        if (tickerID.contains("class=\"actual green") && tickerID.contains("class=\"revactual green")) {
                            tickerBeats.add(i);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 3000); // Delay of 0 milliseconds and repeat every 5 seconds
    }

    public void stopWebsiteQuery() {
        isRunning = false;
        timer.cancel();
        stopForeground(true);
    }

    public static List<String> getTickerBeats() {
        return tickerBeats;
    }

    public class StockQueryServiceBinder extends Binder {
        public StockQueryService getService() {
            return StockQueryService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new StockQueryServiceBinder();
    }
}
