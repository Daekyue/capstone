package com.akj.sns_project;

import java.io.Serializable;
import java.util.List;

public class Movie {
    int[] genre_ids;
    int id;
    String adult;
    String backdrop_path;
    public String title;
    String original_language;
    String original_title;
    String overview;
    public String poster_path;
    String media_type;
    String release_date;
    float  popularity;
    boolean video;
    float vote_average, vote_count;


    public String GetTitle()
    {
        return title;
    }

    public String GetPosterPath()
    {
        return  "https://image.tmdb.org/t/p/w500" + poster_path;
    }
}
