package com.example.distributed_systems_project;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class userActivity implements Serializable {
    private ArrayList<Waypoint> waypoints;
    private String creator;

    public userActivity(InputStream inputFile) throws Exception {
        Object[] temp = gpxParser.parse(inputFile);

        this.creator = (String) temp[0];
        this.waypoints = (ArrayList<Waypoint>)temp[1];
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }

    public String getCreator() {
        return creator;
    }
}
