package com.nojunjae.epor;

import android.speech.tts.UtteranceProgressListener;

public abstract class SimpleUtteranceProgressListener extends UtteranceProgressListener {
    @Override
    public void onError(String utteranceId, int errorCode) {
        super.onError(utteranceId, errorCode);
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        super.onStop(utteranceId, interrupted);
    }

    @Override
    public void onBeginSynthesis(
            String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
        super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
    }

    @Override
    public void onAudioAvailable(String utteranceId, byte[] audio) {
        super.onAudioAvailable(utteranceId, audio);
    }

    @Override
    public void onRangeStart(String utteranceId, int start, int end, int frame) {
        super.onRangeStart(utteranceId, start, end, frame);
    }

    @Override
    public void onStart(String utteranceId) {
    }

    @Override
    public void onDone(String utteranceId) {
    }

    @Deprecated
    @Override
    public void onError(String utteranceId) {
    }
}
