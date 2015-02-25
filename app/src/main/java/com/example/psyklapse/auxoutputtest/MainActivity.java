package com.example.psyklapse.auxoutputtest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;


public class MainActivity extends ActionBarActivity {

    Thread audioTrackThread;
    int sampleRate = 44100;
    boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        try {
            audioTrackThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioTrackThread = null;
    }

    public void onSignalToggleClick(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        EditText frequencyInput = (EditText) findViewById(R.id.frequency);
        final double frequency = Double.parseDouble(frequencyInput.getText().toString());

        if (on) {
            if (frequency < 20 || frequency > 20000)
            {
                throw new IllegalArgumentException("Frequency must be between 20 and 20,000 Hz!");
            }
            audioTrackThread = new Thread() {
                public void run() {
                    setPriority(MAX_PRIORITY);

                    isRunning = true;

                    int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                            AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

                    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                            AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize, AudioTrack.MODE_STREAM);

                    short samples[] = new short[bufferSize];
                    int amp = 10000;
                    double twoPi = 8.*Math.atan(1.);
                    double ph = 0.0;

                    audioTrack.play();

                    while(isRunning){
                        for(int i = 0; i< bufferSize; i++){
                            samples[i] = (short) (amp*Math.sin(ph));
                            ph += twoPi*frequency/sampleRate;
                        }
                        audioTrack.write(samples, 0, bufferSize);
                    }

                    audioTrack.stop();
                    audioTrack.release();
                }
            };

            audioTrackThread.start();
        }
        else {
            isRunning = false;
//            try {
//                audioTrackThread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            audioTrackThread = null;
        }
    }
}
