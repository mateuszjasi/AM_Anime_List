package com.example.am_anime_list;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.am_anime_list.adapter.AnimeSearchAdapter;
import com.example.am_anime_list.database.SQLiteManager;
import com.example.am_anime_list.model.Anime;
import com.example.am_anime_list.apiservice.AnimeService;

import java.util.ArrayList;
import java.util.List;

public class SearchingActivity extends AppCompatActivity {
    private String lastSearchedAnimeTitle;
    private final AnimeService animeService = new AnimeService();
    private int offset;
    private final List<Anime> animeList = new ArrayList<>();
    private AnimeSearchAdapter animeSearchAdapter;
    private boolean loadingMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        animeSearchAdapter = new AnimeSearchAdapter(this, animeList);
        EditText animeTitle = findViewById(R.id.editTextAnimeTitle);
        ListView listView = findViewById(R.id.resultList);
        listView.setAdapter(animeSearchAdapter);
        Intent intent = new Intent(this, DatabaseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        findViewById(R.id.switchViewButton).setOnClickListener(view ->
                startActivity(intent));

        animeTitle.setOnEditorActionListener((v, actionId, event) ->
                setupSearchAnime(actionId, animeTitle));

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

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (sqLiteManager.checkID(animeList.get(position).getId())) {
                Toast.makeText(getApplicationContext(),
                        "Anime is already in your list!",
                        Toast.LENGTH_SHORT).show();
            } else {
                sqLiteManager.addAnimeToDatabase(animeList.get(position));
                Toast.makeText(getApplicationContext(),
                        "Anime added to your list!",
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void addAnimeToList() {
        if (loadingMore) return;
        loadingMore = true;
        animeService.getAnimeFromTitle(lastSearchedAnimeTitle,
                offset,
                10,
                new AnimeService.AnimeCallback() {
            @Override
            public void onSuccess(List<Anime> resultAnimeList) {
                offset += 10;
                animeList.addAll(resultAnimeList);
                animeSearchAdapter.notifyDataSetChanged();
                loadingMore = false;
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Nothing to load!", Toast.LENGTH_SHORT).show();
                animeSearchAdapter.notifyDataSetChanged();
                loadingMore = false;
            }
        });
    }

    private boolean setupSearchAnime(int actionId, EditText animeTitle) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
            offset = 0;
            animeList.clear();
            lastSearchedAnimeTitle = animeTitle.getText().toString();
            addAnimeToList();
            return true;
        }
        return false;
    }
}