package com.example.stockiest.ui.notifications;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.stockiest.R;
import com.example.stockiest.StockQueryService;
import com.example.stockiest.databinding.FragmentNotificationsBinding;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    private Timer consumeTimer;
    private List<String> tickerBeatList;
    private List<String> headlines;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //setup();
        //consume();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setup();
        consume();
    }

    @Override
    public void onDestroyView() {
        stopConsume();
        super.onDestroyView();
        binding = null;
    }

    public void setup() {
        // get ticker beats list
        tickerBeatList = StockQueryService.getTickerBeats();

        // get headlines
        headlines = StockQueryService.getHeadlines();

        TextView headlinesTextView = binding.getRoot().findViewById((R.id.textViewHeadlines));
        TextView beatsTextView = binding.getRoot().findViewById((R.id.textViewBeats));

        if (headlines.size() > 0) {
            // loop over arrays and display values
            for (String headline : headlines) {
                headlinesTextView.append(headline + "\n");
            }
        }

        if (tickerBeatList.size() > 0) {
            // loop over arrays and display values
            for (String beat : tickerBeatList) {
                beatsTextView.append(beat + "\n");
            }
        }
    }

    public void consume() {
        consumeTimer = new Timer();

        consumeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("consuming...");
                    setup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 3000);
    }

    public void stopConsume() {
        System.out.println("stopped consuming.");
        consumeTimer.cancel();
    }
}