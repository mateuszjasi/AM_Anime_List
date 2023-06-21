package com.example.am_anime_list.model;

import com.google.gson.annotations.SerializedName;

public class AnimeDetails {
    private int id;
    private String title;
    @SerializedName("main_picture")
    private MainPicture mainPicture;
    private double mean;
    private String status;
    @SerializedName("num_episodes")
    private int numEpisodes;

    public class MainPicture {
        private String medium;
        private String large;

        public String getMedium() {
            return medium;
        }

        public String getLarge() {
            return large;
        }
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public MainPicture getMainPicture() {
        return mainPicture;
    }

    public double getMean() {
        return mean;
    }

    public String getStatus() {
        return status;
    }

    public int getNumEpisodes() {
        return numEpisodes;
    }
}