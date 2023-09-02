package com.example.distributed_systems_project;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class UserRequest extends Thread {
    private HashMap<UUID, Object[]> requests; // <request_id, [Activity, expected_number_of_responses, ArrayList<Double> Intermediate Results, [Total Elevation, Total Distance, Total Time, Mean speed]]]>
    private HashMap<Object[], HashMap<String, Double>> segments; // <[Segment Name, Segment Waypoints], <com.example.distributed_systems2.User, Time>>

    private ArrayList<ObjectOutputStream> worker_outStreams;
    private int index;
    private final int split_size;

    private ObjectOutputStream user_outStream;
    private ObjectInputStream user_inStream;

    private UUID req_id;

    public UserRequest(HashMap<UUID, Object[]> requests, HashMap<Object[], HashMap<String, Double>> segments, ArrayList<ObjectOutputStream> w_outStreams, int split_size, Socket socket) {
        this.requests = requests;
        this.segments = segments;
        this.worker_outStreams = w_outStreams;
        this.index = 0;
        this.split_size = split_size;

        try {
            this.user_outStream = new ObjectOutputStream(socket.getOutputStream());
            this.user_inStream = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumOfChunks(int size, int split_size) {
        int counter = 0;

        int x = 0;
        for (x = 0; x < size - split_size; x += split_size - 1) {
            counter++;
        }

        if (x != size - 1) {
            counter++;
        }

        return counter;
    }

    //returns next worker
    private Iterator<ObjectOutputStream> roundRobin() {
        return new Iterator<ObjectOutputStream>() {
            @Override
            public boolean hasNext() {
                return true;
            }
            @Override
            public ObjectOutputStream next() {
                //if we have reached the last worker, return to the first worker
                if (index >= worker_outStreams.size()) {
                    index = 0;
                }
                return worker_outStreams.get(index++);
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private void map(ArrayList<Waypoint> waypoints) {
        try {
            int x;

            for(x=0; x < waypoints.size() - this.split_size; x += this.split_size - 1 ) {
                this.roundRobin().next().writeObject(new Object[] {this.req_id, new ArrayList<>(waypoints.subList(x , x + this.split_size))});
            }

            //if there are still waypoints left
            if(x != waypoints.size() - 1){
                this.roundRobin().next().writeObject(new Object[] {this.req_id, new ArrayList<>(waypoints.subList(x , waypoints.size()))});
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void reduce(Waypoint first_waypoint, Waypoint last_waypoint) {
        int expected_intermediate_results = (int)this.requests.get(this.req_id)[1];

        System.out.println("com.example.distributed_systems2.UserRequest " + this.getName() + ": Waiting for Intermediate Results");
        while(((ArrayList<Double[]>)this.requests.get(this.req_id)[2]).size() != expected_intermediate_results) {

        }

        Object[] user_request;
        synchronized (this.requests) {
            user_request = this.requests.get(this.req_id);
        }

        double totalElevation = 0.0;
        double totalDistance = 0.0;

        ArrayList<double[]> intermediate_results = (ArrayList<double[]>) user_request[2];

        for (double[] result : intermediate_results) {
            totalElevation += result[0];
            totalDistance += result[1];

            System.out.println(totalElevation + " " + totalDistance);
        }

        double totalTime = first_waypoint.totalTime(last_waypoint);

        double meanSpeed = totalDistance/totalTime;

        Double[] payload = new Double[] {totalElevation, totalDistance, totalTime, meanSpeed};

        user_request[3] = payload;

        synchronized(this.requests){
            this.requests.put(this.req_id, user_request);
        }

        try {
            Thread.sleep(2000);

            this.user_outStream.writeObject(payload);
            this.user_outStream.reset();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkForSegments(userActivity user_activity) {

        synchronized (this.segments) {
                for (Object[] segment : this.segments.keySet()) {
                    ArrayList<Waypoint> segment_wpts = (ArrayList<Waypoint>) segment[1];

                    int index = Collections.indexOfSubList(user_activity.getWaypoints(), segment_wpts);
                    System.out.println("Checking segment: Index = " + index);

                    if(index!=-1) { // Segment Found
                        System.out.println("Thread " + this.getName() + ": Segment Found at " + index);

                        HashMap<String, Double> segment_stats = this.segments.get(segment);

                        double min_time = user_activity.getWaypoints().get(index).totalTime(user_activity.getWaypoints().get(index + segment_wpts.size()-1));

                        if(segment_stats.containsKey(user_activity.getCreator())) {
                            double current_time = segment_stats.get(user_activity.getCreator());

                            min_time = Math.min(current_time, min_time);
                        }

                        segment_stats.put(user_activity.getCreator(), min_time);

                        segment_stats = segment_stats.entrySet().stream()
                                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                        (a,b)->b, LinkedHashMap::new));

                        this.segments.put(segment, segment_stats);
                }
            }
        }
    }

    public void uploadGPX(userActivity user_activity) {

        synchronized (this.requests) {

            //  Adding new com.example.distributed_systems2.User Request to requests
            UUID new_id = UUID.randomUUID();
            while (this.requests.containsKey(new_id)) {
                new_id = UUID.randomUUID();
            }

            this.req_id = new_id;
            this.requests.put(this.req_id, new Object[]{user_activity.getWaypoints(), this.getNumOfChunks(user_activity.getWaypoints().size(), this.split_size), new ArrayList<Double>(), new Double[4]});
        }

        this.checkForSegments(user_activity);

        this.map(user_activity.getWaypoints());
        this.reduce(user_activity.getWaypoints().get(0), user_activity.getWaypoints().get(user_activity.getWaypoints().size()-1));

        System.out.println("[DONE] Upload GPX: Thread = " + this.getName() + ": " + " Id = " + this.req_id + " Creator = " + user_activity.getCreator());
    }

    public void getAllStatistics() {
        Double[] payload = new Double[] {0.0 ,0.0, 0.0, 0.0}; // [Mean Elevation Gain, Mean Distance, Mean Duration, Mean Speed]
        int num_of_requests;

        synchronized (this.requests) {
            for (Object[] request : this.requests.values()) {
                payload[0] += ((Double[]) request[3])[0];
                payload[1] += ((Double[]) request[3])[1];
                payload[2] += ((Double[]) request[3])[2];
            }

            num_of_requests = this.requests.values().size();
        }

        if(num_of_requests>0) {
            payload[0] /= num_of_requests;
            payload[1] /= num_of_requests;
            payload[2] /= num_of_requests;
            payload[3] = payload[1]/payload[2];
        }

        try {
            this.user_outStream.writeObject(payload);
            this.user_outStream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("[DONE] Get All Statistics: Thread = " + this.getName());
    }

    public void getSegmentStatistics() {
        synchronized (this.segments) {
            try {

                HashMap<String, HashMap<String, Double>> clean_segments = new HashMap<>();

                for(Object[] key : this.segments.keySet()) {
                    clean_segments.put((String)key[0], this.segments.get(key));
                }

                this.user_outStream.writeObject(clean_segments);
                this.user_outStream.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("[DONE] Get Segment Statistics: Thread = " + this.getName());
    }


    public void run() {
        Object[] input;

        try {
            input = (Object[])this.user_inStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if((int)input[0] == 0) { //Upload GPX
            this.uploadGPX((userActivity) input[1]);
        }
        else if((int)input[0] == 1) { // Get all statistics
            this.getAllStatistics();
        }
        else if((int)input[0] == 2) { // Get segment statistics
            this.getSegmentStatistics();
        }
    }

}