package com.example.am_anime_list.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.am_anime_list.R;
import com.example.am_anime_list.model.Anime;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AnimeSearchAdapter extends BaseAdapter {
    private final List<Anime> animeList;
    private final LayoutInflater layoutInflater;

    public AnimeSearchAdapter(Context context, List<Anime> animeList) {
        this.animeList = animeList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return animeList.size();
    }

    @Override
    public Object getItem(int position) {
        return animeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return animeList.get(position).getId();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(
                R.layout.anime_search_row,
                parent,
                false);
        view.setMinimumHeight(200);
        TextView title = view.findViewById(R.id.animeTitle);
        TextView episodes = view.findViewById(R.id.animeEpisodes);
        TextView score = view.findViewById(R.id.animeScore);
        TextView status = view.findViewById(R.id.animeStatus);
        ImageView image = view.findViewById(R.id.animeImageView);
        title.setText(animeList.get(position).getTitle());
        if (animeList.get(position).getProgress() == -1) {
            episodes.setText(animeList.get(position).getNumEpisodes());
        } else {
            episodes.setText(animeList.get(position).getProgress() +
                    " / " +
                    animeList.get(position).getNumEpisodes());
        }
        score.setText(animeList.get(position).getMean());
        status.setText(animeList.get(position).getStatus().getString());
        Picasso.get()
                .load(animeList.get(position).getImageUrl())
                .resize(image.getWidth(), 500)
                .into(image);
        return view;
    }
}
