package com.example.stockiest.ui.notifications;

import android.content.IntentFilter;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.stockiest.R;
import com.example.stockiest.StockiestService;
import com.example.stockiest.databinding.FragmentNotificationsBinding;

import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    List<String> tickerBeatList = StockiestService.getTickerBeats();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (tickerBeatList != null && !tickerBeatList.isEmpty()) {
            List<String> testing = new ArrayList<>(tickerBeatList.subList(0, 3));
            for (String i : testing) {
                TextView textView = new TextView(getActivity());
                textView.setText(i + " beat earnings!");
                textView.setTextSize(24);
                textView.setTextColor(Color.GREEN);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                LinearLayout linearLayout = root.findViewById(R.id.parentLayout);
                linearLayout.addView(textView);
            }
        }
        else {
            TextView textView = new TextView(getActivity());
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