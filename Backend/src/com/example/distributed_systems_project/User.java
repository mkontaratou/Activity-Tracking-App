package com.example.distributed_systems_project;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

public class User {
    public static void upload(String name) throws Exception {
        System.out.println("\nUpload GPX Request");

        Socket socket = new Socket("Localhost", 9000);
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        out.writeObject(new Object[]{0, new userActivity(new FileInputStream("gpxs/" + name + ".gpx"))});
        System.out.println("Master Response: " + Arrays.toString((Double[])in.readObject()));

        in.close();
        out.close();
        socket.close();
    }

    public static void all_statistics() throws IOException, ClassNotFoundException {
        System.out.println("\nAll Statistics Request");

        Socket socket = new Socket("Localhost", 9000);
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        out.writeObject(new Object[]{1});

        System.out.println(Arrays.toString((Double[]) in.readObject()));

    }

    public static void segments() throws IOException, ClassNotFoundException {
        System.out.println("\nSegment Statistics Request");

        Socket socket = new Socket("Localhost", 9000);
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());


        out.writeObject(new Object[]{2});

        HashMap<String, HashMap<String, Double>> stats = (HashMap<String, HashMap<String, Double>>) in.readObject();

        stats.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).forEach(System.out::println);
    }

    public static void main(String[] args) throws Exception {
        upload("route1");
        upload("route2");
        upload("route3");
        upload("route4");
        upload("route5");
        upload("route6");


        all_statistics();

        segments();

    }
}
