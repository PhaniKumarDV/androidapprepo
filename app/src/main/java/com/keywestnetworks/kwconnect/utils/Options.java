package com.keywestnetworks.kwconnect.utils;

import com.keywestnetworks.kwconnect.constants.CountryCode;
import com.keywestnetworks.kwconnect.constants.DeviceMode;
import com.keywestnetworks.kwconnect.constants.EnableDisable;
import com.keywestnetworks.kwconnect.constants.IPAddressType;
import com.keywestnetworks.kwconnect.constants.OperationalMode;
import com.keywestnetworks.kwconnect.constants.SVlanEtherType;
import com.keywestnetworks.kwconnect.constants.SpatialStream;
import com.keywestnetworks.kwconnect.constants.TrunkOpt;
import com.keywestnetworks.kwconnect.constants.VlanMode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Options {
    public static final Options DEV_MODE = new Options();
    public static final Options OPERATIONAL_MODE = new Options();
    public static final Options ENABLE_DISABLE = new Options();
    public static final Options IP_ADDRESS_TYPE = new Options();
    public static final Options SPATIAL_STREAM = new Options();
    public static final Options COUNTRY_CODE_OPTIONS = new Options();
    public static final Options VLAN_MODE = new Options();
    public static final Options TRUNK_OPT = new Options();
    public static final Options SVLAN_ETHERTYPE = new Options();

    static {
        DEV_MODE.add(DeviceMode.AP,"AP");
        DEV_MODE.add(DeviceMode.SU, "SU");

        OPERATIONAL_MODE.add(OperationalMode._11A, "11A");
        OPERATIONAL_MODE.add(OperationalMode._11NA, "11NA");
        OPERATIONAL_MODE.add(OperationalMode._11AC, "11AC");

        ENABLE_DISABLE.add(EnableDisable.DISABLE, "Disable");
        ENABLE_DISABLE.add(EnableDisable.ENABLE, "Enable");

        IP_ADDRESS_TYPE.add(IPAddressType.STATIC, "Static");
        IP_ADDRESS_TYPE.add(IPAddressType.DYNAMIC, "Dynamic");

        SPATIAL_STREAM.add(SpatialStream.SINGLE, "Single");
        SPATIAL_STREAM.add(SpatialStream.DUAL, "Dual");
        SPATIAL_STREAM.add(SpatialStream.AUTO, "Auto");

        COUNTRY_CODE_OPTIONS.add(CountryCode.INDIA1, "INDIA");
        COUNTRY_CODE_OPTIONS.add(CountryCode.INDIA2, "5GHZ");

        VLAN_MODE.add(VlanMode.TRANSPARENT,  "Transparent");
        VLAN_MODE.add(VlanMode.ACCESS, "Access");
        /*VLAN_MODE.add(VlanMode.TRUNK,"Trunk");
        VLAN_MODE.add(VlanMode.QinQ,"QinQ");*/

        TRUNK_OPT.add(TrunkOpt.LIST, "LIST");
        TRUNK_OPT.add(TrunkOpt.ALL, "ALL");

        SVLAN_ETHERTYPE.add(SVlanEtherType.Ox8100,"0x8100");
        SVLAN_ETHERTYPE.add(SVlanEtherType.Ox88a8,"0x88a8");
        SVLAN_ETHERTYPE.add(SVlanEtherType.Ox9100,"0x9100");
        SVLAN_ETHERTYPE.add(SVlanEtherType.Ox9200,"0x9200");
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
