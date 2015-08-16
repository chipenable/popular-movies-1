package ru.chipenable.popularmovies.view;

import android.app.ActionBar;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.client.MovieClient;
import ru.chipenable.popularmovies.model.Command;
import ru.chipenable.popularmovies.model.moviedetail.Genre;
import ru.chipenable.popularmovies.model.moviedetail.MovieDetail;

/**
 * Created by Pashgan on 07.07.2015.
 */
public class DetailFragment extends BaseFragment {

    public static final String TAG = "DetailFragment";
    private static final String MOVIE_ID = "movie_id";

    //view components
    private TextView mTitle;
    private TextView mDate;
    private TextView mDuration;
    private TextView mRating;
    private ImageView mPoster;
    private TextView mPlot;
    private TextView mGenre;

    private long mMovieId;
    private boolean mFailureFlag;
    private MovieDetail mMovieDetail;

    public static DetailFragment newInstance(long movieId) {
        Bundle bundle = new Bundle();
        bundle.putLong(MOVIE_ID, movieId);
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(bundle);
        return detailFragment;
    }

    @Override
    public void updateData(boolean connectionState) {
        if (connectionState && mFailureFlag) {
            downloadMovieDetails();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFailureFlag = false;
        mMovieDetail = null;

        Bundle bundle = getArguments();
        if (bundle != null) {
            mMovieId = bundle.getLong(MOVIE_ID);
        }

        Log.d(TAG, "movie id: " + Long.toString(mMovieId));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        mTitle = (TextView) view.findViewById(R.id.title);
        mDate  = (TextView) view.findViewById(R.id.date);
        mDuration = (TextView) view.findViewById(R.id.duration);
        mRating = (TextView) view.findViewById(R.id.rating);
        mPoster = (ImageView) view.findViewById(R.id.poster);
        mPlot   = (TextView) view.findViewById(R.id.plot);
        mGenre  = (TextView) view.findViewById(R.id.genre);

        Point screenSize = getScreenSize();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)mPoster.getLayoutParams();
        layoutParams.height = screenSize.y/getResources().getInteger(R.integer.poster_div);
        mPoster.setLayoutParams(layoutParams);

        downloadMovieDetails();
        return view;
    }

    private void downloadMovieDetails() {

        if (mMovieDetail != null){
            displayDetails(mMovieDetail);
            return;
        }

        mCallback.fragmentCallback(Command.START_DOWNLOADING, 0);
        mClient.getMovieDetail(mMovieId, new retrofit.Callback<MovieDetail>() {
            @Override
            public void success(MovieDetail movieDetail, Response response) {
                Log.d(TAG, "url: " + response.getUrl());
                mMovieDetail = movieDetail;
                displayDetails(movieDetail);
                mCallback.fragmentCallback(Command.STOP_DOWNLOADING, 0);
                mFailureFlag = false;
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                Log.d(TAG, error.getMessage());
                mCallback.fragmentCallback(Command.STOP_DOWNLOADING, 0);
                mFailureFlag = true;
            }
        });
    }

    private void displayDetails(MovieDetail movieDetail) {
        if (movieDetail == null || getActivity() == null) {
            return;
        }

        mTitle.setText(movieDetail.getTitle());
        mDate.setText(movieDetail.getReleaseDate());

        String duration = Integer.toString(movieDetail.getRuntime())
                + " " + getString(R.string.min);
        mDuration.setText(duration);

        String rating = Double.toString(movieDetail.getVoteAverage()) + "/10.0";
        mRating.setText(rating);

        String posterUrl = MovieClient.BASE_IMG_PATH_300 + movieDetail.getPosterPath();
        Picasso.with(getActivity())
                .load(posterUrl)
                .error(R.drawable.noposter)
                .into(mPoster);

        mPlot.setText(movieDetail.getOverview());

        String movieGenres = movieDetail.genresToStr();
        if (movieGenres != null && (!movieGenres.equals(""))) {
            String genres = getString(R.string.genre) + " " + movieGenres;
            mGenre.setText(genres);
        }
    }


}
