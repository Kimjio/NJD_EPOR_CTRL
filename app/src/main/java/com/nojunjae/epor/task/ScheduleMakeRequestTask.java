package com.nojunjae.epor.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.nojunjae.epor.eventR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.nojunjae.epor.Constants.TAG;

public class ScheduleMakeRequestTask extends AsyncTask<Void, Void, ArrayList<eventR>> {
    private Calendar mService;
    private Exception exception;

    private TaskListener taskListener;

    public ScheduleMakeRequestTask(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService =
                new Calendar.Builder(
                        transport, jsonFactory, credential)
                        .setApplicationName("joSSS")
                        .build();
    }

    @Override
    protected ArrayList<eventR> doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: Start!");
        try {
            return getDataFromApi();
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
            cancel(true);
            return null;
        }
    }

    private ArrayList<eventR> getDataFromApi() throws IOException {
        // List the next 10 events from the primary calendar.
        ArrayList<eventR> scheduleDataArrayList = new ArrayList<>();
        Events events =
                mService.events()
                        .list("primary")
                        .setMaxResults(100) // 100개 까지 표시
                        .setTimeMin(new DateTime(System.currentTimeMillis())) // 현재 시각 기준
                        // .setOrderBy("updated") //startTime : 시작 시간으로 정렬 -> 단일 정렬일 경우 사용가능
                        // updated : 마지막으로
                        // 수정된 시간으로
                        // .setSingleEvents(true) //단일 일정만
                        .execute();
        List<Event> items = events.getItems();

        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                start = event.getStart().getDate();
            }
            if (end == null) {
                // All-day events don't have end times, so just use
                // the end date.
                end = event.getEnd().getDate();
            }

            scheduleDataArrayList.add(
                    new eventR(
                            event.getSummary(),
                            event.getDescription(),
                            start.toString(),
                            end.toString()));
        }
        return scheduleDataArrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<eventR> scheduleDataArrayList) {
        if (scheduleDataArrayList == null) {
            scheduleDataArrayList = new ArrayList<>();
            scheduleDataArrayList.add(new eventR(null, null, null, null));
        } else if (scheduleDataArrayList.size() == 0) {
            scheduleDataArrayList.add(new eventR(null, null, null, null));
        }
        if (taskListener != null) taskListener.onTaskFinished(scheduleDataArrayList);
    }

    @Override
    protected void onCancelled() {
        if (taskListener != null) taskListener.onTaskCancelled(exception);
    }

    public ScheduleMakeRequestTask setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
        return this;
    }

    public interface TaskListener {
        void onTaskFinished(ArrayList<eventR> scheduleDataArrayList);

        void onTaskCancelled(Exception lastException);
    }
}
