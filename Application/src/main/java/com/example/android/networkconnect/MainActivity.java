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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.common.devices.DeviceDeltas;
import com.example.android.common.devices.DigestDevices;

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
import java.util.TreeMap;

// FIXME for MainActivity can we go with extends Activity instead of FragmentActivity?
/**
 * Sample application demonstrating how to connect to the network and fetch raw
 * text from URL using AsyncTask to do the fetch on a background thread.
 *
 * To establish the network connection, it uses HttpURLConnection.
 */
public class MainActivity extends FragmentActivity {

    //public static final String TAG = "Network Connect";

    private TextView mTextViewDevices;
    private TextView mTextViewResult;

    private Uri mChimeSoundUri = Uri.parse("android.resource://com.example.android.networkconnect/" + R.raw.scandium_mp3);
    private Uri mAlarmSoundUri = Uri.parse("android.resource://com.example.android.networkconnect/" + R.raw.quindar_push_rel_zing_mp3);
    private Uri mSoundUri = mChimeSoundUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewDevices = (TextView) findViewById(R.id.devicesRichTextView);
        mTextViewResult = (TextView) findViewById(R.id.resultRichTextView);

        // make our ClickableSpans and URLSpans work
        mTextViewDevices.setMovementMethod(LinkMovementMethod.getInstance());

        // pump our styled text into the TextView
        mTextViewDevices.setText("initial onCreate text", TextView.BufferType.SPANNABLE);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/UbuntuMono-R.ttf");
        mTextViewDevices.setTypeface(font);

        // initialize TextView for result one-liner
        mTextViewResult.setText(R.string.welcome_message);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // When the user clicks Refresh, fetch the web text from URL
            case R.id.fetch_action:
                refresh();
                return true;

            // Clear is JUST A PLACEHOLDER action for now
            case R.id.clear_action:
                mTextViewResult.setText("This result line changes with Refresh AsyncTask.");
                mTextViewResult.setTextColor(Color.YELLOW);
                mSoundUri = this.mAlarmSoundUri;
                loopSound(1);

        }
        return false;
    }

    private void refresh(){
        // clear
        mTextViewResult.setText("Started refresh via DownloadTask, please wait...");

        // fetch
        new DownloadTask().execute("http://pims.grc.nasa.gov/plots/user/sams/status/sensortimes.txt");
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
        }).start(); // start thread to play sound
    }

    public void loopSound(int repeat) {
        loopSound(repeat, this.mSoundUri);
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
            // FIXME find out if we need this and why (not)
            super.onPostExecute(result);

            // show result in textView
            if (result == null) {
                mTextViewDevices.setText("Error in downloading. Please try again.");
            } else {
                updateResults(result);
            }

/*            Typeface font = Typeface.createFromAsset(getAssets(), "fonts/UbuntuMono-R.ttf");
            mTextViewDevices.setTypeface(font);*/

            // close progress dialog
            dialog.dismiss();

            // play notify sound [once?]
            loopSound(1);

        }
    }

    private void updateResults(String result) {
        // at this point, result is big string: several DeviceDeltas lines with newline characters
        TreeMap<String,DeviceDeltas> sorted_map = DeviceDeltas.getSortedMap(result);

        // FIXME with better way to handle this subset of few prefs
        List<String> ignore_devices = new ArrayList<String>();
/*            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean es03rtCheckBox = prefs.getBoolean("es03rtCheckBox", false);
            boolean es05rtCheckBox = prefs.getBoolean("es05rtCheckBox", false);
            boolean es06rtCheckBox = prefs.getBoolean("es06rtCheckBox", false);
            if (!es03rtCheckBox) { ignore_devices.add("es03rt"); }
            if (!es05rtCheckBox) { ignore_devices.add("es05rt"); }
            if (!es06rtCheckBox) { ignore_devices.add("es06rt"); }   */
        ignore_devices.add("es03rt");
        ignore_devices.add("es05rt");
        ignore_devices.add("es06rt");

        // FIXME use different input profile in DigestDevices to get ranges from prefs...
        // ...AFTER adding XML for ranges FIRST.

        // now we have sorted map, so iterate over devices to digest info
        DigestDevices digestDevices = new DigestDevices(sorted_map, ignore_devices);
        digestDevices.processMap();

        // FIXME suppress this [ what about final build suppress all Log? ]
        Log.i("DIGEST", "bad ho count = " + digestDevices.getCountBadDeltaHosts());
        Log.i("DIGEST", "bad ku count = " + digestDevices.getCountBadDeltaKus());
        Log.i("DIGEST", "ho range = " + digestDevices.getDeltaHostRange().toString());
        Log.i("DIGEST", "ku range = " + digestDevices.getDeltaKuRange().toString());

        // FIXME with both color result (for one-liner) and sound too
        // get result state
        int mResultValue = digestDevices.getResultState();
        if (mResultValue < 0) {
            mSoundUri = this.mAlarmSoundUri;
        }
        else {
            mSoundUri = this.mChimeSoundUri;
        }
        //getSupportActionBar().setTitle(GreenSpannableStringHere);  // spannable color change???

        // make our ClickableSpans and URLSpans work
        mTextViewDevices.setMovementMethod(LinkMovementMethod.getInstance());

        // populate top result (one-liner) text view with alarm results in spannable text form
        mTextViewResult.setText(digestDevices.getResultOneLiner(), TextView.BufferType.SPANNABLE);

        // populate devices text view with device times info in spannable form
        mTextViewDevices.setText(digestDevices.getDeviceLines(), TextView.BufferType.SPANNABLE);

    }

    /** Initiates the fetch operation. */
    private String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString);
            str = readIt(stream);
        } catch (Exception e) {
            Log.e("loadFromNetwork", "Error while downloading: " + e.toString());
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

    /** Reads an InputStream and converts it to a String.
     * @param stream InputStream containing HTML from targeted site.
     * @return String concatenated according to len parameter.
     * @throws java.io.IOException
     * @throws java.io.UnsupportedEncodingException
     */
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

}
