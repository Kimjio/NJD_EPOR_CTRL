package com.nojunjae.epor.task;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.nojunjae.epor.R;
import com.nojunjae.epor.data.YouTubeData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class YouTubeMakeRequestTask
        extends AsyncTask<String, Void, YouTubeData> { // 검색하는 class
    private YouTube mService;
    private YouTubePlayer youTubePlayer;

    public YouTubeMakeRequestTask(
            Context context, YouTubePlayer youTubePlayer, GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService =
                new YouTube.Builder(transport, jsonFactory, credential)
                        .setApplicationName(context.getString(R.string.app_name))
                        .build();
        this.youTubePlayer = youTubePlayer;
    }

    /**
     * Background task to call YouTube Data API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected YouTubeData doInBackground(String... params) { //
        try {
            return getDataFromApi(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            cancel(true);
            return null;
        }
    }

    private YouTubeData getDataFromApi(String musicName)
            throws IOException, JSONException { // 검색한 기록의 맨위 비디오의 ID를 반환
        YouTubeData result = new YouTubeData("", "");

        SearchListResponse searchRes =
                mService.search()
                        .list("snippet")
                        .setType("playlist, video") // 형식은 재생목록, 비디오 (채널은 제외)
                        .setMaxResults(1L) // 첫번째 줄
                        .setQ(musicName) // string으로 받아옴
                        .execute(); // 실행
        List<SearchResult> searches = searchRes.getItems();
        if (searches != null) { // 검색결과가 null값이 아닐 경우
            SearchResult searchResult = searches.get(0);
            JSONObject resultJSON = new JSONObject(searchResult.getId());
            try {
                result =
                        new YouTubeData(
                                resultJSON.getString("playlistId"),
                                resultJSON.getString(
                                        "kind")); // 첫번째줄 형식이 재생목록일 경우 result에 재생목록ID 값을 넣음
            } catch (JSONException e) {
                result =
                        new YouTubeData(
                                resultJSON.getString("videoId"),
                                resultJSON.getString(
                                        "kind")); // 첫번째줄 형식이 비디오일 경우 result에 비디오ID 값을 넣음
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(final YouTubeData output) { // 갑을 입력한 노래가 출력됨
        if (output == null) {
            // TODO Not Found
        } else {
            if (youTubePlayer != null) // 유튜브 playlist값이 널값이 아닐경우
                if (output.isPlayList())
                    youTubePlayer.loadPlaylist(output.getId()); // 고정된 값을 넣고 그값의 재생목록을 재생
                else youTubePlayer.loadVideo(output.getId()); // 고정된 값을 넣고 그값의 비디오를 재생
        }
    }
}