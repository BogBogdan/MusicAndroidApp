package com.example.soundstone;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.soundstone.services.OnClearFromRecentService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    ListView listView;
    String songs[];
    static Intent tren;
    static boolean da=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        listView=findViewById(R.id.ListViewSongs);
        runTimePermition();

    }

    public void runTimePermition()
    {
        Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                displaySongs();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public ArrayList<File> findSongs(File file)
    {
        ArrayList<File> songs=new ArrayList<>();
        File files[]=file.listFiles();

        if(file.exists())
        for(File trenfile:files)
        {
            if(trenfile.isDirectory() && !trenfile.isHidden())
            {
                songs.addAll(findSongs(trenfile));
            }
            else if(trenfile.getName().endsWith(".mp3") || trenfile.getName().endsWith(".wav"))
            {
                songs.add(trenfile);
            }
        }

        return songs;

    }

    public void displaySongs()
    {
        final ArrayList<File> Songs=findSongs(Environment.getExternalStorageDirectory());
        songs=new String[Songs.size()];

        for(int i=0;i<Songs.size();i++)
        {
            songs[i]=Songs.get(i).getName().replace(".mp3","").replace(".wav","");
        }
        System.out.println(songs.length);
      /*  ArrayAdapter<String> Adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,songs);
        listView.setAdapter(Adapter);*/

        customAdapter customAdapter=new customAdapter();
        listView.setAdapter(customAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                    tren=new Intent(getApplicationContext(), Player.class)
                            .putExtra("songs", Songs)
                            .putExtra("songsNames", songs)
                            .putExtra("pos", i);
                    startActivity(tren);

                }
        });


    }

    public class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return songs.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView=getLayoutInflater().inflate(R.layout.list_item,null);
            TextView textView=myView.findViewById(R.id.txtsongname);
            textView.setSelected(true);
            textView.setText(songs[i]);

            return myView;
        }
    }
}