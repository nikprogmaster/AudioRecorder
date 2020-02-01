package com.example.audiorecorder.dataprovider;

import android.os.Environment;
import android.util.Log;

import com.example.audiorecorder.model.Record;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataProvider  {


    public static final String MAIN_FOLDER = "/AudioRecorder";
    List<Record> recordsList;

    public List<Record> loadFiles() {
        String state = Environment.getExternalStorageState();
        List<File> fileList;
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            File f = Environment.getExternalStorageDirectory();
            String path = f.getAbsolutePath() + MAIN_FOLDER;
            File mainFolder = new File(path);
            File[] files = mainFolder.listFiles();
            if (files != null) {
                fileList = Arrays.asList(files);
                recordsList = new ArrayList<>();
                for (int i = 0; i < fileList.size(); i++) {
                    recordsList.add(new Record((fileList.get(i).getName()), fileList.get(i).getAbsolutePath()));
                }
                return recordsList;
            }
        }
        return null;
    }


    public String makeFolder() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            File f = Environment.getExternalStorageDirectory();
            File newfolder = new File(f, MAIN_FOLDER);
            if (!newfolder.exists()) {
                newfolder.mkdirs();
                return newfolder.getAbsolutePath();
            } else
                return f.getAbsolutePath() + "/" + MAIN_FOLDER;
        }
        return null;
    }



}
