package com.example.soundstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.content.BroadcastReceiver;

import com.example.soundstone.services.OnClearFromRecentService;

//import com.koddev.notificationmusicplayer.Services.OnClearFromRecentService;
public class Player extends AppCompatActivity implements NotificationPlay{


    Button playbtn,nextbtn,prevbtn,btnff,btnfr;
    TextView txtname,txtstart,txtstop;
    SeekBar seekBar;

    String sname;
    public static final String EXTRA_NAME="song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekBar;
    NotificationChannel channel;


    static NotificationManager notificationManager;
    List<Track> tracks;

    public void removeReciver()
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(channel.getId());
        }
        unregisterReceiver(broadcastReceiver);

    }


    @Override
    protected void onDestroy() {

        removeReciver();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Sound Stone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        prevbtn=findViewById(R.id.prevbtn);
        playbtn=findViewById(R.id.playbtn);
        nextbtn=findViewById(R.id.nextbtn);
        btnff=findViewById(R.id.btnff);
        btnfr=findViewById(R.id.btnfr);
        txtname=findViewById(R.id.txtsn);
        txtstart=findViewById(R.id.txtstart);
        txtstop=findViewById(R.id.txtstop);
        seekBar=findViewById(R.id.seekbar);

        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i=getIntent();
        Bundle bundle=i.getExtras();
        mySongs=(ArrayList) bundle.getParcelableArrayList("songs");
        String songName=i.getStringExtra("songname");
        position=bundle.getInt("pos",0);
        txtname.setSelected(true);
        Uri uri=Uri.parse(mySongs.get(position).toString());
        sname=mySongs.get(position).getName();
        txtname.setText(sname);

        mediaPlayer=mediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(completionListener);
        playbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if(mediaPlayer.isPlaying())
                {
                    playbtn.setBackgroundResource(R.drawable.play_arrow);
                    mediaPlayer.pause();
                    CreateNotification.createNotification(Player.this, tracks.get(position),
                            R.drawable.play_arrow, position, tracks.size()-1);
                }
                else
                {
                    playbtn.setBackgroundResource(R.drawable.play_pause);
                    mediaPlayer.start();
                    CreateNotification.createNotification(Player.this, tracks.get(position),
                            R.drawable.play_pause, position, tracks.size()-1);
                }


            }

        });


                updateSeekBar=new Thread()
                {
                    @Override
                    public void run() {
                        int totalDuradation=mediaPlayer.getDuration();
                        int currentposition=0;

                        while(currentposition<totalDuradation)
                        {
                            try{

                                sleep(500);
                                currentposition=mediaPlayer.getCurrentPosition();
                                seekBar.setProgress(currentposition);




                            }catch (InterruptedException | IllegalStateException e)
                            {
                                e.printStackTrace();
                            }

                        }
                    }
                };
                seekBar.setMax(mediaPlayer.getDuration());
                updateSeekBar.start();
                seekBar.getProgressDrawable().setColorFilter(getResources().getColor(com.karumi.dexter.R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
                seekBar.getThumb().setColorFilter(getResources().getColor(com.karumi.dexter.R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        mediaPlayer.seekTo(seekBar.getProgress());
                    }
                });

                String endTime=createTime(mediaPlayer.getDuration());
                txtstop.setText(endTime);

                final Handler handler=new Handler();
                final int delay=1000;

                handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            String currentTime=createTime(mediaPlayer.getCurrentPosition());
                                            txtstart.setText(currentTime);
                                            handler.postDelayed(this,delay);
                                        }
                                    }, delay
                );

        nextbtn.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=(position+1)%mySongs.size();

                Uri u=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                sname=mySongs.get(position).getName();
                txtname.setText(sname);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(completionListener);
                playbtn.setBackgroundResource(R.drawable.play_pause);


                String endTime = createTime(mediaPlayer.getDuration());
                seekBar.setMax(mediaPlayer.getDuration());
                seekBar.setProgress(0);
                txtstop.setText(endTime);

                CreateNotification.createNotification(Player.this, tracks.get(position),
                        R.drawable.play_pause, position, tracks.size()-1);
            }
        });

        prevbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position--;
                if(position<0)
                    position=mySongs.size()-1;
                Uri u=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                sname=mySongs.get(position).getName();
                txtname.setText(sname);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(completionListener);
                playbtn.setBackgroundResource(R.drawable.play_pause);

                String endTime = createTime(mediaPlayer.getDuration());
                seekBar.setMax(mediaPlayer.getDuration());
                seekBar.setProgress(0);
                txtstop.setText(endTime);

                CreateNotification.createNotification(Player.this, tracks.get(position),
                        R.drawable.play_pause, position, tracks.size()-1);

            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mediaPlayer.isPlaying())
                            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);

                    }
                });
        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying())
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);

            }
        });


        //Notification player

            popluateTracks();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                createChannel();

                    registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));


                startService(new Intent(getBaseContext(), OnClearFromRecentService.class));

            CreateNotification.createNotification(this, tracks.get(position),
                    R.drawable.play_pause, position, tracks.size()-1);

        }


    }


    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            nextbtn.performClick();

        }
    };

    public String createTime(int duration)
    {
        String time="";
        int min=duration/1000/60;
        int sec=duration/1000%60;

        time+=min+":";
        if(sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;
    }

    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(Player.this, tracks.get(position),
                R.drawable.play_pause, position, tracks.size()-1);
        playbtn.setBackgroundResource(R.drawable.play_pause);
        mediaPlayer.start();
    }

    @Override
    public void onTrackPouse() {
        CreateNotification.createNotification(Player.this, tracks.get(position),
                R.drawable.play_arrow, position, tracks.size()-1);
        playbtn.setBackgroundResource(R.drawable.play_arrow);
        mediaPlayer.pause();
    }

    @Override
    public void onTrackNext() {
        mediaPlayer.stop();
        mediaPlayer.release();
        position=(position+1)%mySongs.size();

        Uri u=Uri.parse(mySongs.get(position).toString());
        mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
        sname=mySongs.get(position).getName();
        txtname.setText(sname);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(completionListener);
        playbtn.setBackgroundResource(R.drawable.play_pause);

        String endTime = createTime(mediaPlayer.getDuration());
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);
        txtstop.setText(endTime);

        CreateNotification.createNotification(Player.this, tracks.get(position),
                R.drawable.play_pause, position, tracks.size()-1);
    }

    @Override
    public void onTrackPrevious() {
        mediaPlayer.stop();
        mediaPlayer.release();
        position--;
        if(position<0)
            position=mySongs.size()-1;
        Uri u=Uri.parse(mySongs.get(position).toString());
        mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
        sname=mySongs.get(position).getName();
        txtname.setText(sname);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(completionListener);
        playbtn.setBackgroundResource(R.drawable.play_pause);

        String endTime = createTime(mediaPlayer.getDuration());
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);
        txtstop.setText(endTime);

        CreateNotification.createNotification(Player.this, tracks.get(position),
                R.drawable.play_pause, position, tracks.size()-1);

    }
    private void popluateTracks(){
        tracks = new ArrayList<>();
        int br=0;
        for(File trensong:mySongs)
        {
            tracks.add(new Track(trensong.getName().replace(".mp3",""), "Broj "+br, R.drawable.playerbackground1));
            br++;
        }

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action){
                case CreateNotification.ACTION_PREVIUOS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (mediaPlayer.isPlaying()){
                        onTrackPouse();
                    } else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "KOD Dev", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

