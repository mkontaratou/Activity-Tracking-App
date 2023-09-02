package com.example.distributed_systems_project;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

//Workers are connected to the master the moment they are created
//this connection stays open
public class Worker {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public Worker () {

        try {
            //connect to master
            this.socket = new Socket("localhost", 9000);
            this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.inputStream = new ObjectInputStream(this.socket.getInputStream());

            System.out.println("Connection to Master@" +  this.socket.getInetAddress().getHostAddress() + ":" +  this.socket.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //thread is created when worker receives request from master
    public void acceptRequest() throws java.net.SocketException{
        try {
            WorkerRequestThread object = new WorkerRequestThread(this.outputStream, (Object[])this.inputStream.readObject());
            object.start();

        } catch (java.net.SocketException e) {
            throw e;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        //create worker
        Worker worker = new Worker();

        while (true) {
            try {
                worker.acceptRequest();
            } catch (SocketException e) {
                System.out.println("com.example.distributed_systems2.Master is down");
                break;
            }
        }
    }
}