package com.example.am_anime_list;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.am_anime_list.adapter.AnimeSearchAdapter;
import com.example.am_anime_list.database.SQLiteManager;
import com.example.am_anime_list.model.Anime;
import com.example.am_anime_list.model.Status;

import java.util.ArrayList;
import java.util.List;

public class DatabaseActivity extends AppCompatActivity {
    private int offset;
    private final List<Anime> animeList = new ArrayList<>();
    private String lastSearchedAnimeTitle = "";
    private boolean loadingMore = false, noMoreAnime = false;
    private AnimeSearchAdapter animeSearchAdapter;
    private SQLiteManager sqLiteManager;

    @SuppressLint("SetTextI18n")
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

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Anime selectedAnime = animeList.get(position);
            @SuppressLint("InflateParams") View popupView = getLayoutInflater().
                    inflate(R.layout.edit_anime_popup, null);
            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            Spinner spinnerStatus = popupView.findViewById(R.id.spinnerAnimeStatus);
            String[] optionsStatus = {Status.watching.getString(),
                    Status.completed.getString(),
                    Status.on_hold.getString(),
                    Status.dropped.getString(),
                    Status.plan_to_watch.getString()};
            ArrayAdapter<String> spinnerStatusAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    optionsStatus);
            spinnerStatus.setAdapter(spinnerStatusAdapter);
            int defaultSelectionIndex = -1;
            for (int i = 0; i < optionsStatus.length; i++) {
                if (optionsStatus[i].equals(selectedAnime.getStatus().getString())) {
                    defaultSelectionIndex = i;
                    break;
                }
            }
            TextView episodesView = popupView.findViewById(R.id.animeProgress);
            episodesView.setText(selectedAnime.getProgress().toString());
            popupView.findViewById(R.id.addProgress).setOnClickListener(v -> {
                int progress = Integer.parseInt(String.valueOf(episodesView.getText()));
                if (progress < Integer.parseInt(selectedAnime.getNumEpisodes())) {
                    episodesView.setText(String.valueOf(progress + 1));
                }
            });
            popupView.findViewById(R.id.removeProgress).setOnClickListener(v -> {
                int progress = Integer.parseInt(String.valueOf(episodesView.getText()));
                if (progress > 0) {
                    episodesView.setText(String.valueOf(progress - 1));
                }
            });
            spinnerStatus.setSelection(defaultSelectionIndex);
            Spinner spinnerScore = popupView.findViewById(R.id.spinnerAnimeScore);
            String[] optionsScore = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
            ArrayAdapter<String> spinnerScoreAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    optionsScore);
            spinnerScore.setAdapter(spinnerScoreAdapter);
            spinnerScore.setSelection(Integer.parseInt(selectedAnime.getMean()));
            popupView.findViewById(R.id.editAnimeButton).setOnClickListener(v -> {
                selectedAnime.setProgress(Integer.parseInt(episodesView.getText().toString()));
                selectedAnime.setMean(spinnerScore.getSelectedItem().toString());
                selectedAnime.setStatus(Status.fromString(spinnerStatus.getSelectedItem().toString()));
                sqLiteManager.updateAnime(selectedAnime);
                animeSearchAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(),
                        "Data edited",
                        Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            popupWindow.setElevation(10f);
            popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
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