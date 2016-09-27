package com.ifms.tcc.marcusbruno.sisconfealuno.Utils;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * source http://stackoverflow.com/questions/33103798/how-to-get-wi-fi-mac-address-in-android-marshmallow
 */
public class MacAddress {

    public static String getValueMacAddres() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
}
