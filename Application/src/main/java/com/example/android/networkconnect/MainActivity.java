/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.networkconnect;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.common.devices.DeviceDeltas;
import com.example.android.common.devices.DigestDevices;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sample application demonstrating how to connect to the network and fetch raw
 * HTML. It uses AsyncTask to do the fetch on a background thread. To establish
 * the network connection, it uses HttpURLConnection.
 *
 * This sample uses the logging framework to display log output in the log
 * fragment (LogFragment).
 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "Network Connect";

    // Reference to the fragment showing events, so we can clear it with a button as needed.
    private LogFragment mLogFragment;

    private Uri mUriChimeSound = Uri.parse("android.resource://com.example.android.networkconnect/" + R.raw.scandium_mp3);
    private Uri mUriAlarmSound = Uri.parse("android.resource://com.example.android.networkconnect/" + R.raw.quindar_push_rel_zing_mp3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        // Initialize text fragment that displays intro text.
        SimpleTextFragment introFragment = (SimpleTextFragment)
                    getSupportFragmentManager().findFragmentById(R.id.intro_fragment);
        introFragment.setText(R.string.welcome_message);
        introFragment.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f); // was 16.0f

        // Initialize the logging framework.
        initializeLogging();

    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // When the user clicks FETCH, fetch the web text from URL
            case R.id.fetch_action:
                refresh();
                return true;

            // Clear the log view fragment.
            case R.id.clear_action:
              mLogFragment.getLogView().setText("");
              loopSound(1, this.mUriAlarmSound);

        }
        return false;
    }

    private void refresh(){
        // clear
        mLogFragment.getLogView().setText("");

        // fetch
        new DownloadTask().execute("http://pims.grc.nasa.gov/plots/user/sams/status/sensortimes.txt");

        // play notify sound ONCE
        loopSound(1, this.mUriChimeSound);
    }

    public void loopSound(int repeat, Uri uri) {
        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), uri);
        final int count = repeat - 1;
        new Thread(new Runnable() {
            public void run() {
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    int n = 0;
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (n < count) {
                            mp.start();
                            n++;
                        }
                    }
                });
                mp.start();
            }
        }).start();
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog ;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // show progress dialog when downloading
            dialog = ProgressDialog.show(MainActivity.this, null, "Downloading...");

        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
              return getString(R.string.connection_error);
            }
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(String result) {
            // TODO change so result can be SpannableString for formatting

            // At this point, result is big string: several DeviceDeltas lines with newline chars
            TreeMap<String,DeviceDeltas> sorted_map = DeviceDeltas.getSortedMap(result);

            // FIXME with better way to handle these prefs
            List<String> ignore_devices = new ArrayList<String>();
/*            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean es03rtCheckBox = prefs.getBoolean("es03rtCheckBox", false);
            boolean es05rtCheckBox = prefs.getBoolean("es05rtCheckBox", false);
            boolean es06rtCheckBox = prefs.getBoolean("es06rtCheckBox", false);
            if (!es03rtCheckBox) { ignore_devices.add("es03rt"); }
            if (!es05rtCheckBox) { ignore_devices.add("es05rt"); }
            if (!es06rtCheckBox) { ignore_devices.add("es06rt"); }   */
            //ignore_devices.add("es03rt");
            ignore_devices.add("es05rt");
            ignore_devices.add("es06rt");

            // now we have sorted map, so iterate over devices to digest info
            DigestDevices digestDevices = new DigestDevices(sorted_map, ignore_devices);
            digestDevices.processMap();

            // Prepend debug info before big result str
            String debugstr = "";
            debugstr += "bad ho count = " + digestDevices.getCountBadDeltaHosts();
            debugstr += "\nbad ku count = " + digestDevices.getCountBadDeltaKus();
/*            debugstr += "\nho range = " + digestDevices.getDeltaHostRange().toString();
            debugstr += "\nku range = " + digestDevices.getDeltaKuRange().toString();*/

            Log.i(TAG, debugstr + "\n\n"  + result);

            //Log.i(TAG, debugstr + "\n\n" + digestDevices.getDeviceLines());

            // close progresses dialog
            dialog.dismiss();
        }
    }

    /** Initiates the fetch operation. */
    private String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString);
            //str = readIt(stream, 500);
            str = readIt(stream);
       } finally {
           if (stream != null) {
               stream.close();
            }
        }
        return str;
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets
     * an input stream.
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     * @throws java.io.IOException
     */
    private InputStream downloadUrl(String urlString) throws IOException {
        // BEGIN_INCLUDE(get_inputstream)
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
        // END_INCLUDE(get_inputstream)
    }

    /** Reads an InputStream and converts it to a String.
     * @param stream InputStream containing HTML from targeted site.
     * @param len Length of string that this method returns.
     * @return String concatenated according to len parameter.
     * @throws java.io.IOException
     * @throws java.io.UnsupportedEncodingException
     */
    private String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private String OLDreadIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            if(line.startsWith("begin") || line.startsWith("end") || line.startsWith("yyyy")) continue;
            total.append(line + "\n");
        }
        String result = total.toString();
        return result;
    }

    private String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        List<String> deviceLines = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            if(line.startsWith("begin") || line.startsWith("end") || line.startsWith("yyyy")  || line.charAt(4)=='-') continue;
            if (line.endsWith("HOST")) { line += "\n------------------------------"; }
            deviceLines.add(line);
        }

        // create comparator for reverse order
        Comparator cmp = Collections.reverseOrder();
        Collections.sort(deviceLines, cmp);

        return join(deviceLines, "\n");
    }

    public static String join(List<?> list, String delim) {
        int len = list.size();
        if (len == 0)
            return "";
        StringBuilder sb = new StringBuilder(list.get(0).toString());
        for (int i = 1; i < len; i++) {
            sb.append(delim);
            sb.append(list.get(i).toString());
        }
        return sb.toString();
    }

    /** Create a chain of targets that will receive log data */
    public void initializeLogging() {

        // Using Log, front-end to the logging chain, emulates
        // android.util.log method signatures.

        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        // A filter that strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        mLogFragment = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
        msgFilter.setNext(mLogFragment.getLogView());
    }
}
