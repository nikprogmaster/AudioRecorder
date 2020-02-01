package com.example.audiorecorder.view;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiorecorder.model.MediaPlayerControler;
import com.example.audiorecorder.model.MediaRecorderControler;
import com.example.audiorecorder.R;
import com.example.audiorecorder.services.PlayService;
import com.example.audiorecorder.services.RecordService;
import com.example.audiorecorder.view.adapters.RecordClickListener;
import com.example.audiorecorder.view.adapters.RecordsAdapter;
import com.example.audiorecorder.dataprovider.DataProvider;
import com.example.audiorecorder.model.Record;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class RecordsFragment extends Fragment {

    public static final String BUNDLE_RECORD_NAME = "record_name";
    private List<Record> recordsList = new ArrayList<>();
    private RecordsAdapter recordsAdapter;
    private RecyclerView recyclerView;
    private View loadingView;
    private View noRecordsView, playTrackLayout, recordTrackLayout;
    private DataProvider dataProvider;
    private EditText recordName;
    private ImageButton startRecordButton, stopButton, startPlayButton, stopPlayButton;
    private TextView trackName;
    private RecordsFragment thisFragment;
    public static final int MULTIPLE_PERMISSIONS = 10;
    private static String ROOT_FOLDER_PATH;
    public static MediaRecorderControler mediaRecorderControler;
    private MediaPlayerControler mediaPlayerControler;
    private static boolean mBound;
    private static boolean mBound2;
    private static RecordService mService;
    Messenger mMessenger = new Messenger(new PlayHandler());

    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    private RecordClickListener recordClickListener = new RecordClickListener() {
        @Override
        public void onRecordClick(Record record) {
            recordTrackLayout.setVisibility(View.GONE);
            playTrackLayout.setVisibility(View.VISIBLE);
            stopPlayButton.setVisibility(View.VISIBLE);
            startPlayButton.setSelected(false);
            trackName.setText(record.getName());
            mediaPlayerControler.startPlayer(record.getPath());
            startPlayService();
        }
    };

    public ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            RecordService.LocalBinder binder = (RecordService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            setServiceControler();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };



    private ServiceConnection mPlayerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Messenger playServiceMessenger = new Messenger(service);
            mBound2 = true;
            Message msg = Message.obtain(null, PlayService.MSG_START_PlAYER);
            msg.replyTo = mMessenger;
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_RECORD_NAME, trackName.getText().toString());
            msg.setData(bundle);
            try {
                playServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessenger = null;
            mBound2 = false;
        }
    };


    private void setServiceControler() {
        mService.setControler(new IServiceControler() {
            @Override
            public void onStopService() {
                if (mBound) {
                    getActivity().unbindService(mConnection);
                    mBound = false;
                }
                Intent intent = new Intent(getActivity(), RecordService.class);
                getActivity().stopService(intent);
                stopButton.setEnabled(false);
                recordName.setEnabled(true);
                startRecordButton.setSelected(true);
                mediaRecorderControler.stopRecording();
                Toast.makeText(getActivity(), "Аудиофайл сохранен", Toast.LENGTH_SHORT).show();
                refreshRecycler();
            }

            @Override
            public void onPauseService() {
                startRecordButton.setSelected(true);
                mediaRecorderControler.pauseRecording();
            }

            @Override
            public void onResumeService() {
                startRecordButton.setSelected(false);
                mediaRecorderControler.resumeRecording();
            }
        });
    }

    public RecordsFragment newInstance() {
        thisFragment = new RecordsFragment();
        return thisFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.records_fragment_layout, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler);
        loadingView = view.findViewById(R.id.loading_view);
        noRecordsView = view.findViewById(R.id.no_records_view);
        recordName = view.findViewById(R.id.record_name);
        stopButton = view.findViewById(R.id.stop_button);
        startRecordButton = view.findViewById(R.id.start_record_button);
        startPlayButton = view.findViewById(R.id.start_play_button);
        stopPlayButton = view.findViewById(R.id.stop_playing_button);
        trackName = view.findViewById(R.id.track_name);
        playTrackLayout = view.findViewById(R.id.play_track_layout);
        recordTrackLayout = view.findViewById(R.id.recordTrackLayout);

        mediaRecorderControler = new MediaRecorderControler();
        MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startPlayButton.setSelected(true);
                stopPlayService();
            }
        };
        mediaPlayerControler = new MediaPlayerControler(listener);

        startRecordButton.setSelected(true);
        recordsAdapter = new RecordsAdapter(recordsList, recordClickListener);
        recyclerView.setAdapter(recordsAdapter);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dataProvider = new DataProvider();
        LoadFilesTask loadFilesTask = new LoadFilesTask(this);
        loadFilesTask.execute();

        startPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayerControler.isPlaying()) {
                    startPlayButton.setSelected(true);
                    mediaPlayerControler.pausePlayer();
                    pausePlayService();
                } else {
                    startPlayButton.setSelected(false);
                    mediaPlayerControler.resumePlayer();
                    resumePlayService();
                }
            }
        });

        stopPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerControler.stopPlayer();
                playTrackLayout.setVisibility(View.GONE);
                recordTrackLayout.setVisibility(View.VISIBLE);
                stopPlayService();
            }
        });

        startRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaRecorderControler.getIsFirstStarted()) {
                    if (!recordName.getText().toString().equals("")) {
                        mediaRecorderControler.setIsFirstStarted(false);
                        mediaRecorderControler.setIsRecording(true);
                        recordName.setEnabled(false);
                        stopButton.setVisibility(View.VISIBLE);
                        stopButton.setEnabled(true);
                        v.setSelected(false);
                        requestPermissions();
                    } else {
                        recordName.setError(getResources().getString(R.string.error_text));
                    }
                } else if (mediaRecorderControler.getIsRecording()) {
                    pauseRecordService();
                } else {
                    resumeRecordService();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecordService();
                Toast.makeText(getActivity(), "Аудиофайл сохранен", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                ROOT_FOLDER_PATH = dataProvider.makeFolder();
                mediaRecorderControler.initRecorder(ROOT_FOLDER_PATH + "/" + recordName.getText().toString() + ".mp3");
                startRecordService();
                mediaRecorderControler.startRecording();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) &&
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getActivity(), "App required access to audio", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(permissions, MULTIPLE_PERMISSIONS);
            }

        } else {
            ROOT_FOLDER_PATH = dataProvider.makeFolder();
            mediaRecorderControler.initRecorder(ROOT_FOLDER_PATH + "/" + recordName.getText().toString() + ".mp3");
            startRecordService();
            mediaRecorderControler.startRecording();
        }
    }

    private void refreshRecycler() {
        dataProvider = new DataProvider();
        LoadFilesTask task = new LoadFilesTask(this);
        task.execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MULTIPLE_PERMISSIONS && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            ROOT_FOLDER_PATH = dataProvider.makeFolder();
            mediaRecorderControler.initRecorder(ROOT_FOLDER_PATH + "/" + recordName.getText().toString() + ".mp3");
            startRecordService();
            mediaRecorderControler.startRecording();

        }
    }


    private void stopRecordService() {
        Intent intent = new Intent(getActivity(), RecordService.class);
        intent.setAction(RecordService.ACTION_CLOSE);
        getActivity().startService(intent);
    }

    private void pauseRecordService() {
        Intent intent = new Intent(getActivity(), RecordService.class);
        intent.setAction(RecordService.ACTION_PAUSE);
        getActivity().startService(intent);
    }


    private void resumeRecordService() {
        Intent intent = new Intent(getActivity(), RecordService.class);
        intent.setAction(RecordService.ACTION_PAUSE);
        getActivity().startService(intent);
    }


    private void startRecordService() {
        Intent intent = new Intent(getActivity(), RecordService.class);
        intent.setAction(RecordService.ACTION_START);
        getActivity().startService(intent);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private static class LoadFilesTask extends AsyncTask<Void, Void, List<Record>> {

        private final WeakReference<RecordsFragment> fragmentRef;
        private final DataProvider dataProvider;

        private LoadFilesTask(RecordsFragment fragment) {
            dataProvider = fragment.dataProvider;
            fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
            RecordsFragment fragment = fragmentRef.get();
            if (fragment != null) {
                fragment.loadingView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<Record> doInBackground(Void... voids) {
            return dataProvider.loadFiles();
        }

        @Override
        protected void onPostExecute(List<Record> records) {
            RecordsFragment fragment = fragmentRef.get();
            if (fragment == null) {
                return;
            }
            fragment.loadingView.setVisibility(View.GONE);
            if (records == null) {
                fragment.noRecordsView.setVisibility(View.VISIBLE);
            } else {
                fragment.noRecordsView.setVisibility(View.GONE);
                fragment.recordsAdapter.setRecords(records);
            }
        }
    }

    public class PlayHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case PlayService.MSG_STOP_PlAYER:
                    mediaPlayerControler.stopPlayer();
                    playTrackLayout.setVisibility(View.GONE);
                    recordTrackLayout.setVisibility(View.VISIBLE);
                    Log.d("Во фрагменте СТОП", "handleMessage() called with: msg = [" + msg + "]");
                    unBindPlayerService();
                    break;
                case PlayService.MSG_PAUSE_PlAYER:
                    mediaPlayerControler.pausePlayer();
                    startPlayButton.setSelected(true);
                    Log.d("Во фрагменте ПАУЗА", "handleMessage() called with: msg = [" + msg + "]");
                    break;
                case PlayService.MSG_RESUME_PlAYER:
                    startPlayButton.setSelected(false);
                    mediaPlayerControler.resumePlayer();
                    Log.d("Во фрагменте RESUME", "handleMessage() called with: msg = [" + msg + "]");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    private void stopPlayService() {
        Intent intent = new Intent(getActivity(), PlayService.class);
        intent.setAction(PlayService.ACTION_CLOSE);
        getActivity().startService(intent);
    }

    private void pausePlayService() {
        Intent intent = new Intent(getActivity(), PlayService.class);
        intent.setAction(PlayService.ACTION_PAUSE);
        getActivity().startService(intent);
    }


    private void resumePlayService() {
        Intent intent = new Intent(getActivity(), PlayService.class);
        intent.setAction(PlayService.ACTION_PAUSE);
        getActivity().startService(intent);
    }


    private void startPlayService() {
        Intent intent = new Intent(getActivity(), PlayService.class);
        intent.setAction(PlayService.ACTION_START);
        getActivity().startService(intent);
        getActivity().bindService(intent, mPlayerConnection, Context.BIND_AUTO_CREATE);

    }

    private void unBindPlayerService() {
        getActivity().unbindService(mPlayerConnection);
    }

}
