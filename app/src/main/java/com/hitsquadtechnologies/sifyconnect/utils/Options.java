package com.hitsquadtechnologies.sifyconnect.utils;

import com.hitsquadtechnologies.sifyconnect.constants.CountryCode;
import com.hitsquadtechnologies.sifyconnect.constants.EnableDisable;
import com.hitsquadtechnologies.sifyconnect.constants.IPAddressType;
import com.hitsquadtechnologies.sifyconnect.constants.OperationalMode;
import com.hitsquadtechnologies.sifyconnect.constants.SpatialStream;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Options {
    public static final Options OPERATIONAL_MODE = new Options();
    public static final Options ENABLE_DISABLE = new Options();
    public static final Options IP_ADDRESS_TYPE = new Options();
    public static final Options SPATIAL_STREAM = new Options();
    public static final Options COUNTRY_CODE_OPTIONS = new Options();

    static {
        OPERATIONAL_MODE.add(OperationalMode._11A, "11A");
        OPERATIONAL_MODE.add(OperationalMode._11NA, "11NA");
        OPERATIONAL_MODE.add(OperationalMode._11AC, "11AC");

        ENABLE_DISABLE.add(EnableDisable.DISABLE, "Disable");
        ENABLE_DISABLE.add(EnableDisable.ENABLE, "Enable");

        IP_ADDRESS_TYPE.add(IPAddressType.STATIC, "Static");
        IP_ADDRESS_TYPE.add(IPAddressType.DYNAMIC, "Dynamic");

        SPATIAL_STREAM.add(SpatialStream.SINGLE, "Single");
        SPATIAL_STREAM.add(SpatialStream.DUAL, "Dual");

        COUNTRY_CODE_OPTIONS.add(CountryCode.INDIA_UL, "INDIA_UL");
        COUNTRY_CODE_OPTIONS.add(CountryCode.INDIA_L, "INDIA_L");
        COUNTRY_CODE_OPTIONS.add(CountryCode.RUSSIA, "RUSSIA");

    }

    private List<UniquePair> statuses = new LinkedList<>();

    public Options() {
    }

    public void add(int key, String name) {
        statuses.add(new UniquePair(key, name));
    }

    public String getValueByKey(int key) {
        for (UniquePair status: statuses) {
            if(status.key == key) {
                return status.value;
            }
        }
        return "";
    }

    public int getKeyByValue(String value) {
        for (UniquePair status: statuses) {
            if(status.value.equals(value)) {
                return status.key;
            }
        }
        return Integer.MIN_VALUE;
    }

    public int findPositionByKey(int key) {
        return findPositionByKey(key, 0);
    }

    public int findPositionByKey(int key, int defaultValue) {
        int i = 0;
        for (UniquePair status: statuses) {
            if(status.key == key) {
                return i;
            } else {
                i++;
            }
        }
        return defaultValue;
    }

    public Iterator<UniquePair> iterator() {
        return statuses.iterator();
    }

    public String[] values() {
        List<String> values = new LinkedList<>();
        for (int i = 0; i < statuses.size(); i++) {
            values.add(statuses.get(i).value);
        }
        return values.toArray(new String[values.size()]);
    }
}

class UniquePair {

    public final Integer key;
    public final String value;

    UniquePair(Integer key, String value) {
        this.key = key;
        this.value = value;
    }
}
