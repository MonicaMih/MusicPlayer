package com.example.musicplayerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;


public class PlayerActivity extends AppCompatActivity {


    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private float old_rz = 0.0f;
    private float old_tx = 0.0f;
    private float old_tz = 0.0f;
    private Boolean notFirstTimeR = false;
    private Boolean notFirstTimeT = false;

    Button playBtn, nextBtn, prevBtn, fastForwardBtn, fastRewindBtn;
    TextView txtSongName, txtSongStart, txtSongStop;
    SeekBar seekBar;
    BarVisualizer visualizer;
    ImageView imageView;

    String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> songList;
    Thread  updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(visualizer != null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setTitle("Now playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prevBtn = findViewById(R.id.prevbtn);
        nextBtn = findViewById(R.id.nextbtn);
        playBtn = findViewById(R.id.playbtn);
        fastForwardBtn = findViewById(R.id.fastforwardbtn);
        fastRewindBtn = findViewById(R.id.fastrewindbtn);
        txtSongName = findViewById(R.id.txtsn);
        txtSongStart = findViewById(R.id.txtsongstart);
        txtSongStop = findViewById(R.id.txtsongstop);
        seekBar = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.bar);
        imageView = findViewById(R.id.imageview);

        accelerometer = new Accelerometer(this);
        gyroscope = new Gyroscope(this);

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songList = (ArrayList) bundle.getParcelableArrayList("songs");
        final String song_name = intent.getStringExtra("songname");
        position = bundle.getInt("position",0);
        txtSongName.setSelected(true);
        Uri uri = Uri.parse(songList.get(position).toString());
        songName = songList.get(position).getName();
        txtSongName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while(currentPosition < totalDuration){
                    try{
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }catch(InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.simpleWhite), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.simpleWhite), PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = calculateTime(mediaPlayer.getDuration());
        txtSongStop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = calculateTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    playBtn.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }else{
                    playBtn.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextBtn.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1){
            visualizer.setAudioSessionId(audioSessionId);
        }

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1) % songList.size());
                Uri u = Uri.parse(songList.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                songName = songList.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();
                playBtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1) < 0) ? (songList.size() - 1 ) : (position - 1);
                Uri u = Uri.parse(songList.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                songName = songList.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();
                playBtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        fastForwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });

        fastRewindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });


        accelerometer.setListener(new Accelerometer.Listener() {
            @Override

            public void onTranslation(float tx, float ty, float tz) {

                //System.out.println("/*/*before - tx:" + tx + "---------  old_tx:" + old_tx);

                //System.out.println("/*/*before - tz:" + tz + "---------  old_tz:" + old_tz);
                if (notFirstTimeT) {
                    float difference = old_tx - tx;
                    if (Math.abs(difference) > 5.0f) {
                        if (difference < 0.0f) {
                            //System.out.println("I am performing next btn click");
                            nextBtn.performClick();
                        } else if (difference > 0.0f) {
                            //System.out.println("I am performing prev btn click");
                            prevBtn.performClick();
                        }
                    }

                    if (Math.abs(old_tz - tz) > 10.0f) {

                        //System.out.println("I am performing play btn click-cool");
                        playBtn.performClick();
                    }
                }

                old_tx = tx;
                old_tz = tz;
                //System.out.println("/*/*after - tx:"+tx+"-------- old_tx:"+old_tx);
                //System.out.println("/*/*after - tz:" + tz + "---------  old_tz:" + old_tz);
                notFirstTimeT = true;
            }
        });

        gyroscope.setListener(new Gyroscope.Listener() {
            @Override
            public void onRotation(float rx, float ry, float rz) {

               // System.out.println("/*/*before - rz:"+rz+"           -----   old_rz:"+old_rz);
                if(notFirstTimeR) {
                    if (Math.abs(old_rz - rz) > 5f) {

                        //System.out.println("I am performing play btn click");
                        playBtn.performClick();
                        old_rz = rz;
                    }
                }

               // System.out.println("/*/*after  - rz:"+rz+"             -----   old_rz:"+old_rz);
                old_rz = rz;
                notFirstTimeR = true;

            }
        });
    }

    public void startAnimation(View view){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        accelerometer.register();
        gyroscope.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        accelerometer.unregister();
        gyroscope.unregister();
    }

    public String calculateTime(int duration){
        String timeToDisplay = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        timeToDisplay += min + ":";
        if (sec < 10) {
            timeToDisplay += "0";
        }
        timeToDisplay += sec;
        return timeToDisplay;
    }
}
