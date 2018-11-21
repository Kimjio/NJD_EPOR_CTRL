package com.nojunjae.epor.activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.youtube.YouTubeScopes;
import com.nojunjae.epor.DeviceListFragment;
import com.nojunjae.epor.R;
import com.nojunjae.epor.RemoteFragment;
import com.nojunjae.epor.SimpleLocationListener;
import com.nojunjae.epor.XRDuino;
import com.nojunjae.epor.eventR;
import com.nojunjae.epor.task.NaverNewsTask;
import com.nojunjae.epor.task.ScheduleMakeRequestTask;
import com.nojunjae.epor.task.TrafficTask;
import com.nojunjae.epor.task.YouTubeMakeRequestTask;
import com.nojunjae.epor.weatherR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.os.SystemClock.sleep;
import static com.nojunjae.epor.Constants.TAG;
import static com.nojunjae.epor.Constants.YOUTUBE_PLAYER_API_KEY;

public class MainActivity extends YouTubeBaseActivity implements OnInitListener {

    public static final String BEAT_BOX =
            "Uh, sama lamaa duma lamaa you assuming I'm a human What I gotta do to get it through to you I'm superhuman Innovative and I'm made of rubber So that anything you saying ricocheting off of me and it'll glue to you I'm never stating, more than never demonstrating How to give a motherfuckin' audience a feeling like it's levitating Never fading, and I know that the haters are forever waiting For the day that they can say I fell off, they'd be celebrating Cause I know the way to get 'em motivatedI make elevating music, you make elevator music";
    public static final String JANG_BEAT =
            "췍        췍       암더코리안 탑클래스 힙합모범 노블레스 패뷸러스 터뷸런스 고져스 벗 덴져러스 난 비트를 비틀어 재껴버리는 서브미션 챔피온 똑바로 눈떠라 떠나면 잡지못할 버스 불을 지폈어 암더코리안 탑클래스 힙합모범 노블레스 패뷸러스 터뷸런스 고져스 벗 덴져러스 난 비트를 비틀어 재껴버리는 서브미션 챔피온 똑바로 눈떠라 떠나면 잡지못할 버스 불을 지폈어";

    private static final int RESULT_SPEECH = 0; // REQUEST_CODE로 쓰임
    private static final int ACCOUNT_PICKER = 1;
    private static final int AUTHORIZATION = 2;
    private static final int GOOGLE_PLAY_SERVICES = 3;
    private static final int PERMISSIONS = 4;
    private static final int REQUEST_SELECT_DEVICE = 5;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {
            YouTubeScopes.YOUTUBE_READONLY, CalendarScopes.CALENDAR_READONLY
    };

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    public TextToSpeech tts;
    int lMotor = 0, rMotor = 0;
    int head = 90, lArm = 90, rArm = 90;
    GoogleAccountCredential mCredential;


    Button button_r;
    Button button_g;
    Button button_b;
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer;
    private Intent intent;
    private XRDuino xrDuino;
    private Thread danceThread = new Thread(() -> {
        while (true) {
            lArm = 30;
            rArm = 30;
            lMotor = 200;
            rMotor = -200;
            xrDuino.setMotorSpeed(rMotor, lMotor);
            xrDuino.setServoAngle(head, rArm, lArm);
            sleep(2000);

            lArm = 160;
            rArm = 160;
            lMotor = -200;
            rMotor = 200;
            xrDuino.setMotorSpeed(rMotor, lMotor);
            xrDuino.setServoAngle(head, rArm, lArm);
            sleep(2000);
        }
    });

    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;

    private DeviceListFragment deviceFragment = new DeviceListFragment();
    private RemoteFragment remoteFragment = new RemoteFragment();
    private boolean isPermission;

    private void initAPI() { // API 에서 부터의 결과 네트워크 확인
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            // TODO Offline
        } else {
            new YouTubeMakeRequestTask(this, youTubePlayer, mCredential).execute();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        youTubePlayerView.onConfigurationChanged(newConfig);
    }

    private boolean isDeviceOnline() { // Offline 인지 확인
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_r = findViewById(R.id.button_r);
        button_g = findViewById(R.id.button_g);
        button_b = findViewById(R.id.button_b);

        button_r.setOnClickListener(v -> xrDuino.setRGBLed(255, 0, 0));

        button_g.setOnClickListener(v -> xrDuino.setRGBLed(0, 255, 0));

        button_b.setOnClickListener(v -> xrDuino.setRGBLed(0, 0, 255));

        xrDuino = XRDuino.getInstance(this);
        remoteFragment.setXrDuino(xrDuino);
        checkPermission();

        mCredential =
                GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff());

        tts = new TextToSpeech(this, this);

        FloatingActionButton mic = findViewById(R.id.button_mic);
        mic.setOnClickListener(
                view -> {
                    if (view.getId() == R.id.button_mic) {
                        if (tts.isSpeaking()) tts.stop();

                        // Intent 부분
                        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // Intent 생성
                        intent.putExtra(
                                RecognizerIntent.EXTRA_CALLING_PACKAGE,
                                getPackageName()); // 호출한 패키지
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 인식할 언어를 설정한다.
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말을 하시오"); // 유저에게 보여줄 문자

                        try {
                            startActivityForResult(intent, RESULT_SPEECH);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    R.string.stt_not_found,
                                    Toast.LENGTH_SHORT)
                                    .show();
                            e.getStackTrace();
                        }
                    }
                });

        youTubePlayerView = findViewById(R.id.youtube_view);

        youTubePlayerView.initialize(
                YOUTUBE_PLAYER_API_KEY,
                new YouTubePlayer.OnInitializedListener() { // api key 확인
                    @Override
                    public void onInitializationSuccess(
                            YouTubePlayer.Provider provider, YouTubePlayer yPlayer, boolean b) {
                        yPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
                        yPlayer.setShowFullscreenButton(false);
                        youTubePlayer = yPlayer;
                    }

                    @Override
                    public void onInitializationFailure(
                            YouTubePlayer.Provider provider,
                            YouTubeInitializationResult youTubeInitializationResult) {
                    }
                });

        initAPI();
    }

    public void onClick(View v) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (v.getId()) {
            case R.id.button_bluetooth:
                if (deviceFragment.isVisible()) fragmentTransaction.remove(deviceFragment);
                else fragmentTransaction.replace(R.id.fragment, deviceFragment);
                break;
            case R.id.button_remote:
                if (remoteFragment.isVisible()) fragmentTransaction.remove(remoteFragment);
                else fragmentTransaction.replace(R.id.fragment, remoteFragment);
                break;
        }
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                    }
                }
                break;
                /*case AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                }
                break;*/
            case REQUEST_SELECT_DEVICE:
                xrDuino.consumeRequestDeviceSelect(resultCode, data);
                return;
        }

        if (resultCode == RESULT_OK && requestCode == RESULT_SPEECH) {

            // data.getString...() 호출로 음성 인식 결과를 배열리스트로
            ArrayList<String> sttResult =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // 음성과 가장 유사한 단어부터 시작되는 0번째 문자열을 저장
            speechResult(sttResult.get(0));
        }
    }

    public void speechResult(String result) {
        if (result.contains("일정")
                || result.contains("스케쥴")
                || result.contains("스케줄")
                || result.contains("스케듈")
                || result.contains("할일")
                || result.contains("할것")) {
            final ScheduleMakeRequestTask scheduleMakeRequestTask =
                    new ScheduleMakeRequestTask(mCredential);
            scheduleMakeRequestTask.setTaskListener(
                    new ScheduleMakeRequestTask.TaskListener() {
                        @Override
                        public void onTaskFinished(ArrayList<eventR> scheduleDataArrayList) {
                            eventR result = scheduleDataArrayList.get(0);
                            tts.speak(result.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                            if (result.isEmpty())
                                for (int i = 0; i < 4; i++) {
                                    xrDuino.setServoAngle(30, 90, 90);
                                    sleep(500);
                                    xrDuino.setServoAngle(150, 90, 90);
                                    sleep(500);
                                }
                            else
                                for (int i = 0; i < 4; i++) {
                                    xrDuino.setServoAngle(90, 160, 20);
                                    sleep(500);
                                    xrDuino.setServoAngle(90, 90, 90);
                                    sleep(500);
                                    xrDuino.setServoAngle(90, 20, 160);
                                    sleep(500);
                                }
                            xrDuino.setServoAngle(90, 90, 90);
                        }

                        @Override
                        public void onTaskCancelled(Exception exception) {
                            if (exception instanceof UserRecoverableAuthIOException) {
                                startActivityForResult(
                                        ((UserRecoverableAuthIOException) exception)
                                                .getIntent(),
                                        AUTHORIZATION);
                                scheduleMakeRequestTask.execute();
                            } else
                                tts.speak(
                                        "일정을 가져올 수 없습니다.",
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        null);
                        }
                    });
            scheduleMakeRequestTask.execute();
        } else if (result.contains("날씨") || result.contains("온도")) {
            getWeather();
        } else if (result.contains("뉴스")
                || result.contains("기사")
                || result.contains("기삿거리")
                || result.contains("신문")) {
            new NaverNewsTask()
                    .setTaskListener(
                            s -> {
                                tts.setPitch(0.8f);
                                tts.speak(
                                        "뉴스를 불러오고 있는 중입니다. 기다려주십시오.",
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        null);
                                if (TextUtils.isEmpty(s)) s = "뉴스가 없습니다.";
                                tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
                                xrDuino.setServoAngle(90, 90, 0);
                                xrDuino.setRGBLed(0, 153, 255);
                            })
                    .execute();
        } else if (result.contains("교통상황") || result.contains("교통정보")) {
            try {
                new TrafficTask(this, getLocation()).setTaskListener(s -> {
                    tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
                    if (s.contains("밀립니다")) xrDuino.setMotorSpeed(100, -100);
                    else if (s.contains("원활합니다")) xrDuino.setMotorSpeed(150, -150);
                    else if (s.contains("쾌적합니다")) xrDuino.setMotorSpeed(200, -200);
                }).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (result.contains("틀어") || result.contains("재생")) {
            YouTubeMakeRequestTask youTubeMakeRequestTask =
                    new YouTubeMakeRequestTask(this, youTubePlayer, mCredential);
            String musicName = getMusicName(result);
            youTubeMakeRequestTask.execute(musicName);
            tts.setPitch(1.2f);
            tts.speak(musicName + ", 에뽀가 부릅니다.", TextToSpeech.QUEUE_FLUSH, null, null);
            tts.setPitch(1.0f);
        } else if (result.contains("앞으로")) {
            lMotor = 170;
            rMotor = 170;
            xrDuino.setMotorSpeed(lMotor, rMotor);
        } else if (result.contains("뒤로")) {
            lMotor = -240;
            rMotor = -240;
            xrDuino.setMotorSpeed(lMotor, rMotor);
        } else if (result.contains("왼쪽으로")) {
            xrDuino.setMotorSpeed(240, -240);
            sleep(500);
            xrDuino.setMotorSpeed(lMotor, rMotor);
        } else if (result.contains("오른쪽으로")) {
            xrDuino.setMotorSpeed(-240, 240);
            sleep(500);
            xrDuino.setMotorSpeed(lMotor, rMotor);
        } else if (result.contains("왼손")) {
            lArm = 30;
            xrDuino.setServoAngle(head, rArm, lArm);
        } else if (result.contains("오른손")) {
            rArm = 150;
            xrDuino.setServoAngle(head, rArm, lArm);
        } else if (result.contains("도리도리")) {
            for (int i = 0; i < 3; i++) {
                head = 60;
                xrDuino.setServoAngle(head, rArm, lArm);
                sleep(500);
                head = 120;
                xrDuino.setServoAngle(head, rArm, lArm);
                sleep(500);
            }
            head = 90;
            xrDuino.setServoAngle(head, rArm, lArm);
        } else if (result.contains("스탑") || result.contains("스톱") || result.contains("멈춰")) {
            lMotor = 0;
            rMotor = 0;
            xrDuino.setMotorSpeed(lMotor, rMotor);
        } else if (result.contains("차렷")) {
            if (danceThread.isAlive()) {
                try {
                    danceThread.join(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lMotor = 0;
            rMotor = 0;
            head = 90;
            lArm = 90;
            rArm = 90;
            xrDuino.setMotorSpeed(lMotor, rMotor);
            xrDuino.setServoAngle(head, rArm, lArm);
        } else if (result.contains("돌아")) {
            xrDuino.setMotorSpeed(-200, 200);
            sleep(2000);
            xrDuino.setMotorSpeed(0, 0);
        } else if (result.contains("흰색") || result.contains("하얀색")) {
            xrDuino.setRGBLed(255, 255, 255);
        } else if (result.contains("무색")) {
            xrDuino.setRGBLed(0, 0, 0);
        } else if (result.contains("인사")) {
            lMotor = 200;
            rMotor = 200;
            xrDuino.setMotorSpeed(lMotor, rMotor);
            sleep(2500);

            tts.speak("안녕하세요 에뽀에여 ", TextToSpeech.QUEUE_FLUSH, null, null);

            lMotor = -200;
            rMotor = -200;
            xrDuino.setMotorSpeed(lMotor, rMotor);
            sleep(1000);

            lMotor = 0;
            rMotor = 0;
            xrDuino.setMotorSpeed(lMotor, rMotor);
        } else if (result.contains("춤")) {
            danceThread.start();
        } else if (result.contains("비트박스") || result.contains("랩")) {
            tts.setSpeechRate(2.0f);
            switch (new Random().nextInt(2)) {
                case 0:
                    tts.setLanguage(Locale.ENGLISH);
                    tts.speak(BEAT_BOX, TextToSpeech.QUEUE_FLUSH, null, null);
                case 1:
                    tts.speak(JANG_BEAT, TextToSpeech.QUEUE_FLUSH, null, null);
                    break;
            }
            tts.setSpeechRate(1.0f);
            tts.setLanguage(Locale.KOREA);
        } else if (result.contains("안녕")) {
            tts.speak(getText(R.string.hello_tip), TextToSpeech.QUEUE_FLUSH, null, null);
            rArm = 160;
            lArm = 160;
            xrDuino.setServoAngle(head, rArm, lArm);
            sleep(500);

            rArm = 30;
            lArm = 30;
            xrDuino.setServoAngle(head, rArm, lArm);
            sleep(500);

            rArm = 160;
            lArm = 160;
            xrDuino.setServoAngle(head, rArm, lArm);
            sleep(500);

            rArm = 30;
            lArm = 30;
            xrDuino.setServoAngle(head, rArm, lArm);
            sleep(500);

            rArm = 90;
            lArm = 90;
            xrDuino.setServoAngle(head, rArm, lArm);

        } else if (result.contains("이름")) {
            tts.speak(getText(R.string.intro), TextToSpeech.QUEUE_FLUSH, null, null);
        } else if (result.contains("사용 방법")
                || result.contains("메뉴얼")
                || result.contains("뭐 할 수")
                || result.contains("기능")
                || result.contains("매뉴얼")) {
            tts.speak(getText(R.string.manual), TextToSpeech.QUEUE_FLUSH, null, null);
        } else if (result.contains("노래")) {
            tts.speak(getText(R.string.fallback_music), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void onInit(int status) {
        Log.d(TAG, "onInit: " + status);
    }

    public String getMusicName(String musicName) {
        String replaceTargets[] = {" 틀어 줘", " 틀어줘", " 틀어", " 재생"};
        for (String replaceTarget : replaceTargets) {
            musicName = musicName.replaceAll(replaceTarget, "");
        }
        Log.d(TAG, "getMusicName: " + musicName);

        return musicName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        tts.shutdown();
        xrDuino.shutdownNow();
        super.onDestroy();
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.GET_ACCOUNTS
                    },
                    PERMISSIONS);
        }
    }

    private void getWeather() {
        if (ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this)
                    .getWeather()
                    .addOnSuccessListener(
                            weatherResponse -> {
                                Weather weather = weatherResponse.getWeather();

                                tts.speak(
                                        new weatherR(
                                                (Math.round(
                                                        weather.getTemperature(
                                                                Weather.CELSIUS) * 10f) / 10f),
                                                (Math.round(
                                                        weather.getFeelsLikeTemperature(
                                                                Weather.CELSIUS) * 10f) / 10f),
                                                (Math.round(weather.getHumidity())),
                                                weather.getConditions()[0])
                                                .toString(),
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        null);

                                switch (weather.getConditions()[0]) {
                                    case Weather.CONDITION_CLEAR: // 맑음
                                        xrDuino.setRGBLed(255, 130, 0);
                                        xrDuino.setMotorSpeed(-200, 200);
                                        for (int i = 1; i < 5; i++) {
                                            xrDuino.setServoAngle(90, 20, 20);
                                            sleep(1000);
                                            xrDuino.setServoAngle(90, 160, 160);

                                            sleep(1000);
                                        }
                                        xrDuino.setServoAngle(90, 90, 90);
                                        xrDuino.setMotorSpeed(0, 0);
                                        break;
                                    case Weather.CONDITION_CLOUDY:
                                    case Weather.CONDITION_WINDY: // 구름 & 바람
                                        xrDuino.setRGBLed(28, 32, 50);
                                        for (int i = 1; i <= 5; i++) {
                                            xrDuino.setServoAngle(90, 20, 160);
                                            sleep(1000);
                                            xrDuino.setServoAngle(90, 160, 20);
                                            sleep(1000);
                                        }
                                        xrDuino.setServoAngle(90, 90, 90);

                                        break;
                                    case Weather.CONDITION_FOGGY:
                                    case Weather.CONDITION_HAZY: // 흐림 & 안개
                                        xrDuino.setRGBLed(39, 79, 37);
                                        for (int i = 1; i <= 8; i++) {
                                            xrDuino.setMotorSpeed(100, 100);
                                            sleep(500);
                                            xrDuino.setMotorSpeed(-100, -100);
                                            sleep(500);
                                        }
                                        xrDuino.setServoAngle(90, 90, 90);
                                        xrDuino.setMotorSpeed(0, 0);
                                        break;

                                    case Weather.CONDITION_ICY:
                                    case Weather.CONDITION_RAINY:
                                    case Weather.CONDITION_SNOWY:
                                    case Weather.CONDITION_STORMY: // 얼음 비 눈 폭풍우
                                        xrDuino.setRGBLed(30, 26, 250);
                                        for (int i = 1; i < 5; i++) {
                                            xrDuino.setMotorSpeed(200, -200);
                                            sleep(1000);
                                        }
                                        break;

                                    case Weather.CONDITION_UNKNOWN:
                                        xrDuino.setRGBLed(91, 26, 250);

                                        for (int i = 1; i <= 8; i++) {
                                            xrDuino.setServoAngle(120, 90, 90);
                                            sleep(500);
                                            xrDuino.setServoAngle(60, 90, 90);
                                            sleep(500);
                                        }
                                        xrDuino.setServoAngle(90, 90, 90);
                                }
                            });
        }
    }

    public void exitSelectFragment(BluetoothDevice device) {
        xrDuino.consumeRequestDeviceSelect(device);
        //TODO View
        Snackbar.make(getWindow().getDecorView().getRootView(), "연결되었습니다.", Snackbar.LENGTH_LONG).show();
    }

    private void chooseAccount() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_GRANTED) {
            String accountName =
                    getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), ACCOUNT_PICKER);
            }
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of) Google Play Services on
     *                             this device.
     */
    private void showGooglePlayServicesAvailabilityErrorDialog( // 구글플레이 서비스 연동안돼있을때 오류
                                                                final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog =
                apiAvailability.getErrorDialog(
                        MainActivity.this, connectionStatusCode, GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private Location getLocation() {
        // 네트워크 사용유무
        boolean isNetworkEnabled;

        /*//상태값
        boolean isGetLocation = false;*/

        Location location = null;

        LocationManager locationManager;

        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return null;
        }
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // 현재 네트워크 상태 값 알아오기
            isNetworkEnabled =
                    locationManager != null
                            && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        10,
                        new SimpleLocationListener() {
                        });

                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }
}
