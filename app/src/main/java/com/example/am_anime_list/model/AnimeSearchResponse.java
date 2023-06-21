package com.example.am_anime_list.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AnimeSearchResponse {
    @SerializedName("data")
    private List<AnimeData> animeData;

    public List<AnimeData> getAnimeData() {
        return animeData;
    }

    public List<Integer> getAnimeIds() {
        List<Integer> animeIds = new ArrayList<>();
        if (animeData != null) {
            for (AnimeData animeData : animeData) {
                Node node = animeData.getNode();
                if (node != null) {
                    animeIds.add(node.getId());
                }
            }
        }
        return animeIds;
    }
}

class AnimeData {
    private Node node;

    public Node getNode() {
        return node;
    }
}

class Node {
    private int id;

    public int getId() {
        return id;
    }
}
