package com.nojunjae.epor.data;

public class YouTubeData {
    private String id; // id를 string 값으로 받아오기위한 변수 선언
    private boolean isPlayList = false; // 재생목록 값을 false로 초기화
    private boolean isVideo = false; // 비디오 값을 false로 초기화

    public YouTubeData(String id, String type) {
        if (type.contains("video")) isVideo = true; // 형식이 비디오일 경우 비디오 값을 true로 초기화
        else isPlayList = true; // 아닐시 재생목록 값을 true로 초기화

        this.id = id; // id값을 받아옴
    }

    public String getId() {
        return id;
    }

    public boolean isPlayList() {
        return isPlayList;
    }

    public boolean isVideo() {
        return isVideo;
    }
}
