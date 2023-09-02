package com.example.distributed_systems_project.ui.segments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.example.distributed_systems_project.R;
import com.example.distributed_systems_project.databinding.FragmentSegmentsBinding;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SegmentsFragment extends Fragment {

    private FragmentSegmentsBinding binding;

    private Button top_button;

    private TableLayout table;

    private TextView segmentName;

    private HashMap<String, HashMap<String, Double>> segment_data;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = FragmentSegmentsBinding.inflate(inflater, container, false);

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.top_button = (Button) getView().findViewById(R.id.top_button);

        this.table = (TableLayout) getView().findViewById(R.id.table);
        this.table.isStretchAllColumns();
        this.segmentName = getView().findViewById(R.id.segmentName);

        this.resetSegments();
    }

    public void resetSegments() {
        this.table.removeAllViews();
        this.table.setTranslationY(0);

        this.top_button.setText("ALL SEGMENTS");
        this.top_button.setEnabled(false);


        Thread thread = new Thread(() -> {
            try  {
                Socket socket = new Socket("10.0.2.2", 9000);

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeObject(new Object[]{2});

                segment_data = (HashMap<String, HashMap<String, Double>>) in.readObject();

                ArrayList<String> sortedKeys = new ArrayList<>(segment_data.keySet());
                Collections.sort(sortedKeys);

                TableRow tr;
                segmentName.setVisibility(View.INVISIBLE);

                for(int i=0; i<segment_data.keySet().size(); i++) {

                    tr = new TableRow(getContext());
                    tr.setGravity(Gravity.CENTER);

                    Button bt = new Button(getContext());
                    bt.setText((String)(sortedKeys.get(i)));
                    bt.setId(i);

                    bt.setTextColor(getActivity().getResources().getColor(R.color.white));
                    bt.setBackgroundTintList(getActivity().getResources().getColorStateList(R.color.purple_500));
                    bt.setTextSize(20);

                    bt.setOnClickListener(v -> specificSegment(segment_data.keySet().size()- 1 - bt.getId()));
                    tr.addView(bt);

                    final TableRow tr_final = tr;
                    requireActivity().runOnUiThread(() -> table.addView(tr_final));
                }

                in.close();
                out.close();
                socket.close();

            } catch (Exception e) {
            e.printStackTrace();
        }
    });

        thread.start();
    }

    public void specificSegment(int key_num) {
        this.table.removeAllViews();
        this.table.setTranslationY(160);

        this.top_button.setText("RETURN TO ALL SEGMENTS");
        this.top_button.setEnabled(true);
        this.top_button.setOnClickListener(v -> this.resetSegments());

        HashMap<String, Double> stats = this.segment_data.get(this.segment_data.keySet().toArray()[key_num]);

        TableRow tr;
        TextView position;
        TextView name;
        TextView time;

        DecimalFormat df = new DecimalFormat("0.00");

        ArrayList<String> usernames = new ArrayList(stats.keySet());

        tr = new TableRow(getContext());

        this.segmentName.setText("Leaderboard " + (String)(this.segment_data.keySet().toArray()[key_num]));

        this.segmentName.setTextSize(25);
        this.segmentName.setVisibility(View.VISIBLE);

        tr.setGravity(Gravity.CENTER);
        position = new TextView(getContext());
        position.setText("Position   ");
        position.setTextSize(25);
        position.setTypeface(Typeface.DEFAULT_BOLD);
        tr.addView(position);


        name = new TextView(getContext());
        name.setText("Username   ");
        name.setTextSize(25);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        tr.addView(name);

        time = new TextView(getContext());
        time.setText("Time");
        time.setTextSize(25);
        time.setTypeface(Typeface.DEFAULT_BOLD);
        tr.addView(time);


        this.table.addView(tr);

        for (String user : usernames) {
            tr = new TableRow(getContext());
            tr.setGravity(Gravity.CENTER);

            position = new TextView(getContext());

            name = new TextView(getContext());

            time = new TextView(getContext());


            position.setText(String.valueOf(usernames.indexOf(user)+1));
            position.setTextSize(20);
            name.setText(user);
            name.setTextSize(20);
            time.setText((df.format(stats.get(user) * 60)) + "min");
            time.setTextSize(20);

            tr.addView(position);
            tr.addView(name);
            tr.addView(time);

            this.table.addView(tr);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}