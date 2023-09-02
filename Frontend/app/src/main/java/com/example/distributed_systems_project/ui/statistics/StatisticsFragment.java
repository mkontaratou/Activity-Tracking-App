package com.example.distributed_systems_project.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.distributed_systems_project.MainActivity;
import com.example.distributed_systems_project.R;
import com.example.distributed_systems_project.databinding.FragmentStatisticsBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;

    private ImageButton next_graph;
    private ImageButton prev_graph;

    private BarChart barChart;

    private double userElevation;
    private double userDistance;
    private double userDuration;
    private double userSpeed;

    private double globalElevation;
    private double globalDistance;
    private double globalDuration;
    private double globalSpeed;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = FragmentStatisticsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.barChart = getActivity().findViewById(R.id.idBarChart);

        this.barChart.getAxisRight().setDrawGridLines(false);
        this.barChart.getAxisLeft().setDrawGridLines(false);
        this.barChart.getXAxis().setDrawGridLines(false);

        this.barChart.getAxisRight().setDrawLabels(false);
        this.barChart.getAxisLeft().setDrawLabels(false);
        this.barChart.getXAxis().setDrawLabels(false);

        this.barChart.getLegend().setTextSize(20f);
        this.barChart.getDescription().setEnabled(false);

        this.getStatistics();

        this.next_graph = getActivity().findViewById(R.id.next_graph);
        this.next_graph.setOnClickListener(v -> {
            switch (barChart.getData().getDataSetLabels()[0]) {
                case "Elevation Gain (m)":
                    setData(this.userDistance, this.globalDistance, "Distance Travelled (km)");
                    break;

                case "Distance Travelled (km)":
                    setData(this.userDuration * 60, this.globalDuration * 60, "Activity Duration (min)");
                    break;

                case "Activity Duration (min)":
                    setData(this.userSpeed, this.globalSpeed, "Mean Speed (km/h)");
                    break;

                case "Mean Speed (km/h)":
                    setData(this.userElevation, this.globalElevation, "Elevation Gain (m)");
                    break;
            }
        });

        this.prev_graph = getActivity().findViewById(R.id.prev_graph);
        this.prev_graph.setOnClickListener(v -> {
            switch (barChart.getData().getDataSetLabels()[0]) {
                case "Elevation Gain (m)":
                    setData(this.userSpeed, this.globalSpeed, "Mean Speed (km/h)");
                    break;

                case "Distance Travelled (km)":
                    setData(this.userElevation, this.globalElevation, "Elevation Gain (m)");
                    break;

                case "Activity Duration (min)":
                    setData(this.userDistance, this.globalDistance, "Distance Travelled (km)");
                    break;

                case "Mean Speed (km/h)":
                    setData(this.userDuration * 60, this.globalDuration * 60, "Activity Duration (min)");
                    break;
            }
        });
    }

    public void setData(double user_val, double global_val, String label) {
        ArrayList<BarEntry>barEntriesArrayList = new ArrayList<>();

        barEntriesArrayList.add(new BarEntry(0f, (float)user_val));
        barEntriesArrayList.add(new BarEntry(1f, (float)global_val));

        BarDataSet dataset = new BarDataSet(barEntriesArrayList, label);

        dataset.setValueTextColor(Color.BLACK);
        dataset.setValueTextSize(16f); // bar label

        dataset.setColor(ContextCompat.getColor(getContext(), R.color.purple_500));

        BarData barData = new BarData(dataset);

        this.barChart.setData(barData);
        this.barChart.invalidate();
    }


    public void getStatistics() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    Socket socket = new Socket("10.0.2.2", 9000);

                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                    out.writeObject(new Object[]{1});

                    Double[] global_data = (Double[]) in.readObject();

                    globalElevation = global_data[0];
                    globalDistance = global_data[1];
                    globalDuration = global_data[2];
                    globalSpeed = global_data[3];

                    MainActivity activity = ((MainActivity) getActivity());

                    if(activity.numOfUserActivities>0) {
                        userElevation = activity.sumElevation / activity.numOfUserActivities;
                        userDistance = activity.sumDistance / activity.numOfUserActivities;
                        userDuration = activity.sumDuration / activity.numOfUserActivities;
                        userSpeed = activity.sumSpeed / activity.numOfUserActivities;
                    }
                    else {
                        userSpeed = 0;
                    }

                    setData(userElevation, globalElevation, "Elevation Gain (m)");

                    in.close();
                    out.close();
                    socket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}