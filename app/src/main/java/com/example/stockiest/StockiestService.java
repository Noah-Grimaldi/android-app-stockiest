package com.example.stockiest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class StockiestService extends Service {
    private List<String> tickerBeats = new ArrayList<>();
    public static final String CHANNEL_ID = "stockiest_service_channel";

    public List<String> getTickerBeats() {
        return tickerBeats;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String NOTIFICATION_CHANNEL_ID = "stockiest_service_channel";
        String channelName = "My Background Service";
        NotificationChannel chan;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Stockiest Service")
                .setContentText("Stockiest service is running in the background.")
                .setSmallIcon(R.drawable.stockiesticon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        new WebScrapeTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Return START_STICKY here to restart the service if the system kills it
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class WebScrapeTask extends AsyncTask<Void, Void, Void> {
        private final Executor executor;
        private final Context context;

        public WebScrapeTask(Context context) {
            this.executor = Executors.newSingleThreadExecutor();
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            executor.execute(() -> {
                try {
                    Intent intent = new Intent("new-textview-event");
                    intent.putExtra("message", "Let's see if it works.");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
            });

            return null;
        }
    }
}
