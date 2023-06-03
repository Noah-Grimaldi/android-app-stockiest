package com.example.stockiest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StockQueryService extends Service {
    private Timer queryTimer;
    private Timer newsTimer;
    private static List<String> tickerBeats = new ArrayList<>();
    private static List<String> headlines = new ArrayList<>();
    private boolean isQueryRunning = false;
    private boolean isNewsRunning = false;
    private final String[] keywordSetup = {"phase clinical trial", "merge", " ipo ", "acquisition","nasdaq", "cancer", "cells", "partnership", "equity financing"," deal ","fda approval"," trial", "eps exceeded","contract award", "heart monitor", "pardon", "collaboration", "receives", "acquire", "funding recipients", "agreement", "alliance", "layoff"};
    List<String> seen = new ArrayList<>();
    public static final String CHANNEL_ID = "stockiest_service_channel";
    private final String urlString = "https://static.newsfilter.io/landing-page/main-content-2.json";

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

    public boolean getQueryRunning() {
        return isQueryRunning;
    }

    public boolean getNewsRunning() {
        return isNewsRunning;
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
        isQueryRunning = true;
        queryTimer = new Timer();

        sendNotification();
        System.out.println("sent notif");
        queryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // delete this println when no longer needed
                    System.out.println("query made");

                    // fetch html page
                    Document doc = Jsoup.connect("https://www.earningswhispers.com/calendar").get();
                    // retrieve all classes of "ticker"
                    String tickers = doc.getElementsByClass("ticker").text();
                    Elements companyNames = doc.getElementsByClass("company");
                    StringBuilder resultBuilder = new StringBuilder();

                    for (Element element : companyNames) {
                        String companyName = element.text();
                        resultBuilder.append(companyName).append(", ");
                    }
                    if (resultBuilder.length() > 0) {
                        resultBuilder.setLength(resultBuilder.length() - 2);
                    }

                    String result = resultBuilder.toString();
                    String[] companyArray = result.split(", ");
                    System.out.println(Arrays.toString(companyArray));

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
        isQueryRunning = false;
        queryTimer.cancel();
        stopForeground(true);
    }

    public void startStockNewsQuery() {
        isNewsRunning = true;
        newsTimer = new Timer();

        sendNotification();
        System.out.println("sent notif");
        newsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // delete this println when you no longer need it
                    System.out.println("NEWS QUERY");

                    // attempt to make connection to url
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();

                    // if successful
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // read response
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder content = new StringBuilder();

                        // append to a string
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();

                        // cast string result into an array of JSON Objects
                        JSONArray jArray = new JSONArray(content.toString());

                        // loop through array and parse necessary data
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject element = jArray.getJSONObject(i);
                            String title = element.getString("title").toLowerCase();
                            String ticker = element.getString("symbols").toLowerCase();

                            // check if current element matches keyword
                            // only print to console if its new (hasn't been printed already)
                            if (Arrays.stream(keywordSetup).anyMatch(title::contains) && ticker.length() > 0 && !seen.contains(title)) {
                                System.out.println("[*] TITLE: " + title + "\n    TICKERS: " + ticker + "\n");
                                seen.add(title);
                                headlines.add(title + ticker);
                            }
                        }
                    } else {
                        System.out.println("Failed to retrieve JSON data. Response Code: " + responseCode);
                    }

                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 3000); // Delay of 0 milliseconds and repeat every 3 seconds
    }

    public void stopStockNewsQuery() {
        isNewsRunning = false;
        newsTimer.cancel();
        stopForeground(true);
    }

    public static List<String> getTickerBeats() {
        return tickerBeats;
    }

    public static List<String> getHeadlines() {
        return headlines;
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
