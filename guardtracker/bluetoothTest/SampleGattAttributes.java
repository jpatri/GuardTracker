/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patri.guardtracker.bluetoothTest;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String GUARD_TRACKER_DEBUG_SERVICE = "d5e330ff-8924-4c0d-bbcb-62a49fbf47da";
    public static String GUARD_TRACKER_DEBUG_WAKE_STATE_CHARC = "6821ea84-c199-4c8f-8ab0-c4a7d87202de";
    public static String GUARD_TRACKER_DEBUG_ERRNO_CHARC = "bcda445a-cc16-44fe-a451-f011fe6385cb";
    public static String GUARD_TRACKER_DATA_CHANNEL_SERVICE = "d74f127b-b0f4-4868-9026-30ac3966df94";
    public static String GUARD_TRACKER_DATA_CHARC = "69893418-0f97-4860-9a24-f5da189613f1";

    static {
        // Sample Services.
        attributes.put(GUARD_TRACKER_DEBUG_SERVICE, "GuardTracker debug service");
        attributes.put(GUARD_TRACKER_DATA_CHANNEL_SERVICE, "GuardTracker data channel service");
        //attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(GUARD_TRACKER_DEBUG_WAKE_STATE_CHARC, "GuardTracker wake state");
        attributes.put(GUARD_TRACKER_DEBUG_ERRNO_CHARC, "GuardTracker errno");
        attributes.put(GUARD_TRACKER_DATA_CHARC, "GuardTracker data");
        //attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
