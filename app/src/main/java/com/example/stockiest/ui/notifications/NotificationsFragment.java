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

import java.util.List;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setup();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setup() {
        // get ticker beats list
        List<String> tickerBeatList = StockQueryService.getTickerBeats();

        // get headlines
        List<String> headlines = StockQueryService.getHeadlines();

        TextView headlinesTextView = binding.getRoot().findViewById((R.id.textViewHeadlines));
        headlinesTextView.setText("test\ntest2\ntest3\ntest\ntest2\ntest3\ntest\ntest2\ntest3\ntest\ntest2\ntest3\ntest\ntest2\ntest3\ntest\ntest2\ntest3\ntest\ntest2\ntest3\n");

        if (tickerBeatList != null && !tickerBeatList.isEmpty()) {

            String text = tickerBeatList.get(0) + " beat earnings!\n" +
                    tickerBeatList.get(1) + " beat earnings!\n" +
                    tickerBeatList.get(2) + " beat earnings!\n";

//            textView.setText(text);
//            textView.setTextSize(24);
//            textView.setTextColor(Color.GREEN);
//            textView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        else {
//            textView.setText("None");
//            textView.setTextSize(24);
//            textView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }
}