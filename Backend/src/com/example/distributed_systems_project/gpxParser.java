package com.example.distributed_systems_project;

import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class gpxParser {

    //reads file and returns it as document
    public static Document readFile(InputStream stream) throws Exception { //exception is thrown if user gives invalid file name (caught in user.java)
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(stream);
        doc.getDocumentElement().normalize();
        return doc;
    }


    //returns Arraylist of waypoints read from file
    public static Object[] parse(InputStream stream) throws Exception { //exception is thrown if user gives invalid file name (caught in user.java)
        ArrayList<Waypoint> output = new ArrayList<>();

        Document doc = readFile(stream);

        String creator = doc.getDocumentElement().getAttribute("creator");

        NodeList node_list = doc.getElementsByTagName("wpt");
        Node node;
        //for every waypoint
        for (int temp = 0; temp < node_list.getLength(); temp++) {
            node = node_list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                //add waypoint to Arraylist
                output.add(new Waypoint(Double.parseDouble(eElement.getAttribute("lat")), Double.parseDouble(eElement.getAttribute("lon")), (float) Double.parseDouble(eElement.getElementsByTagName("ele").item(0).getTextContent()), eElement.getElementsByTagName("time").item(0).getTextContent()));
            }
        }
        return new Object[] {creator, output};
    }
}