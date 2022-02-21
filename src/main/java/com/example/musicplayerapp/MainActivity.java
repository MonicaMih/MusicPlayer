package com.example.musicplayerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    ListView listView;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView= findViewById(R.id.listViewSong);
        runtimePermission();
    }

    public void runtimePermission(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displayAllSongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> findSongs(File file){
        ArrayList<File> songs = new ArrayList<>();
        File[] files = file.listFiles();
        if(files!=null) {
            for (File songFile : files) {
                if (songFile.isDirectory() && !songFile.isHidden()) {
                    songs.addAll(findSongs(songFile));
                } else {
                    if (songFile.getName().endsWith(".mp3") || songFile.getName().endsWith(".wav")) {
                        songs.add(songFile);
                    }
                }
            }
        }
        return songs;
    }


    void displayAllSongs(){
        final ArrayList<File> songs = findSongs(Environment.getExternalStorageDirectory());
        displaySongs(songs);
    }

    public void displaySongs(final ArrayList<File> songs) {
        items = new String[songs.size()];
        for (int i = 0; i<songs.size(); i++){
            items[i] = songs.get(i).getName().replace(".mp3","").replace(".wav","");
        }

        CustomAdapter customAdapter = new CustomAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = (String)listView.getItemAtPosition(position);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                .putExtra("songs",songs)
                .putExtra("songname", songName)
                .putExtra("position", position));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search a song...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final ArrayList<File> songs = findSongs(Environment.getExternalStorageDirectory());
                ArrayList<File> filteredSongs = new ArrayList<>();
                for(File song : songs)
                    if(song.getName().toLowerCase().contains(newText.toLowerCase())){
                        filteredSongs.add(song);
                    }
                displaySongs(filteredSongs);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textSong = view.findViewById(R.id.txtsongname);
            textSong.setSelected(true);
            textSong.setText(items[position]);
            return view;
        }
    }
}



