package com.example.distributed_systems_project;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Waypoint implements Serializable {
    private final Double elevation;
    private final LocalDateTime time;
    private final double lat;
    private final double lon;

    //Constructor
    public Waypoint(double lat, double lon, double elevation, String time) {
        this.elevation = elevation;
        this.time = parseTime(time);
        this.lat = lat;
        this.lon = lon;
    }

    //parses String time to LocalDateTime
    private static LocalDateTime parseTime(String time) {
        //this is how time is displayed on gpx file
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return format.parse(time).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
    public LocalDateTime getTime() {
        return this.time;
    }

    //calculates distance from this to nextWaypoint
    public double distance(Waypoint nextWaypoint) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(nextWaypoint.lat - this.lat);
        double lonDistance = Math.toRadians(nextWaypoint.lon - this.lon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(nextWaypoint.lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; //convert to meters

        double height = nextWaypoint.elevation - this.elevation;
        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance)/1000; //converts to km
    }

    //calculates time from this to nextWaypoint
    public double totalTime(Waypoint nextWaypoint){
        return this.getTime().until(nextWaypoint.getTime(),ChronoUnit.NANOS) * 2.77777778 * Math.pow(10,-13); //converts to h
    }

    public double elevationGain(Waypoint nextWaypoint){
        double elevDif = nextWaypoint.elevation - this.elevation;
        //checks if elevation has increased
        if (elevDif>0)
            return elevDif;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Waypoint)) {
            return false;
        }

        return this.distance((Waypoint) o) < 0.01; // 10 meters
    }
}
