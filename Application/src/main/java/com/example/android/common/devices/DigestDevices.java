package com.example.android.common.devices;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Range;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by pims on 7/3/15.
 */
public class DigestDevices {
    private final TreeMap<String,DeviceDeltas> mSortedMap;
    private final List<String> mIgnoreDevices;
    private SpannableString mDeviceLines = new SpannableString("");
    private SpannableString mResultOneLiner = new SpannableString("");
    private Range<Integer> mDeltaHostRange, mDeltaKuRange;
    //private SpannableStringBuilder mDeviceLines = new SpannableStringBuilder();
    private int mCountBadDeltaHosts;
    private int mCountBadDeltaKus;
    private Boolean mBadPhoneHostDelta = Boolean.TRUE;
    private int mResultState;

    private static final SimpleDateFormat DOY = new SimpleDateFormat("DDD:");
    private static final SimpleDateFormat HHMMSS = new SimpleDateFormat("HH:mm:ss");

    public DigestDevices(TreeMap<String, DeviceDeltas> sorted_map, List<String> ignore_devices,
                         Range<Integer> delta_host_range, Range<Integer> delta_ku_range) {
        mSortedMap = sorted_map;
        mIgnoreDevices = ignore_devices;
        mDeltaHostRange = delta_host_range;
        mDeltaKuRange = delta_ku_range;
        mCountBadDeltaKus = 0;
        mCountBadDeltaHosts = 0;
        mResultState = 0;
    }

    public DigestDevices(TreeMap<String, DeviceDeltas> sorted_map, List<String> ignore_devices) {
        mSortedMap = sorted_map;
        mIgnoreDevices = ignore_devices;
        mDeltaHostRange = Range.create(13, 17);
        mDeltaKuRange = Range.create(-3, 3);
        mCountBadDeltaKus = 0;
        mCountBadDeltaHosts = 0;
        mResultState = 0;
    }

    // setters
    public void setDeltaHostRange(Integer rmin, Integer rmax) { mDeltaHostRange = Range.create(rmin, rmax); }

    public void setDeltaKuRange(Integer rmin, Integer rmax) { mDeltaKuRange = Range.create(rmin, rmax); }

    private void setCountBadDeltaKus(int value) { mCountBadDeltaKus = value; }

    private void setCountBadDeltaHosts(int value) { mCountBadDeltaHosts = value; }

    private void setResultState(int value) { mResultState = value; }

    // getters
    public TreeMap<String, DeviceDeltas> getSortedMap() {
        return mSortedMap;
    }

    public List<String> getIgnoreDevices() {
        return mIgnoreDevices;
    }

    public Range<Integer> getDeltaHostRange() {
        return mDeltaHostRange;
    }

    public Range<Integer> getDeltaKuRange() {
        return mDeltaKuRange;
    }

    public SpannableString getDeviceLines() {
        return mDeviceLines;
    }

    public SpannableString getResultOneLiner() {
        return mResultOneLiner;
    }

    public int getCountBadDeltaHosts() {
        return mCountBadDeltaHosts;
    }

    public int getCountBadDeltaKus() {
        return mCountBadDeltaKus;
    }

    public Boolean isBadPhoneHostDelta() {
        return mBadPhoneHostDelta;
    }
    public int getResultState() {
        return mResultState;
    }

    private static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    // TODO set one of 3 result states: (1) GREEN okay, (2) YELLOW unknown, (3) RED bad
    // NEED NEW DELTA = (TextClockGMT - host) SHOULD BE LESS THAN [ 10 SEC? ]
    // if bad dPhone, then bad; maybe bigtime on butters host is down or butters crashed
    // any bad dKu is bad
    // all dHost bad is unknown (because ku is in that set), SO YELLOW COLOR & CHIME SOUND (no alarm)
    // otherwise (all other criteria above failed) go with okay so GREEN COLOR & CHIME SOUND
    public void processMap() {

        // get formatted spannable string for device lines & result one-line PLUS set result state
        try {

            // iterate over device lines to get deltas with formatting and bad host/ku delta counts
            mDeviceLines = getFormattedDeviceLines();

            // examine bad host/ku delta counts and such to determine result state & formatted one-liner
            mResultOneLiner = getFormattedResultOneLiner();

            // TODO main activity change sound to chime/alarm based on mResultState

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private SpannableString getFormattedDeviceLines() {

        // at this point, we should have sorted map, so iterate to build formatted spannable strings
        SpannableStringBuilder devLines = new SpannableStringBuilder();

        // counter for setting spans in growing-formatted spannable string
        int start;

        // FIXME seems like sometimes you use setter/getters and elsewhere just use member variable?
        // to start out, we assume all deltas are okay
        setCountBadDeltaHosts(0);
        setCountBadDeltaKus(0);

        // header line
        devLines.append("DOY:hh:mm:ss  dHost    dKu  device\n");
        devLines.append("----------------------------------\n");
        devLines.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        for (TreeMap.Entry<String, DeviceDeltas> entry: mSortedMap.entrySet()) {
            DeviceDeltas dev = entry.getValue();
            String device_name = dev.getDevice();
            Date device_time = dev.getTime();
            float device_dh = dev.getDeltaHost();
            float device_dk = dev.getDeltaKu();

            // FIXME when we get to host entry, entire line background gets distinct color
            // FIXME user config for dimmed devices

            // TODO make a home for snippet keeper for repeat character like this
            // the next line shows how to repeat char "-" 80 times
            //    Log.i("SEP", new String(new char[80]).replace("\0", "-"));

            // if host, then make this a distinctive line via bg color
            if (device_name.equals("host")) {

                start = devLines.length();

                devLines.append(DOY.format(device_time));
                devLines.append(HHMMSS.format(device_time));
                devLines.append(String.format(" %6.1f", device_dh));
                devLines.append(String.format(" %6.1f", device_dk));
                devLines.append("  ").append(device_name).append("      ");
                devLines.setSpan(new ForegroundColorSpan(Color.BLACK), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                devLines.setSpan(new BackgroundColorSpan(Color.WHITE), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
            // if this is device to ignore, dim this row
            else if (mIgnoreDevices.contains(device_name)) {

                start = devLines.length();

                devLines.append(DOY.format(device_time));
                devLines.append(HHMMSS.format(device_time));
                devLines.append("       ");
                devLines.append("       ");
                devLines.append("  ").append(device_name).append("      ");
                devLines.setSpan(new ForegroundColorSpan(Color.DKGRAY), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //devLines.setSpan(new BackgroundColorSpan(Color.BLACK), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
            // if this is phone device, then if dHost delta is too big, make it red
            else if (device_name.equals("phone")) {
                start = devLines.length();
                devLines.append(DOY.format(device_time));
                devLines.append(HHMMSS.format(device_time));
                devLines.append(String.format(" %6.1f", device_dh));
                devLines.append(String.format(" %6.1f", device_dk));
                devLines.append("  ").append(device_name).append("----< ");
                if (Math.abs(device_dh) < 121.0f) {
                    mBadPhoneHostDelta = Boolean.FALSE;
                } else if (Math.abs(device_dh) >= 121.0f) {
                    mBadPhoneHostDelta = Boolean.TRUE;
                    devLines.setSpan(new ForegroundColorSpan(Color.RED), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            // otherwise, we have a device to consider for sound notify (check its dh and dk)
            else {

                // device time DOY: is plain
                devLines.append(DOY.format(device_time));

                // device time HH:MM:SS is white
                start = devLines.length();
                devLines.append(HHMMSS.format(device_time));
                //devLines.setSpan(new ForegroundColorSpan(0xFFCC5500), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // TODO for out of range dh (or dk), make red that value and device name too
                // deltaHost span
                //start = devLines.length();
                float dh = device_dh;
                if (dh < -999.9) {
                    dh = -999.9f;
                } else if (dh > 999.9f) {
                    dh = 999.9f;
                }
                devLines.append(String.format(" %6.1f", dh));
                //devLines.setSpan(new ForegroundColorSpan(0xFFCC5500), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //devLines.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // deltaKu span
                //start = devLines.length();
                float dk = device_dk;
                boolean clipped = false;
                if (dk < -999.9) {
                    dk = -999.9f; clipped = true;
                } else if (dk > 999.9f) {
                    dk = 999.9f;  clipped = true;
                }
                devLines.append(String.format(" %6.1f", dk));
                if (clipped) {
                    //devLines.setSpan(new ForegroundColorSpan(0x80ff0000), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    devLines.setSpan(new ForegroundColorSpan(Color.RED), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //devLines.setSpan(new ForegroundColorSpan(0xFFCC5500), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //devLines.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // device name is clickable linked to WHAT?
                devLines.append("  ");
                //start = devLines.length();
                devLines.append(padRight(device_name, 12));
                //devLines.setSpan(new URLSpan("http://pims.grc.nasa.gov/plots/sams/121f03/121f03.jpg"), start, devLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // check dh
                if (dh < 13.0f || dh > 17.0f) { mCountBadDeltaHosts++; }

                // check dk
                if (Math.abs(dk) > 3.0f) { mCountBadDeltaKus++; }

            }

            devLines.append("\n");

        }
        return SpannableString.valueOf(devLines);
    }

    private SpannableString getFormattedResultOneLiner() {

        // init state to zero (i.e. unknown state)
        setResultState(0);

        // construct result one-liner spannable string
        SpannableStringBuilder resLine = new SpannableStringBuilder();
        int startResLine = resLine.length();
        int countBadDeltaHosts = getCountBadDeltaHosts();
        int countBadDeltaKus = getCountBadDeltaKus();
        if (countBadDeltaHosts + countBadDeltaKus == 0) {
            resLine.append("All dHost okay, and all dKu okay.");
            resLine.setSpan(new ForegroundColorSpan(Color.GREEN), startResLine, resLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            setResultState(1);
        }
        else if (isBadPhoneHostDelta()) {
            resLine.append("Bad phone host delta.");
            resLine.setSpan(new ForegroundColorSpan(Color.RED), startResLine, resLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            setResultState(-1);
        }
        else {
            startResLine = resLine.length();
            if (countBadDeltaHosts > 0) {
                resLine.append(String.format("%d bad dHost, ", countBadDeltaHosts));
                resLine.setSpan(new ForegroundColorSpan(Color.RED), startResLine, resLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else {
                resLine.append("all dHost okay, ");
                resLine.setSpan(new ForegroundColorSpan(Color.GREEN), startResLine, resLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            startResLine = resLine.length();
            if (countBadDeltaKus > 0) {
                resLine.append(String.format("%d bad dKu.", countBadDeltaKus));
                resLine.setSpan(new ForegroundColorSpan(Color.RED), startResLine, resLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                setResultState(-1);
            }
            else {
                resLine.append("all dKu okay.");
                resLine.setSpan(new ForegroundColorSpan(Color.GREEN), startResLine, resLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return SpannableString.valueOf(resLine);
    }

}