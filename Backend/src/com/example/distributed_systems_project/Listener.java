package com.example.distributed_systems_project;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

//thread that is always running on master
//used to filter worker responses

public class Listener extends Thread{
    private HashMap<UUID, Object[]> requests; // <request_id, [user_activity, expected_number_of_responses, ArrayList<Double> intermediate_results]>

    private final ObjectInputStream inStream;

    public Listener(HashMap<UUID, Object[]> requests, ObjectInputStream inStream) {
        this.requests = requests;
        this.inStream = inStream;
    }

    public void filter(Object[] response) { // response = [UUID, response_data]
        UUID request_id = (UUID) response[0];
        double[] response_data = (double[]) response[1]; //

        synchronized(this.requests){

            Object[] current_request = this.requests.get(request_id);

            ((ArrayList<double[]>)current_request[2]).add(response_data);

            this.requests.put(request_id, current_request);
            System.out.println("[!] Listener " + this.getName() + ": Request id = " + request_id + ", Data = " + Arrays.toString(response_data));
        }
    }

    public void run(){
        while(true) {
            //read message
            try {
                this.filter((Object[]) this.inStream.readObject());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


}