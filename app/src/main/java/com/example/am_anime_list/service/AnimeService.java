package com.example.am_anime_list.service;

import androidx.annotation.NonNull;

import com.example.am_anime_list.R;
import com.example.am_anime_list.model.Anime;
import com.example.am_anime_list.model.AnimeDetails;
import com.example.am_anime_list.model.AnimeSearchResponse;
import com.example.am_anime_list.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AnimeService {
    private final AnimeApi animeApi;
    private final String apiValue = "dc7b2de23341fdd30dbea0949bf6c5e1";

    public AnimeService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.myanimelist.net/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build();

        animeApi = retrofit.create(AnimeApi.class);
    }

    public void getAnimeFromTitle(String title,
                                  int offset,
                                  int limit,
                                  final AnimeCallback callback) {
        Call<AnimeSearchResponse> call = animeApi.getAnimeFromTitle(apiValue, title, offset, limit);
        call.enqueue(new Callback<AnimeSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<AnimeSearchResponse> call,
                                   @NonNull retrofit2.Response<AnimeSearchResponse> response) {
                if (response.isSuccessful()) {
                    AnimeSearchResponse animeSearchResponse = response.body();
                    if (animeSearchResponse != null) {
                        List<Integer> animeIds = animeSearchResponse.getAnimeIds();
                        getAnimeDetails(animeIds, callback);
                    } else {
                        callback.onFailure(new Exception("Empty response body"));
                    }
                } else {
                    callback.onFailure(new Exception("Request failed"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AnimeSearchResponse> call, @NonNull Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private void getAnimeDetails(List<Integer> animeIds, final AnimeCallback callback) {
        List<Call<AnimeDetails>> callList = new ArrayList<>();
        for (Integer id : animeIds) {
            Call<AnimeDetails> call = animeApi.getAnimeDetails(apiValue, id);
            callList.add(call);
        }
        List<Anime> animeList = new ArrayList<>();

        AtomicInteger responseCount = new AtomicInteger(callList.size());

        for (Call<AnimeDetails> call : callList) {
            call.enqueue(new Callback<AnimeDetails>() {
                @Override
                public void onResponse(@NonNull Call<AnimeDetails> call,
                                       @NonNull retrofit2.Response<AnimeDetails> response) {
                    if (response.isSuccessful()) {
                        AnimeDetails animeDetails = response.body();
                        if (animeDetails != null) {
                            Anime anime = new Anime();
                            anime.setId(animeDetails.getId());
                            anime.setTitle(animeDetails.getTitle());
                            anime.setImageUrl(animeDetails.getMainPicture().getLarge() == null ?
                                    String.valueOf(R.string.SampleImage) :
                                    animeDetails.getMainPicture().getLarge());
                            anime.setMean(animeDetails.getMean() != 0 ?
                                    String.valueOf(animeDetails.getMean()) : "N/A");
                            anime.setStatus(Status.valueOf(animeDetails.getStatus()));
                            anime.setNumEpisodes(animeDetails.getNumEpisodes() != 0 ?
                                    String.valueOf(animeDetails.getNumEpisodes()) : "?");

                            animeList.add(anime);
                        }
                    }

                    if (responseCount.decrementAndGet() == 0) {
                        callback.onSuccess(animeList);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AnimeDetails> call, @NonNull Throwable t) {
                    callback.onFailure(t);
                }
            });
        }
    }

    public interface AnimeCallback {
        void onSuccess(List<Anime> animeList);
        void onFailure(Throwable t);
    }
}
