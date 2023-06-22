package com.example.am_anime_list;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.am_anime_list.adapter.AnimeSearchAdapter;
import com.example.am_anime_list.model.Anime;
import com.example.am_anime_list.service.AnimeService;

import java.util.ArrayList;
import java.util.List;

public class SearchingActivity extends AppCompatActivity {
    private String lastSearchedAnimeTitle;
    private final AnimeService animeService = new AnimeService();
    private int offset;
    private final List<Anime> animeList = new ArrayList<>();
    private AnimeSearchAdapter animeSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);
        animeSearchAdapter = new AnimeSearchAdapter(this, animeList);
        EditText animeTitle = findViewById(R.id.editTextAnimeTitle);
        ListView listView = findViewById(R.id.resultList);
        listView.setAdapter(animeSearchAdapter);
        animeTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                offset = 0;
                animeList.clear();
                lastSearchedAnimeTitle = animeTitle.getText().toString();
                addAnimeToList();
                return true;
            }
            return false;
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
                    addAnimeToList();
                }
            }
        });

    }

    private void addAnimeToList() {
        animeService.getAnimeFromTitle(lastSearchedAnimeTitle, offset, 10, new AnimeService.AnimeCallback() {
            @Override
            public void onSuccess(List<Anime> resultAnimeList) {
                animeList.addAll(resultAnimeList);
                animeSearchAdapter.notifyDataSetChanged();
                offset += 10;
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Nothing to load!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}