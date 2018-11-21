package com.nojunjae.epor;

import com.google.android.gms.awareness.state.Weather;

public class weatherR {

    /*public void setTemp(float temp) {
        Temp = temp;
    }

    public void setSent(float sent) {
        Sent = sent;
    }

    public void setHumi(int humi) {
        Humi = humi;
    }

    public void setCond(int cond) {
        Cond = cond;
    }*/

    private float Temp; // 온도
    private float Sent; // 체감온도
    private int Humi; // 습도
    private int Cond; // 컨디션 ex) 맑음, 흐림

    public weatherR(float Temp, float Sent, int Humi, int Cond) {

        this.Temp = Temp;
        this.Sent = Sent;
        this.Humi = Humi;
        this.Cond = Cond;
    }

    public float getTemp() {
        return Temp;
    }

    public float getSent() {
        return Sent;
    }

    public float getHumi() {
        return Humi;
    }

    public String getCond() {

        if (Weather.CONDITION_CLEAR == Cond) {
            return "맑음";
        } else if (Weather.CONDITION_CLOUDY == Cond) {
            return "구름";
        } else if (Weather.CONDITION_FOGGY == Cond) {
            return "흐림";
        } else if (Weather.CONDITION_HAZY == Cond) {
            return "안개";
        } else if (Weather.CONDITION_ICY == Cond) {
            return "얼음";
        } else if (Weather.CONDITION_RAINY == Cond) {
            return "비";
        } else if (Weather.CONDITION_SNOWY == Cond) {
            return "눈";
        } else if (Weather.CONDITION_STORMY == Cond) {
            return "폭풍우";
        } else if (Weather.CONDITION_WINDY == Cond) {
            return "바람";
        } else if (Weather.CONDITION_UNKNOWN == Cond) {
            return "알 수 없음";
        }

        return null;
    }

    public String toString() {

        return "오늘은 대체적으로 "
                + getCond()
                + "이며, 온도는 "
                + getTemp()
                + "도 이고 습도는 "
                + getHumi()
                + " 체감온도는 "
                + getSent()
                + "도 입니다.";
    }
}
