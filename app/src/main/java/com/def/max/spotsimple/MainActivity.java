package com.def.max.spotsimple;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;

import com.def.max.spotsimple.Utils.Languages;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private SpeechService mSpeechService;

    private VoiceRecorder mVoiceRecorder;

    private NiceSpinner sourceSpinner;

    private ArrayList<String> categories;

    private AppCompatButton reco;

    private AppCompatTextView mText,recorder;

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback()
    {
        @Override
        public void onVoiceStart()
        {
            if (mSpeechService != null)
            {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size)
        {
            if (mSpeechService != null)
            {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd()
        {


            if (mSpeechService != null)
            {
                mSpeechService.finishRecognizing();
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder)
        {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mSpeechService = null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reco = findViewById(R.id.reco);
        sourceSpinner = findViewById(R.id.source_spinner);
        mText = findViewById(R.id.mText);
        recorder = findViewById(R.id.recorder);

        categories = new ArrayList<>();

        Collections.addAll(categories, Languages.getLangEN());

        sourceSpinner.attachDataSource(categories);

        reco.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bindService(new Intent(MainActivity.this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

                startVoiceRecorder();
            }
        });
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder()
    {
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        if (mVoiceRecorder != null)
        {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private final SpeechService.Listener mSpeechServiceListener = new SpeechService.Listener()
    {
        @Override
        public void onSpeechRecognized(final String text, final boolean isFinal)
        {
            if (isFinal)
            {
                mVoiceRecorder.dismiss();
            }
            if (mText != null && !TextUtils.isEmpty(text))
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (isFinal)
                        {
                            mText.setText(null);

                            stopVoiceRecorder();
                        }
                        else
                        {
                            mText.setText(text);
                        }
                    }
                });
            }
        }
    };

}
