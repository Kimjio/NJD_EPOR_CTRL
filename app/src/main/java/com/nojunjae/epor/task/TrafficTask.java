package com.nojunjae.epor.task;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.nojunjae.epor.RoadData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.nojunjae.epor.Constants.ROAD_KEY;
import static com.nojunjae.epor.Constants.TAG;

public class TrafficTask extends AsyncTask<Void, Integer, String> {

    private RoadData[] rdData;
    private SparseArray<String> loadname = new SparseArray<>();
    private SparseArray<String> load = new SparseArray<>();
    private SparseIntArray avgspeed = new SparseIntArray();
    private SparseIntArray avg = new SparseIntArray();
    private int avgCnt = 0;
    private int cnt2 = 0;
    private SparseIntArray stat = new SparseIntArray();
    private StringBuilder roadSpeak = new StringBuilder();
    private Location location;
    private boolean isRoad = true;

    private TaskListener taskListener;

    public TrafficTask(Context context, Location location) {
        rdData = RoadData.getInstances(context.getResources());
        this.location = location;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            /*longitude = 128.612781f;
            latitude = 35.907858f;*/

            // runOnUiThread(() -> Toast.makeText(getApplicationContext(), "당신의 위치 - \n위도: " +
            // latitude + "\n경도: " + longitude, Toast.LENGTH_LONG).show());

            String xmlUrl =
                    "http://openapi.its.go.kr/api/NTrafficInfo?key="
                            + ROAD_KEY
                            + "&ReqType=2&MinX="
                            + (longitude - 0.05f)
                            + "&MaxX="
                            + (longitude + 0.05f)
                            + "&MinY="
                            + (latitude - 0.05f)
                            + "&MaxY="
                            + (latitude + 0.05f);
            Log.d("xml", xmlUrl);
            URL url = new URL(xmlUrl);
            URLConnection connection = url.openConnection();
            int cnt = 0;
            Document doc = parseXML(connection.getInputStream());
            NodeList checkNULL = doc.getElementsByTagName("response");
            Node Nul = checkNULL.item(0).getFirstChild();
            if (Nul.getTextContent().equals("NULL")) isRoad = false;
            NodeList descNodes = doc.getElementsByTagName("data");

            for (int i = 0; i < descNodes.getLength(); i++) {
                for (Node node = descNodes.item(i).getFirstChild();
                     node != null;
                     node = node.getNextSibling())
                    if (node.getNodeName().equals("roadnametext")) {
                        loadname.put(i, node.getTextContent());
                    }
            }
            for (int i = 0; i < descNodes.getLength(); i++) {
                Log.d("xml", "a");
                for (Node node = descNodes.item(i).getFirstChild();
                     node != null;
                     node = node.getNextSibling()) {
                    if (node.getNodeName().equals("roadnametext")) {
                        Log.d("loadname", node.getTextContent());
                        loadname.put(i, node.getTextContent());
                    }
                    if (node.getNodeName().equals("avgspeed")) {
                        avgspeed.put(i, Integer.parseInt(node.getTextContent()));
                    }
                }
            }
            for (int i = 0; i < descNodes.getLength(); i++) {
                if (loadname.get(i) != null)
                    if (i == 0) {
                        load.put(cnt, loadname.get(i));
                        avg.put(cnt2, avg.get(cnt2) + avgspeed.get(i));
                        cnt++;
                    } else if (loadname.get(i).equals(loadname.get(i - 1))) {
                        avg.put(cnt2, avg.get(cnt2) + avgspeed.get(i));
                        avgCnt++;
                    } else {
                        avg.put(cnt2, avg.get(cnt2) / avgCnt);
                        load.put(cnt, loadname.get(i));
                        avgCnt = 1;
                        cnt++;
                        cnt2++;
                    }
            }
            for (int i = 0; avg.get(i) != 0; i++) {
                for (RoadData aRdData : rdData) {
                    Log.d(TAG, load.get(i) + "/" + aRdData.getRoadName());
                    if (load.get(i).equals(aRdData.getRoadName()))
                        switch (aRdData.getRoadRank()) {
                            case 101:
                            case 102:
                                if (avg.get(i) < 60) stat.put(i, -1);
                                else if (avg.get(i) >= 80) stat.put(i, 1);
                                else stat.put(i, 0);
                                break;
                            case 103:
                            case 104:
                                if (avg.get(i) <= 20) stat.put(i, -1);
                                else if (avg.get(i) >= 40) stat.put(i, 1);
                                else stat.put(i, 0);
                                break;
                            case 105:
                            case 106:
                            case 107:
                            case 108:
                                if (avg.get(i) <= 20) stat.put(i, -1);
                                else if (avg.get(i) >= 40) stat.put(i, 1);
                                else stat.put(i, 0);
                                break;
                        }
                }
            }
            if (isRoad) {
                for (int i = 0; i < 2; i++) {
                    roadSpeak.append(load.get(i));
                    roadSpeak.append("는 현재,");
                    switch (stat.get(i)) {
                        case -1:
                            roadSpeak.append(" 밀립니다.");
                            break;
                        case 0:
                            roadSpeak.append(" 원활합니다.");
                            break;
                        case 1:
                            roadSpeak.append(" 쾌적합니다.");
                            break;
                    }
                }
            } else {
                return "주위에 지원하는 도로가 없습니다";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roadSpeak.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (taskListener != null) taskListener.onTaskFinished(s);
    }

    public TrafficTask setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
        return this;
    }

    private Document parseXML(InputStream stream)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory objDocumentBuilderFactory;
        DocumentBuilder objDocumentBuilder;
        Document doc;

        objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();

        doc = objDocumentBuilder.parse(stream);

        return doc;
    }

    public interface TaskListener {
        void onTaskFinished(String s);
    }
}
