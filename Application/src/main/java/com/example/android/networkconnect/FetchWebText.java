package com.example.android.networkconnect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by pims on 8/8/15.
 */
public class FetchWebText {

    public static void main(String[] args) throws IOException {
        List<String> list;
        list = loadFromNetwork("http://pims.grc.nasa.gov/plots/user/sams/status/sensortimes.txt");
        for (String s : list) {
            System.out.println(s);
        }
    }

    private static InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Start the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    public static List<String> loadFromNetwork(String urlString) throws IOException {
        List<String> list;
        InputStream stream = null;
        try {
            stream = downloadUrl(urlString);
            list = readIt(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return list;
    }

    private static List<String> readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        List<String> deviceLines = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            if(line.startsWith("begin") || line.startsWith("end") || line.startsWith("yyyy")) continue;
            if (line.endsWith("HOST")) {
                line += "<<<<<<";
            }
            deviceLines.add(line);
        }

        // create comparator for reverse order
        Comparator cmp = Collections.reverseOrder();
        Collections.sort(deviceLines, cmp);

        return deviceLines;
    }

}
