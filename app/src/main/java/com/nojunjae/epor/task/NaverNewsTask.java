package com.nojunjae.epor.task;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by gsy01 on 2018-04-12.
 */
public class NaverNewsTask extends AsyncTask<Void, Integer, String> {

    private static final String URL = "http://www.naver.com";
    private static final String CLIENT_ID = "y2qTHIfiZoE_fZ8VbHe1";
    private static final String CLIENT_SECRET = "QAm1K317rN";
    private static int rank = 0;
    private TaskListener taskListener;

    @Override
    protected String doInBackground(Void... voids) {
        try {
            Document document = Jsoup.connect(URL).maxBodySize(0).get();

            Element element = document.select(".ah_k").get(rank);
            Log.d(getClass().getSimpleName(), "doInBackground: " + element.text());
            String apiURL =
                    "https://openapi.naver.com/v1/search/news.xml?query="
                            + URLEncoder.encode(element.text(), "UTF-8")
                            + "&start=1&display=10";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
            con.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == HttpURLConnection.HTTP_OK)
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            else br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) response.append(inputLine);
            br.close();
            con.disconnect();

            String newsLink = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Log.d(getClass().getSimpleName(), "doInBackground1: " + response.toString());
            org.w3c.dom.Document oDocument =
                    builder.parse(new InputSource(new StringReader(response.toString())));
            NodeList nodeLists = oDocument.getElementsByTagName("link");
            for (int i = 1; i < nodeLists.getLength(); i += 2) {
                Node node = nodeLists.item(i);
                Log.d(getClass().getSimpleName(), "doInBackground2: " + node.getTextContent());
                if (node.getTextContent().contains("//news.naver.com"))
                    newsLink = node.getTextContent();
            }
            if (newsLink == null) {
                rank++;
                return this.doInBackground();
            }
            document = Jsoup.connect(newsLink).maxBodySize(0).get();
            Log.d(getClass().getSimpleName(), "doInBackgroundasdasd: " + newsLink);
            Log.d(getClass().getSimpleName(), "doInBackground3 : " + document.toString());

            element = document.select("div#articleBodyContents").get(0);
            return element.text();
        } catch (IOException
                | SAXException
                | ParserConfigurationException
                | IllegalArgumentException
                | IndexOutOfBoundsException e) {
            e.printStackTrace();
            rank++;
            if (rank >= 10) {
                rank = 0;
                return null;
            }
            return this.doInBackground();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (taskListener != null) taskListener.onTaskFinished(s);
    }

    public NaverNewsTask setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
        return this;
    }

    public interface TaskListener {
        void onTaskFinished(String s);
    }
}
