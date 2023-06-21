package com.example.am_anime_list;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.am_anime_list.adapter.AnimeSearchAdapter;
import com.example.am_anime_list.model.Anime;
import com.example.am_anime_list.service.AnimeService;

import java.util.List;

public class SearchingActivity extends AppCompatActivity {
    private AnimeService animeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);
        ListView listView = findViewById(R.id.resultList);
        EditText animeTitle = findViewById(R.id.editTextAnimeTitle);
        Context context = this;
        animeService = new AnimeService();
        animeService.getAnimeFromTitle("pokemon", 0, 10, new AnimeService.AnimeCallback() {
            @Override
            public void onSuccess(List<Anime> animeList) {
                AnimeSearchAdapter animeSearchAdapter = new AnimeSearchAdapter(context, animeList);
                listView.setAdapter(animeSearchAdapter);
            }

            @Override
            public void onFailure(Throwable t) {
//                Toast.makeText(getApplicationContext(), "Searching error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}