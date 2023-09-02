package com.example.distributed_systems_project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Master {
    private ServerSocket server;
    private ArrayList<Listener> worker_Listeners = new ArrayList<>();
    private ArrayList<ObjectOutputStream> worker_Streams = new ArrayList<>();
    private int split_size;

    private HashMap<UUID, Object[]> requests; // <request_id, [request, expected_number_of_responses, ArrayList<Double> Intermediate Results, [Total Elevation, Total Distance, Total Time, Mean speed]]>
    private HashMap<Object[], HashMap<String, Double>> segments = new HashMap<>(); // <[Segment Name, Segment Waypoints], <com.example.distributed_systems2.User, Time>>

    public Master(int numOfWorkers , int split_size) {
        this.split_size = split_size;
        this.requests = new HashMap<>();

        try {
            this.server = new ServerSocket(9000);

            this.addSegments();

            //wait for workers to connect
            System.out.println("Expecting " + numOfWorkers + " Workers...");
            for(int i = 0; i < numOfWorkers; i += 1) {
                this.addWorker();
            }

        } catch (IOException e ) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addWorker() throws IOException {
        Socket socket = this.server.accept(); // Accept the connection from Worker
        Listener listener = new Listener(this.requests, new ObjectInputStream(socket.getInputStream())); //create Listener for the connection
        listener.start(); // Start com.example.distributed_systems.Listener Thread

        this.worker_Listeners.add(listener);
        this.worker_Streams.add(new ObjectOutputStream(socket.getOutputStream()));

        System.out.println("Connection from Worker@" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
    }

    private void addSegments() {
        try {
            for(String filename : new String[]{"gpxs/segment1.gpx", "gpxs/segment2.gpx"}) {
                this.segments.put(new Object[] {filename.split("/")[1].split("\\.")[0], gpxParser.parse(new FileInputStream(new File(filename)))[1]}, new HashMap<String,Double>());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Segments Added...");
    }

    public void acceptConnection() {
        try {
            UserRequest request = new UserRequest(this.requests, this.segments, this.worker_Streams, this.split_size, this.server.accept());
            request.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Master master = new Master(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        while(true) {
            master.acceptConnection();
        }
    }
}

