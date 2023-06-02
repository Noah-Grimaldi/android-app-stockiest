package com.example.stockiest.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.stockiest.R;
import com.example.stockiest.StockQueryService;
import com.example.stockiest.databinding.FragmentNotificationsBinding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<String> tickerBeatList = StockQueryService.getTickerBeats();
        Set<String> tickerBeatSet = new HashSet<>(tickerBeatList);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("stockBeatsAndNews", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("dailyTickerBeats", tickerBeatSet);
        editor.apply();

        tickerBeatSet = sharedPreferences.getStringSet("dailyTickerBeats", new HashSet<>());
        System.out.println("What's saved: " + tickerBeatSet);

        TextView textView = new TextView(getActivity());

        if (tickerBeatList != null && !tickerBeatList.isEmpty()) {

            String text = tickerBeatList.get(0) + " beat earnings!\n" +
                          tickerBeatList.get(1) + " beat earnings!\n" +
                          tickerBeatList.get(2) + " beat earnings!\n";

            textView.setText(text);
            textView.setTextSize(24);
            textView.setTextColor(Color.GREEN);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout linearLayout = root.findViewById(R.id.parentLayout);
            linearLayout.addView(textView);
        }
        else {
            textView.setText("None");
            textView.setTextSize(24);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout linearLayout = root.findViewById(R.id.parentLayout);
            linearLayout.addView(textView);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}