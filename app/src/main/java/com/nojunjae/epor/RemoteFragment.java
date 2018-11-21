package com.nojunjae.epor;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

public class RemoteFragment extends Fragment implements View.OnClickListener {

    int Lmotor = 0, Rmotor = 0;
    int head = 90, Larm = 90, Rarm = 90;
    private Button motorUL, motorU, motorUR;
    private Button motorL, motorSTOP, motorR;
    private Button motorDL, motorD, motorDR;
    private Button headL, headSTOP, headR;

    private Button lArmU;
    private Button lArmSTOP;
    private Button lArmD;

    private Button rArmU;
    private Button rArmSTOP;
    private Button rArmD;

    private XRDuino xrDuino;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.remote_button, null);
        return initMotor(initArms(initHead(view)));
    }

    public void setXrDuino(XRDuino xrDuino) {
        this.xrDuino = xrDuino;
    }

    private View initHead(View view) {
        headL = view.findViewById(R.id.head_left);
        headSTOP = view.findViewById(R.id.head_stop);
        headR = view.findViewById(R.id.head_right);

        headL.setOnClickListener(this);
        headSTOP.setOnClickListener(this);
        headR.setOnClickListener(this);
        return view;
    }

    private View initArms(View view) {
        lArmU = view.findViewById(R.id.larm_up);
        lArmSTOP = view.findViewById(R.id.larm_stop);
        lArmD = view.findViewById(R.id.larm_down);

        rArmU = view.findViewById(R.id.rarm_up);
        rArmSTOP = view.findViewById(R.id.rarm_stop);
        rArmD = view.findViewById(R.id.rarm_down);

        lArmU.setOnClickListener(this);
        lArmSTOP.setOnClickListener(this);
        lArmD.setOnClickListener(this);

        rArmU.setOnClickListener(this);
        rArmSTOP.setOnClickListener(this);
        rArmD.setOnClickListener(this);

        return view;
    }

    private View initMotor(View view) {
        motorUL = view.findViewById(R.id.motor_ul);
        motorU = view.findViewById(R.id.motor_up);
        motorUR = view.findViewById(R.id.motor_ur);
        motorL = view.findViewById(R.id.motor_left);
        motorSTOP = view.findViewById(R.id.motor_stop);
        motorR = view.findViewById(R.id.motor_right);
        motorDL = view.findViewById(R.id.motor_dl);
        motorDL = view.findViewById(R.id.motor_down);
        motorDR = view.findViewById(R.id.motor_dr);

        motorUL.setOnClickListener(this);
        motorU.setOnClickListener(this);
        motorUR.setOnClickListener(this);
        motorL.setOnClickListener(this);
        motorSTOP.setOnClickListener(this);
        motorR.setOnClickListener(this);
        motorDL.setOnClickListener(this);
        motorDL.setOnClickListener(this);
        motorDR.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (xrDuino == null) return;
        int id = view.getId();

        switch (id) {
            case R.id.motor_dl:
                Lmotor = 100;
                Rmotor = 275;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;
            case R.id.motor_up:
                Lmotor = 200;
                Rmotor = 200;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;
            case R.id.motor_dr:
                Lmotor = 275;
                Rmotor = 100;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;

            case R.id.motor_left:
                Lmotor = -200;
                Rmotor = 200;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;
            case R.id.motor_stop:
                Lmotor = 0;
                Rmotor = 0;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;
            case R.id.motor_right:
                Lmotor = 200;
                Rmotor = -200;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;

            case R.id.motor_ul:
                Lmotor = -100;
                Rmotor = -275;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;
            case R.id.motor_down:
                Lmotor = -200;
                Rmotor = -200;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;
            case R.id.motor_ur:
                Lmotor = -275;
                Rmotor = -100;
                xrDuino.setMotorSpeed(Rmotor, Lmotor);
                break;

            case R.id.head_left: // 머리 왼쪽으로
                head -= 20;
                if (head <= 20) head = 20;

                xrDuino.setServoAngle(head, Rarm, Larm);
                break;
            case R.id.head_stop: // 머리 원상복귀
                head = 90;

                xrDuino.setServoAngle(head, Rarm, Larm);
                break;
            case R.id.head_right: // 머리 오른쪽으로
                head += 20;
                if (head >= 160) head = 160;

                xrDuino.setServoAngle(head, Rarm, Larm);
                break;

            case R.id.larm_up: // 왼쪽손 업
                Larm -= 20;
                if (Larm <= 20) Larm = 20;
                xrDuino.setServoAngle(head, Rarm, Larm);
                break;
            case R.id.larm_down: // 왼손 다운
                Larm += 20;
                if (Larm >= 160) Larm = 160;

                xrDuino.setServoAngle(head, Rarm, Larm);
                break;
            case R.id.larm_stop: // 왼손 원위치
                Larm = 90;

                xrDuino.setServoAngle(head, Rarm, Larm);
                break;

            case R.id.rarm_up:
                Rarm += 20;
                if (Rarm >= 160) Rarm = 160;

                xrDuino.setServoAngle(head, Rarm, Larm);
                break;
            case R.id.rarm_down:
                Rarm -= 20;
                if (Rarm <= 20) Rarm = 20;

                xrDuino.setServoAngle(head, Rarm, Larm);
                break;
            case R.id.rarm_stop:
                Rarm = 90;

                xrDuino.setServoAngle(head, Rarm, Larm);
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
