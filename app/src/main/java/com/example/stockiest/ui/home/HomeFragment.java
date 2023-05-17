package com.example.stockiest.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.stockiest.MainActivity;
import com.example.stockiest.R;
import com.example.stockiest.databinding.FragmentHomeBinding;

import java.util.Objects;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Switch newsSwitch;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        newsSwitch = binding.switch1;
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", MODE_PRIVATE);
        boolean isNewsSwitchOn = prefs.getBoolean("newsSwitchOn", false);
        newsSwitch.setChecked(isNewsSwitchOn);
        newsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("newsSwitchOn", isChecked);
                editor.apply();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", MODE_PRIVATE);
            boolean isNewsSwitchOn = prefs.getBoolean("newsSwitchOn", false);
            newsSwitch.setChecked(isNewsSwitchOn);
            mainActivity.setupSwitches();
        }
        else throw new NullPointerException("main is null");
    }
}
