package com.example.distributed_systems_project;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

public class WorkerRequestThread extends Thread { //calculates intermediate result
    private ObjectOutputStream outputStream; //stream to master
    private final UUID request_id; // request id
    private ArrayList<Waypoint> waypoints; // request data

    //Constructor
    public WorkerRequestThread(ObjectOutputStream outputStream, Object[] message) { // [UUID, message]
        this.outputStream = outputStream;
        this.request_id = (UUID) message[0];
        this.waypoints = (ArrayList<Waypoint>) message[1];

        System.out.println("com.example.distributed_systems2.WorkerRequestThread " + this.getName() + ": Chunk size = " + this.waypoints.size());
    }

    public double[] calculate() {
        double sumElevation = 0;
        double sumDistance = 0;


        for (int i = 0;i< this.waypoints.size()-1;i++){
            sumElevation += this.waypoints.get(i).elevationGain(this.waypoints.get(i+1));
            sumDistance += this.waypoints.get(i).distance(this.waypoints.get(i+1));
        }

        return new double[]{sumElevation , sumDistance};
    }

    public void run() {
        try {
            double[] response = this.calculate();

            System.out.println(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()) + "\n  Thread Name=" + this.getName() + "\n  Request id=" + this.request_id + "\n  Request Data Size=" + this.waypoints.size());


            //synchronises use of output stream
            synchronized (this.outputStream) {
                this.outputStream.writeObject(new Object[]{this.request_id, response});
                this.outputStream.reset();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}