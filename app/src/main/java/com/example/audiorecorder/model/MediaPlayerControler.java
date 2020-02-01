package com.example.audiorecorder.model;

import android.media.MediaPlayer;

public class MediaPlayerControler {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MediaPlayer.OnCompletionListener onCompletionListener;

    public MediaPlayerControler(MediaPlayer.OnCompletionListener onCompletionListener){
        this.onCompletionListener = onCompletionListener;

    }

    public void startPlayer(String fileName) {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        if (mediaPlayer!= null){
            mediaPlayer.pause();
        }
    }

    public void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        releasePlayer();


    }

    public void resumePlayer(){
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
