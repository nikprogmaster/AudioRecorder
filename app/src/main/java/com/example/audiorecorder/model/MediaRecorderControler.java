package com.example.audiorecorder.model;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

public class MediaRecorderControler {

    private static MediaRecorder recorder = new MediaRecorder();
    private boolean isFirstStarted = true;
    private boolean isRecording = false;

    public boolean getIsFirstStarted() {
        return isFirstStarted;
    }

    public void setIsFirstStarted(boolean isFirstStarted) {
        this.isFirstStarted = isFirstStarted;
    }

    public boolean getIsRecording() {
        return isRecording;
    }

    public void setIsRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }


    public void initRecorder(String path){

        File f = new File(path);
        if(f.exists()){
            f.delete();
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(path);
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void releaseRecorder() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    public void startRecording(){
        recorder.start();   // Recording is now started

    }

    public void stopRecording(){
        isRecording = false;
        isFirstStarted = true;
        //startRecordButton.setSelected(true);

        recorder.stop();
    }

    public void pauseRecording(){
        isRecording = false;
        //startRecordButton.setSelected(true);
        recorder.pause();
    }

    public void resumeRecording(){
        isRecording = true;
        //startRecordButton.setSelected(false);
        recorder.resume();
    }

}
