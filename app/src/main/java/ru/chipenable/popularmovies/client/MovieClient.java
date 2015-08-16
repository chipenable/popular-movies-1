package ru.chipenable.popularmovies.client;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import ru.chipenable.popularmovies.model.moviedetail.MovieDetail;
import ru.chipenable.popularmovies.model.movielist.MovieList;

/**
 * Created by Pashgan on 07.07.2015.
 */
public interface MovieClient {

    /*You must add your value of the key !!!!!*/
    String KEY = "";

    String ENDPOINT = "http://api.themoviedb.org/3";
    String REQ_DISCOVER = "/discover/movie";
    String REQ_MOVIES = "/movie/{id}";

    String ID = "id";
    String BASE_IMG_PATH = "http://image.tmdb.org/t/p/w185/";
    String BASE_IMG_PATH_300 = "http://image.tmdb.org/t/p/w300/";
    String API_KEY = "?api_key=" + KEY;

    String PAR_PAGE = "page";
    String PAR_SORT_BY = "sort_by";
    String POPULARITY = "popularity.desc";
    String VOTE_AVERAGE = "vote_average.desc";


    @GET(REQ_DISCOVER + API_KEY)
    void getMovieList(@Query(PAR_SORT_BY) String par, @Query(PAR_PAGE) int page, Callback<MovieList> obj);

    @GET(REQ_MOVIES + API_KEY)
    void getMovieDetail(@Path(ID) long id, Callback<MovieDetail> obj);
}