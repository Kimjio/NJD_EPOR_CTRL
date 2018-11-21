package com.nojunjae.epor;

import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class RoadData {
    private static RoadData[] instances;
    private String linkId;
    private String fNode;
    private String fNodeName;
    private String tNode;
    private String tNodeName;
    private int roadRank;
    private String roadName;
    private int len;
    private String organ;

    private RoadData(
            String linkId,
            String fNode,
            String fNodeName,
            String tNode,
            String tNodeName,
            int roadRank,
            String roadName,
            int len,
            String organ) {
        this.linkId = linkId;
        this.fNode = fNode;
        this.fNodeName = fNodeName;
        this.tNode = tNode;
        this.tNodeName = tNodeName;
        this.roadRank = roadRank;
        this.roadName = roadName;
        this.len = len;
        this.organ = organ;
    }

    private static RoadData[] init(Resources resources) {
        try {
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(resources.openRawResource(R.raw.road_data)));
            List<RoadData> roadDatas = new LinkedList<>();

            String data = reader.readLine();
            while (data != null) {
                String[] datas = data.split("\t");

                roadDatas.add(
                        new RoadData(
                                datas[0],
                                datas[1],
                                datas[2],
                                datas[3],
                                datas[4],
                                Integer.parseInt(datas[5]),
                                datas[6],
                                Integer.parseInt(datas[7]),
                                datas[8]));
                data = reader.readLine();
            }

            Log.d("RoadDatas", "END");
            instances = roadDatas.toArray(new RoadData[0]);
            reader.close();
        } catch (IOException e) {
            Log.w("RoadDatas", e);
            e.printStackTrace();
        }

        return instances;
    }

    public static RoadData[] getInstances(Resources resources) {

        return init(resources);
    }

    public String getLinkId() {
        return linkId;
    }

    public String getRoadName() {
        return roadName;
    }

    public String getOrgan() {
        return organ;
    }

    public int getLen() {
        return len;
    }

    public int getRoadRank() {
        return roadRank;
    }

    public String getFNodeName() {
        return fNodeName;
    }

    public String getTNodeName() {
        return tNodeName;
    }

    public String getTNode() {
        return tNode;
    }

    public String getFNode() {
        return fNode;
    }
}
