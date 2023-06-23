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

        animeTitle.setOnEditorActionListener((v, actionId, event) ->
                setupSearchAnime(actionId, animeTitle));

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem,
                                 int visibleItemCount,
                                 int totalItemCount) {
                if (totalItemCount > 0 && firstVisibleItem + visibleItemCount == totalItemCount)
                    addAnimeToList();
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) ->
                setupRemoveAnime(position));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Anime selectedAnime = animeList.get(position);
            View popupView = createPopupView();
            PopupWindow popupWindow = createPopupWindow(popupView);
            setupStatusSpinner(popupView, selectedAnime);
            setupScoreSpinner(popupView, selectedAnime);
            setupProgressTextView(popupView, selectedAnime);
            setupAddProgressButton(popupView, selectedAnime);
            setupRemoveProgressButton(popupView);
            setupEditAnimeButton(popupView, popupWindow, selectedAnime);
            showPopupWindow(view, popupWindow);
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

    private boolean setupSearchAnime(int actionId, EditText animeTitle) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
            offset = 0;
            noMoreAnime = false;
            animeList.clear();
            lastSearchedAnimeTitle = animeTitle.getText().toString();
            addAnimeToList();
            return true;
        }
        return false;
    }

    private boolean setupRemoveAnime(int position) {
        sqLiteManager.deleteAnime(animeList.get(position).getId());
        Toast.makeText(getApplicationContext(),
                "Removed anime from your list!",
                Toast.LENGTH_SHORT).show();
        animeList.remove(position);
        animeSearchAdapter.notifyDataSetChanged();
        return true;
    }

    @SuppressLint("InflateParams")
    private View createPopupView() {
        return getLayoutInflater().inflate(R.layout.edit_anime_popup, null);
    }

    private PopupWindow createPopupWindow(View popupView) {
        return new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void setupStatusSpinner(View popupView, Anime anime) {
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
            if (optionsStatus[i].equals(anime.getStatus().getString())) {
                defaultSelectionIndex = i;
                break;
            }
        }
        spinnerStatus.setSelection(defaultSelectionIndex);
    }

    private void setupScoreSpinner(View popupView, Anime anime) {
        Spinner spinnerScore = popupView.findViewById(R.id.spinnerAnimeScore);
        String[] optionsScore = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter<String> spinnerScoreAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                optionsScore);
        spinnerScore.setAdapter(spinnerScoreAdapter);
        spinnerScore.setSelection(Integer.parseInt(anime.getMean()));
    }

    @SuppressLint("SetTextI18n")
    private void setupProgressTextView(View popupView, Anime anime) {
        TextView episodesView = popupView.findViewById(R.id.animeProgress);
        episodesView.setText(anime.getProgress().toString());
    }

    private void setupAddProgressButton(View popupView, Anime anime) {
        popupView.findViewById(R.id.addProgress).setOnClickListener(v -> {
            TextView episodesView = popupView.findViewById(R.id.animeProgress);
            int progress = Integer.parseInt(String.valueOf(episodesView.getText()));
            if (progress < Integer.parseInt(anime.getNumEpisodes())) {
                episodesView.setText(String.valueOf(progress + 1));
            }
        });
    }

    private void setupRemoveProgressButton(View popupView) {
        popupView.findViewById(R.id.removeProgress).setOnClickListener(v -> {
            TextView episodesView = popupView.findViewById(R.id.animeProgress);
            int progress = Integer.parseInt(String.valueOf(episodesView.getText()));
            if (progress > 0) {
                episodesView.setText(String.valueOf(progress - 1));
            }
        });
    }

    private void setupEditAnimeButton(View popupView, PopupWindow popupWindow, Anime anime) {
        popupView.findViewById(R.id.editAnimeButton).setOnClickListener(v -> {
            TextView episodesView = popupView.findViewById(R.id.animeProgress);
            Spinner spinnerScore = popupView.findViewById(R.id.spinnerAnimeScore);
            Spinner spinnerStatus = popupView.findViewById(R.id.spinnerAnimeStatus);

            anime.setProgress(Integer.parseInt(episodesView.getText().toString()));
            anime.setMean(spinnerScore.getSelectedItem().toString());
            anime.setStatus(Status.fromString(spinnerStatus.getSelectedItem().toString()));

            sqLiteManager.updateAnime(anime);
            animeSearchAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(),
                    "Data edited",
                    Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });
    }

    private void showPopupWindow(View anchorView, PopupWindow popupWindow) {
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setElevation(10f);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }
}