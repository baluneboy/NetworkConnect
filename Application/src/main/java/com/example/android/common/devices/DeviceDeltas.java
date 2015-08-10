package com.example.android.common.devices;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pims on 6/15/15.
 */
public class DeviceDeltas {

    // member variables
    private Date time;              // device time
    private String device;          // device like "121f03rt"
    private String tag;             // device tag like "SE"
    private Float deltaHost;        // device time minus host time in seconds
    private Float deltaKu;          // device time minus Ku time in seconds
    private Boolean found;          // true if pattern found

    // class constants
    private static final String pattern = "(\\d{4}:\\d+:\\d{2}:\\d{2}:\\d{2})\\s(.*)\\s(.*)";
    private static final Pattern rx = Pattern.compile(pattern);
    //private static final SimpleDateFormat YYYYDDD = new SimpleDateFormat("yyyy:DDD:");
    private static final SimpleDateFormat DOY = new SimpleDateFormat("DDD:");
    private static final SimpleDateFormat HHMMSS = new SimpleDateFormat("HH:mm:ss");

    /**********************************************************
     Method:         Default Constructor
     Purpose:        Create a new DeviceDeltas object and initialize it
                     with invalid deltas
     Parameters:     line string to be parsed like "2015:166:23:18:59 Ku_AOS GSE"
     Preconditions:  None
     Postconditions: a new DeviceDeltas object is created with null deltas
     ***********************************************************/
    public DeviceDeltas(String line) {

        // Create matcher object
        Matcher m = rx.matcher(line);
        found = m.find();

        // convert string to datetime
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:DDD:HH:mm:ss");
        try {
            time = simpleDateFormat.parse(m.group(1));
            // handle the reverse ordering for special case of host
            if (m.group(3).equals("HOST")) {
                device = m.group(3).toLowerCase();
                tag = m.group(2).toUpperCase();
            }
            else {
                device = m.group(2).toLowerCase();
                tag = m.group(3).toUpperCase();
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**********************************************************
     Method:         toString
     Purpose:        Convert the internal representation of DeviceDeltas,
                     to a String which could then be printed to the screen
     Parameters:     None
     Preconditions:  None
     Postconditions: The value of the "this" object will be converted
                     to a String
     Returns:        A String representation of the "this" object
     ***********************************************************/
    public String toString()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd,DDD/HH:mm:ss");
        String t = dateFormat.format(time);
        String buffer = String.format("%s %-9s %-9s", t, device, tag);
        buffer += String.format("  dHo = %9.1fs,", deltaHost);
        buffer += String.format("  dKu = %9.1fs", deltaKu);
        return buffer;
    }

    /********************************************/
    /* public accessor methods for private data */
    /********************************************/
    // getters
    public Date getTime() { return time; }
    public String getDevice() { return device; }
    public String getTag() { return tag; }
    public float getDeltaHost() { return deltaHost; }
    public float getDeltaKu() { return deltaKu; }

    private Float getDeltaSec(DeviceDeltas dd) {
        Calendar otherTime = Calendar.getInstance();
        otherTime.setTime(dd.time);
        Calendar myTime = Calendar.getInstance();
        myTime.setTime(time);
        return (float) (myTime.getTimeInMillis() - otherTime.getTimeInMillis()) / 1000;
    }

    // setters
    private void setDelta(DeviceDeltas dd) {
        if (isHost(dd)) {
            deltaHost = getDeltaSec(dd);
        } else if (isKu(dd)) {
            deltaKu = getDeltaSec(dd);
        } else {
            throw new IllegalArgumentException("invalid argument for device, not in (Ku_AOS, HOST)");
        }
    }

    private static Boolean isHost(DeviceDeltas dd) { return dd.device.equals("host"); }

    private static Boolean isKu(DeviceDeltas dd) { return dd.device.equals("ku_aos"); }

    /******************************************************/
    /* public action methods for manipulating DeviceDeltas */
    /******************************************************/

    /**********************************************************
     Method:         subtract
     Purpose:        subtract two DeviceDeltas, a minus b, where a is the "this"
                     object, and b is passed as the input parameter
     Parameters:     b, the fraction to subtract from "this"
     Preconditions:  both DeviceDeltas a and b must contain valid times
     Postconditions: None
     ***********************************************************/
    private float subtract(DeviceDeltas b) {
        // check preconditions
        if (time == b.time)
            throw new IllegalArgumentException("FIXME just testing: the times were the same!");
        // create new value to return as difference
        float diff = this.time.getTime() - b.time.getTime();
        return diff;
    }

    private static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    private static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    private static void demoJsoup(String url, int tabnum, Boolean header) {

        // Create an array
        int start = 0;
        ArrayList arraylist = new ArrayList<HashMap<String, String>>();
/*        try {
            // Connect to the Website URL
            Document doc = Jsoup.connect(url).get();
            ArrayList<String> downServers = new ArrayList<>();

            // Identify Table Class "worldpopulation"
            Element table = doc.select("table").get(tabnum); // zero selects first table
            Elements rows = table.select("tr");

            System.out.print("Table(" + tabnum + ") has " + rows.size() + " rows");
            if (header) {
                start = 1;
                System.out.println(" (including header row).");
            }
            else {
                System.out.println(" (no header row).");
            }
            for (int i = start; i < rows.size(); i++) { //first row is the col names so skip it.
                Element row = rows.get(i);
                Elements cols = row.select("td");

*//*                if (cols.get(2).text().equals("HOST")) {
                    System.out.println("AT HOST ROW NOW");
                }*//*
                System.out.println(cols.get(0).text() + " " + cols.get(1).text() + " " + cols.get(2).text());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

    }

    public static void main( String args[] ) throws Exception {

        // URL Address
        String url = "http://pims.grc.nasa.gov/plots/user/sams/status/sensortimes.txt";

    }

    public static TreeMap<String, DeviceDeltas> getSortedMap( String result ){

        // Get device times mapping from result text (string)
        Map<String,DeviceDeltas> map = getMapFromResultString(result);
        DeviceDeltasComparator dtc =  new DeviceDeltasComparator(map);
        TreeMap<String,DeviceDeltas> sorted_map = new TreeMap<String,DeviceDeltas>(dtc);

        // Sort by GMT
        sorted_map.putAll(map);

        // Iterate sorted_map entries to establish deltas
        try {
            for (Map.Entry<String, DeviceDeltas> entry: sorted_map.entrySet()) {
                DeviceDeltas dev = entry.getValue();
                dev.setDelta(map.get("host"));
                dev.setDelta(map.get("ku_aos"));
            }
        } catch (Exception e) {
            //You'll need to add proper error handling here
            //System.out.println("SOMETHING WRONG WITH ENTRIES IN MAP...GOT ku_aos AND host ENTRIES?");
            e.printStackTrace();
        }

        return sorted_map;
    }

    private static Map<String, DeviceDeltas> getMapFromResultString(String result) {

        // Iterate over lines, put usable ones into map
        Map<String, DeviceDeltas> map = new HashMap<String, DeviceDeltas>();

        String[] lines = result.split("\\r?\\n");
        for (String line: lines) {
            System.out.println("LINE IS: " + line);
            // FIXME next if lines should be regular expression match
            //       or maybe try/catch around 2 lines below the if lines?
            if (line.startsWith("begin") || line.startsWith("end")) continue;
            if (line.startsWith("2015-") || line.startsWith("---")) continue;
            if (line.startsWith("yyyy") || line.startsWith("xxx")) continue;
            DeviceDeltas dd = new DeviceDeltas(line);
            map.put(dd.device, dd);
        }

        return map;

    }

    private static Map<String, DeviceDeltas> getMapFromFile(String fname) {
        //Get the text file
        File file = new File(fname);

        //Read text from file, put into map
        Map<String, DeviceDeltas> map = new HashMap<String, DeviceDeltas>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
            	// FIXME next if lines should be regular expression match
            	//       or maybe try/catch around 2 lines below the if lines?
            	if ( line.startsWith("begin") || line.startsWith("end") ) continue;
            	if ( line.startsWith("2015-") || line.startsWith("---") ) continue;            	
            	if ( line.startsWith("yyyy")  || line.startsWith("xxx") ) continue;            	
                DeviceDeltas dd = new DeviceDeltas(line);
                map.put(dd.device, dd);
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
			System.out.println("IOException");
        }
        
        return map;

    }	
    
    public static void showMatch(Boolean bFound, Matcher m) {
        if (bFound) {
            System.out.println("matcher count: " + m.groupCount());
            for(int i=1; i<m.groupCount()+1; i++) {
                System.out.println("m.group(" + i + "): " + m.group(i) );
            }
        } else {
            System.out.println("NO MATCH");
        }
        
    }

    public static SpannableString getSpannableFromMap(TreeMap<String, DeviceDeltas> sorted_map, List<String> ignore_devices, TextView tvResult) {

        //List<SpannableString> results = new ArrayList<SpannableString>();
        int start;
        int countBadDeltaHosts = 0;
        int countBadDeltaKus = 0;

        // now we have sorted map, so iterate to build sorted, formatted spannables
        SpannableStringBuilder deviceLines = new SpannableStringBuilder();
        try {

            // header line
            deviceLines.append("DOY:hh:mm:ss  dHost    dKu  device\n");
            deviceLines.append("----------------------------------\n");
            deviceLines.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (TreeMap.Entry<String, DeviceDeltas> entry: sorted_map.entrySet()) {
                DeviceDeltas dev = entry.getValue();
                String device_name = dev.getDevice();
                Date device_time = dev.getTime();
                float device_dh = dev.getDeltaHost();
                float device_dk = dev.getDeltaKu();

                // FIXME when we are get to host entry, entire lines background gets distinct color
                // FIXME user config for dimmed devices

                // TODO make a home for snippet keeper for repeat character like this
                // the next line shows how to repeat char "-" 80 times
                //    Log.i("SEP", new String(new char[80]).replace("\0", "-"));

                // if host, then make this a distinctive line via bg color
                if (device_name.equals("host")) {

                    start = deviceLines.length();

                    deviceLines.append(DOY.format(device_time));
                    deviceLines.append(HHMMSS.format(device_time));
                    deviceLines.append(String.format(" %6.1f", device_dh));
                    deviceLines.append(String.format(" %6.1f", device_dk));
                    deviceLines.append("  " + device_name + "      ");
                    deviceLines.setSpan(new ForegroundColorSpan(Color.BLACK), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    deviceLines.setSpan(new BackgroundColorSpan(Color.WHITE), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                }
                // if this is device to ignore, dim this row
                else if (ignore_devices.contains(device_name)) {

                    start = deviceLines.length();

                    deviceLines.append(DOY.format(device_time));
                    deviceLines.append(HHMMSS.format(device_time));
                    deviceLines.append("       ");
                    deviceLines.append("       ");
                    deviceLines.append("  " + device_name + "      ");
                    deviceLines.setSpan(new ForegroundColorSpan(Color.DKGRAY), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //deviceLines.setSpan(new BackgroundColorSpan(Color.BLACK), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                }
                // otherwise, we have a device to consider for alarm (check its dh and dk)
                else {

                    // device time DOY: is plain
                    deviceLines.append(DOY.format(device_time));

                    // device time HH:MM:SS is orange
                    start = deviceLines.length();
                    deviceLines.append(HHMMSS.format(device_time));
                    deviceLines.setSpan(new ForegroundColorSpan(0xFFCC5500), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // TODO for out of range dh (or dk), make red that value and device name too
                    // deltaHost span
                    //start = deviceLines.length();
                    float dh = device_dh;
                    if (dh < -999.9) {
                        dh = -999.9f;
                    } else if (dh > 999.9f) {
                        dh = 999.9f;
                    }
                    deviceLines.append(String.format(" %6.1f", dh));
                    //deviceLines.setSpan(new ForegroundColorSpan(0xFFCC5500), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //deviceLines.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // deltaKu span
                    //start = deviceLines.length();
                    float dk = device_dk;
                    boolean clipped = false;
                    if (dk < -999.9) {
                        dk = -999.9f; clipped = true;
                    } else if (dk > 999.9f) {
                        dk = 999.9f;  clipped = true;
                    }
                    deviceLines.append(String.format(" %6.1f", dk));
                    if (clipped) {
                        //deviceLines.setSpan(new ForegroundColorSpan(0x80ff0000), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        deviceLines.setSpan(new ForegroundColorSpan(Color.RED), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //deviceLines.setSpan(new ForegroundColorSpan(0xFFCC5500), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //deviceLines.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    // device name is clickable linked to WHAT?
                    deviceLines.append("  ");
                    //start = deviceLines.length();
                    deviceLines.append(padRight(device_name, 12));
                    //deviceLines.setSpan(new URLSpan("http://pims.grc.nasa.gov/plots/sams/121f03/121f03.jpg"), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // check dh
                    if (dh < 13.0f || dh > 17.0f) { countBadDeltaHosts++; }

                    // check dk
                    if (Math.abs(dk) > 3.0f) { countBadDeltaKus++; }

                }

                deviceLines.append("\n");

                // construct result line spannable string
                SpannableStringBuilder resultLine = new SpannableStringBuilder();
                int startResLine = resultLine.length();
                if (countBadDeltaHosts + countBadDeltaKus == 0) {
                    resultLine.append("All host deltas okay, and all Ku deltas okay.");
                    resultLine.setSpan(new ForegroundColorSpan(0xFFCC5500), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                else {
                    // TODO alarm somewhere in/after this else clause [keep track of 3 strikes before alarming?]
                    startResLine = resultLine.length();
                    if (countBadDeltaHosts > 0) {
                        resultLine.append(String.format("%d bad host deltas, ", countBadDeltaHosts));
                        resultLine.setSpan(new ForegroundColorSpan(Color.RED), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else {
                        resultLine.append("all host deltas are ok, ");
                        resultLine.setSpan(new ForegroundColorSpan(0xFFCC5500), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    startResLine = resultLine.length();
                    if (countBadDeltaKus > 0) {
                        resultLine.append(String.format("%d bad Ku deltas.", countBadDeltaKus));
                        resultLine.setSpan(new ForegroundColorSpan(Color.RED), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else {
                        resultLine.append("all Ku deltas are ok.");
                        resultLine.setSpan(new ForegroundColorSpan(0xFFCC5500), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                tvResult.setText(resultLine, TextView.BufferType.SPANNABLE);

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return SpannableString.valueOf(deviceLines);
        /*results.add(SpannableString.valueOf(resultLine));
        results.add(SpannableString.valueOf(deviceLines));
        return results;*/

    }

}
