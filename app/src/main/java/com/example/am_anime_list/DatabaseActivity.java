package com.example.am_anime_list;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.am_anime_list.adapter.AnimeSearchAdapter;
import com.example.am_anime_list.database.SQLiteManager;
import com.example.am_anime_list.model.Anime;

import java.util.ArrayList;
import java.util.List;

public class DatabaseActivity extends AppCompatActivity {
    private int offset;
    private final List<Anime> animeList = new ArrayList<>();
    private String lastSearchedAnimeTitle = "";
    private boolean loadingMore = false, noMoreAnime = false;
    private AnimeSearchAdapter animeSearchAdapter;
    private SQLiteManager sqLiteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        animeSearchAdapter = new AnimeSearchAdapter(this, animeList);
        EditText animeTitle = findViewById(R.id.editTextDatabaseTitle);
        ListView listView = findViewById(R.id.databaseResultList);
        listView.setAdapter(animeSearchAdapter);
        Intent intent = new Intent(this, SearchingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        findViewById(R.id.switchToSearchingButton).setOnClickListener(view ->
                startActivity(intent));

        animeTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                offset = 0;
                noMoreAnime = false;
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

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            sqLiteManager.deleteAnime(animeList.get(position).getId());
            Toast.makeText(getApplicationContext(),
                        "Removed anime from your list!",
                        Toast.LENGTH_SHORT).show();
            animeList.remove(position);
            animeSearchAdapter.notifyDataSetChanged();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        offset = 0;
        noMoreAnime = false;
        animeList.clear();
        addAnimeToList();
    }

    private void addAnimeToList() {
        if (loadingMore || noMoreAnime) return;
        loadingMore = true;
        List<Anime> resultList = sqLiteManager.getAnimeListArray(lastSearchedAnimeTitle,10, offset);
        if (resultList.size() == 0) {
            noMoreAnime = true;
        } else {
            animeList.addAll(resultList);
            offset += 10;
        }
        animeSearchAdapter.notifyDataSetChanged();
        loadingMore = false;
    }
}