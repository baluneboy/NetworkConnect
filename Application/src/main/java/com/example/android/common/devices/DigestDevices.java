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
    }

    public DigestDevices(TreeMap<String, DeviceDeltas> sorted_map, List<String> ignore_devices) {
        mSortedMap = sorted_map;
        mIgnoreDevices = ignore_devices;
        mDeltaHostRange = Range.create(13, 17);
        mDeltaKuRange = Range.create(-3, 3);
        mCountBadDeltaKus = 0;
        mCountBadDeltaHosts = 0;
    }

    // setters
    public void setDeltaHostRange(Integer rmin, Integer rmax) { mDeltaHostRange = Range.create(rmin, rmax); }

    public void setDeltaKuRange(Integer rmin, Integer rmax) { mDeltaKuRange = Range.create(rmin, rmax); }

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

    private static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public void processMap() {

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

            for (TreeMap.Entry<String, DeviceDeltas> entry: mSortedMap.entrySet()) {
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
                else if (mIgnoreDevices.contains(device_name)) {

                    start = deviceLines.length();

                    deviceLines.append(DOY.format(device_time));
                    deviceLines.append(HHMMSS.format(device_time));
                    deviceLines.append("       ");
                    deviceLines.append("       ");
                    deviceLines.append("  " + device_name + "      ");
                    deviceLines.setSpan(new ForegroundColorSpan(Color.DKGRAY), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //deviceLines.setSpan(new BackgroundColorSpan(Color.BLACK), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                }
                // otherwise, we have a device to consider for sound notify (check its dh and dk)
                else {

                    // device time DOY: is plain
                    deviceLines.append(DOY.format(device_time));

                    // device time HH:MM:SS is white
                    start = deviceLines.length();
                    deviceLines.append(HHMMSS.format(device_time));
                    //deviceLines.setSpan(new ForegroundColorSpan(0xFFCC5500), start, deviceLines.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

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

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mDeviceLines = SpannableString.valueOf(deviceLines);

        // construct result line spannable string
        SpannableStringBuilder resultLine = new SpannableStringBuilder();
        int startResLine = resultLine.length();
        if (countBadDeltaHosts + countBadDeltaKus == 0) {
            resultLine.append("All dHost okay, and all dKu okay.");
            resultLine.setSpan(new ForegroundColorSpan(0xFFCC5500), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else {
            // TODO alarm somewhere in/after this else clause [keep track of 3 strikes before alarming?]
            startResLine = resultLine.length();
            if (countBadDeltaHosts > 0) {
                resultLine.append(String.format("%d bad dHost, ", countBadDeltaHosts));
                resultLine.setSpan(new ForegroundColorSpan(Color.RED), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else {
                resultLine.append("all dHost okay, ");
                resultLine.setSpan(new ForegroundColorSpan(0xFFCC5500), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            startResLine = resultLine.length();
            if (countBadDeltaKus > 0) {
                resultLine.append(String.format("%d bad dKu.", countBadDeltaKus));
                resultLine.setSpan(new ForegroundColorSpan(Color.RED), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else {
                resultLine.append("all dKu okay.");
                resultLine.setSpan(new ForegroundColorSpan(0xFFCC5500), startResLine, resultLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        mResultOneLiner = SpannableString.valueOf(resultLine);

    }


}