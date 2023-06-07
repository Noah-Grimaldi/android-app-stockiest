package com.example.stockiest.ui.notifications;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.stockiest.R;
import com.example.stockiest.StockQueryService;
import com.example.stockiest.databinding.FragmentNotificationsBinding;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    private Timer consumeTimer;
    private final int smoothGreenColor = Color.rgb(0, 100, 0);

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
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

    /**
     * function to retrieve values from stock services
     */
    public void setup() {
        // get ticker beats list
        List<String> tickerBeatList = StockQueryService.getTickerBeats();

        // get headlines
        List<String> headlines = StockQueryService.getHeadlines();

        TextView headlinesTextView = binding.getRoot().findViewById((R.id.textViewHeadlines));
        TextView beatsTextView = binding.getRoot().findViewById((R.id.textViewBeats));

        headlinesTextView.setText("");
        beatsTextView.setText("");

        if (headlines.size() > 0) {
            // loop over arrays and display values
            for (String headline : headlines) {
                SpannableString spanStr = new SpannableString(headline);
                spanStr.setSpan(new StyleSpan(Typeface.BOLD), 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanStr.setSpan(new ForegroundColorSpan(smoothGreenColor), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                spanStr.setSpan(new StyleSpan(Typeface.NORMAL), 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanStr.setSpan(new ForegroundColorSpan(Color.BLACK), 10, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                headlinesTextView.append(spanStr);
            }
        }

        if (tickerBeatList.size() > 0) {
            // loop over arrays and display values
            for (String beat : tickerBeatList) {
                SpannableString spanStr = new SpannableString(beat);
                spanStr.setSpan(new ForegroundColorSpan(smoothGreenColor), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanStr.setSpan(new ForegroundColorSpan(Color.BLACK), 4, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                beatsTextView.append(spanStr);
            }
        }
    }

    /**
     * method to constantly check for updates from the service
     */
    public void consume() {
        consumeTimer = new Timer();

        consumeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Handler handler = new Handler(Looper.getMainLooper());
                    System.out.println("consuming...");
                    handler.post(() -> setup());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 3000);
    }

    /**
     * Method for testing purposes
     */
    public void testAdd() {
        Timer testTimer = new Timer();

        testTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    StockQueryService.getTickerBeats().add(0, "TEST\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 7000, 7000);
    }

    public void stopConsume() {
        System.out.println("stopped consuming.");
        consumeTimer.cancel();
    }
}