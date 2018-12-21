package com.hitsquadtechnologies.sifyconnect.Adapters;

import com.jjoe64.graphview.series.DataPoint;

import java.util.LinkedList;
import java.util.List;

public class SharedLinkSpeedGraphData {

    public static final SharedLinkSpeedGraphData INSTANCE = new SharedLinkSpeedGraphData();
    private static final int MAX_DATA_POINTS = 10;

    private List<Integer> localSpeedData = new LinkedList<>();
    private List<Integer> remoteSpeedData = new LinkedList<>();

    private SharedLinkSpeedGraphData() {}

    public void add(int localSpeed, int remoteSpeed) {
        addData(this.localSpeedData, localSpeed);
        addData(this.remoteSpeedData, remoteSpeed);
    }

    public int max() {
        return Math.max(max(this.localSpeedData), max(this.remoteSpeedData));
    }

    private int max(List<Integer> list) {
        int max = 0;
        for (int i : list) {
            max = max > i ? max : i;
        }
        return max;
    }

    public DataPoint[] getLocalData() {
        return toDataPointArray(this.localSpeedData);
    }

    public DataPoint[] getRemoteData() {
        return toDataPointArray(this.remoteSpeedData);
    }

    private DataPoint[] toDataPointArray(List<Integer> seriesData) {
        int len = seriesData.size();
        DataPoint[] dataPoints = new DataPoint[MAX_DATA_POINTS];
        for (int i = 0; i < MAX_DATA_POINTS; i++) {
            int v = 0;
            if ( i > (MAX_DATA_POINTS - len)) {
                v = seriesData.get(i - (MAX_DATA_POINTS - len));
            }
            dataPoints[i] = new DataPoint(i, v);
        }
        return dataPoints;
    }

    private List<Integer> addData(List<Integer> seriesData, int value) {
        if (seriesData.size() > MAX_DATA_POINTS) {
            seriesData = seriesData.subList(seriesData.size() - MAX_DATA_POINTS, seriesData.size());
        }
        seriesData.add(value);
        return seriesData;
    }

    private String seriesData(List<Integer> seriesData) {
        String str = "";
        for (int i = 0; i < seriesData.size(); i++) {
            str += seriesData.get(i) + ",";
        }
        return  str;
    }

    private String seriesData(DataPoint[] dataPoints) {
        String str = "";
        for (int i = 0; i < dataPoints.length; i++) {
            str += dataPoints[i].getY() + ",";
        }
        return  str;
    }
}
