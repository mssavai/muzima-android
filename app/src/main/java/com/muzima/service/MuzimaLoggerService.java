package com.muzima.service;

import android.os.AsyncTask;
import android.os.Build;
import com.muzima.api.context.Context;
import com.muzima.util.MuzimaLogger;

import java.util.UUID;

public class MuzimaLoggerService {
    private static String pseudoDeviceId = null;

    public static void log(final Context context, final String tag, final String details){
        new AsyncTask<Void,Void,Void>(){
            protected Void doInBackground(Void... voids) {
                String deviceId = getPseudoDeviceId();
                MuzimaLogger.log(context, tag, details, deviceId);
                return null;
            }
        }.execute();
    }



    private static String getPseudoDeviceId() {
        if(pseudoDeviceId == null){
            pseudoDeviceId = generatePseudoDeviceId();
        }
        return pseudoDeviceId;
    }

    /**
     * Return pseudo unique ID
     * @return ID
     */
    private static String generatePseudoDeviceId() {
        // If all else fails, if the user does have lower than API 9 (lower
        // than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
        // returns 'null', then simply the ID returned will be solely based
        // off their Android device information. This is where the collisions
        // can happen.
        // Thanks http://www.pocketmagic.net/?p=1662!
        // Try not to use DISPLAY, HOST or ID - these items could change.
        // If there are collisions, there will be overlapping data
        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10)
                + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10)
                + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

        // Thanks to @Roman SL!
        // https://stackoverflow.com/a/4789483/950427
        // Only devices with API >= 9 have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If a user upgrades software or roots their device, there will be a duplicate entry
        String serial = null;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();

            // Go ahead and return the serial for api => 9
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            // String needs to be initialized
            serial = "serial"; // some value
        }

        // Thanks @Joe!
        // https://stackoverflow.com/a/2853253/950427
        // Finally, combine the values we have found by using the UUID class to create a unique identifier
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

}
