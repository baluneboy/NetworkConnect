package com.example.android.common.devices;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class DeviceDeltasComparator implements Comparator<String> {

    Map<String, DeviceDeltas> base;
    public DeviceDeltasComparator(Map<String, DeviceDeltas> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are NOT consistent with equals.
    public int compare(String a, String b) {
        Calendar t1 = Calendar.getInstance();
        t1.setTime(base.get(a).getTime());
        Calendar t2 = Calendar.getInstance();
        t2.setTime(base.get(b).getTime());
        Long aTime = t1.getTimeInMillis();
        Long bTime = t2.getTimeInMillis();
        if (aTime >= bTime) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

public class MySortedMap {

    public static void main(String[] args) {
        
        HashMap<String,DeviceDeltas> map = new HashMap<String,DeviceDeltas>();
        DeviceDeltasComparator bvc =  new DeviceDeltasComparator(map);
        TreeMap<String,DeviceDeltas> sorted_map = new TreeMap<String,DeviceDeltas>(bvc);

        String line1 = "2015:159:21:22:03 es05rt CIR";
        //DeviceDeltas dt1 = new DeviceDeltas(line1, context);

        String line2 = "2015:159:21:22:00 es06rt DIR";
        //DeviceDeltas dt2 = new DeviceDeltas(line2, context);
        
        String line3 = "2015:159:21:22:09 es07rt WIR";
        //DeviceDeltas dt3 = new DeviceDeltas(line3, context);
        
        //map.put("es05rt", dt1);
        //map.put("es06rt", dt2);
        //map.put("es07rt", dt3);
        
    	// Iterate to display unsorted mapped values
        System.out.println("UNSORTED:");
        try {
            for (Map.Entry<String, DeviceDeltas> entry: map.entrySet()) {
                //String key = entry.getKey();
                DeviceDeltas dev = entry.getValue();
                System.out.println(dev);
            }
        } catch (Exception e) {
            //You'll need to add proper error handling here
            System.out.println("SOMETHING WRONG WITH ENTRIES IN MAP.");
        } 
        
        // Sort by GMT
        sorted_map.putAll(map);
       
    	// Iterate to display sorted mapped values
        System.out.println("SORTED BY GMT:");
        try {
            for (Map.Entry<String, DeviceDeltas> entry: sorted_map.entrySet()) {
                //String key = entry.getKey();
                DeviceDeltas dev = entry.getValue();
                System.out.println(dev);
            }
        } catch (Exception e) {
            //You'll need to add proper error handling here
            System.out.println("SOMETHING WRONG WITH ENTRIES IN MAP.");
        }           
        
    }
}
