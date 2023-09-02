package com.example.distributed_systems_project.ui.upload;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.distributed_systems_project.MainActivity;
import com.example.distributed_systems_project.R;
import com.example.distributed_systems_project.userActivity;
import com.example.distributed_systems_project.databinding.FragmentUploadBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UploadFragment extends Fragment implements View.OnClickListener {

    private FragmentUploadBinding binding;
    private UploadViewModel uploadViewModel;
    private static final int PICK_FILE_REQUEST_CODE = 1;

    private Button upload_button;

    private TextView upload_done;
    private TextView elevation_gain;
    private TextView distance_travelled;
    private TextView activity_duration;
    private TextView mean_speed;
    private  Snackbar snackbar;

    private boolean upload_finished = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = FragmentUploadBinding.inflate(inflater, container, false);
        this.uploadViewModel = new ViewModelProvider(this).get(UploadViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.upload_button = (Button) getView().findViewById(R.id.upload_button);
        this.upload_button.setOnClickListener(this);

        this.upload_done = (TextView) getView().findViewById(R.id.upload_done);
        this.elevation_gain = (TextView) getView().findViewById(R.id.elevation_gain);
        this.distance_travelled = (TextView) getView().findViewById(R.id.distance_travelled);
        this.activity_duration = (TextView) getView().findViewById(R.id.activity_duration);
        this.mean_speed = (TextView) getView().findViewById(R.id.mean_speed);

        this.snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.done, Snackbar.LENGTH_LONG);

        if (this.upload_finished){
            showResults();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        // Inside a button click listener or any appropriate event handler
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Specify the file type(s) you want to allow for selection
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }


    //For uploading purposes
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedFileUri = data.getData();

                userActivity new_userActivity = null;

                try {
                    InputStream inputStream = getActivity().getApplicationContext().getContentResolver().openInputStream(selectedFileUri);
                    new_userActivity = new userActivity(inputStream);
                } catch (Exception e) {
                    System.out.println("Not a valid GPX file");
                    e.printStackTrace();
                }

                this.uploadGPX(new_userActivity);

            }
        }
    }

    public void showResults(){

        this.elevation_gain.setText("Elevation Gain: " + this.uploadViewModel.elevation_gain + "m");
        this.distance_travelled.setText("Distance Travelled: " + this.uploadViewModel.distance + "km");
        this.activity_duration.setText("Activity Duration: " + this.uploadViewModel.duration + "h");
        this.mean_speed.setText("Mean Speed: " + this.uploadViewModel.speed + "km/h");

        this.elevation_gain.setVisibility(View.VISIBLE);
        this.distance_travelled.setVisibility(View.VISIBLE);
        this.activity_duration.setVisibility(View.VISIBLE);
        this.mean_speed.setVisibility(View.VISIBLE);

        this.upload_done.setVisibility(View.VISIBLE);
        this.upload_button.setEnabled(true);
    }

    public void hideResults(){
        upload_button.setEnabled(false);

        this.upload_done.setVisibility(View.INVISIBLE);

        this.elevation_gain.setVisibility(View.INVISIBLE);
        this.distance_travelled.setVisibility(View.INVISIBLE);
        this.activity_duration.setVisibility(View.INVISIBLE);
        this.mean_speed.setVisibility(View.INVISIBLE);
    }

    private void uploadGPX(userActivity new_userActivity) {
        this.upload_finished = false;

        hideResults();

        Thread thread = new Thread(() -> {
            try {
                Socket socket = new Socket("10.0.2.2", 9000);

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeObject(new Object[]{0, new_userActivity});

                Double[] response = (Double[]) in.readObject();

                in.close();

                out.close();

                socket.close();

                upload_finished = true;
                snackbar.show();

                uploadViewModel.elevation_gain = response[0];
                uploadViewModel.distance = response[1];
                uploadViewModel.duration = response[2];
                uploadViewModel.speed = response[3];

                MainActivity activity = (MainActivity) getActivity();

                activity.numOfUserActivities += 1;
                activity.sumElevation += uploadViewModel.elevation_gain;
                activity.sumDistance += uploadViewModel.distance;
                activity.sumDuration += uploadViewModel.duration;
                activity.sumSpeed += uploadViewModel.speed;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResults();
                    }

                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }
}



