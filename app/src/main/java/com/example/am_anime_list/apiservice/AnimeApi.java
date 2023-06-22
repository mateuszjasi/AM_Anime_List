package com.example.am_anime_list.apiservice;

import com.example.am_anime_list.model.AnimeDetails;
import com.example.am_anime_list.model.AnimeSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AnimeApi {
    @GET("anime")
    Call<AnimeSearchResponse> getAnimeFromTitle(
            @Header("X-MAL-CLIENT-ID") String apiKey,
            @Query("q") String title,
            @Query("offset") int offset,
            @Query("limit") int limit
    );

    @GET("anime/{id}?fields=id,title,main_picture,mean,status,num_episodes")
    Call<AnimeDetails> getAnimeDetails(
            @Header("X-MAL-CLIENT-ID") String apiKey,
            @Path("id") int id
    );
}
