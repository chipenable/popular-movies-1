package ru.chipenable.popularmovies.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.chipenable.popularmovies.connection.ConnectionStateReceiver;
import ru.chipenable.popularmovies.model.Command;
import ru.chipenable.popularmovies.utils.ImageAdapter;
import ru.chipenable.popularmovies.client.MovieClient;
import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.model.movielist.MovieList;
import ru.chipenable.popularmovies.model.movielist.Result;

/* This fragment shows GridView with movie posters
*
* */
public class MainFragment extends BaseFragment implements ImageAdapter.EndListListener {

    public final static String TAG = "MainFragment";

    private final static String PREF_TYPE_SORT = "type_sort";
    private final static int SORT_BY_POPULARITY = 0;
    private final static int SORT_BY_RATING = 1;
    private static final int MAX_AMOUNT_PAGES = 1000;

    private GridView mGridView;
    private ImageAdapter mAdapter;
    private List<Result> mResultList;
    private boolean mFailureFlag;          //The flag will be set "true" if a data isn't downloaded
    private boolean mStartupFlag;          //The flag will be set "true" when fragment works first time
    private int mSort;
    private boolean mConnectionState;      //The flag is true if the Internet is available
    private int mCurPage;
    private HashMap<Integer, PageState> mPagesList;

    enum PageState{DOWNLOADING, DONE}

    //***************************** fragment methods ***************************************//

    /*Receiver's callback function. It'll be called when a state of a network is changed*/
    @Override
    public void updateData(boolean connectionState) {
        Log.d(TAG, "connection state: " + connectionState);

        mConnectionState = connectionState;
        if (connectionState && mFailureFlag) {
            downloadMovies(mCurPage, mSort);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFailureFlag = false;
        mStartupFlag = false;
        mResultList = new ArrayList<>();
        mPagesList = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) view.findViewById(R.id.movie_grid_view);

        /*It calculates a size of the ImageView for the GridView*/
        Point screenSize = getScreenSize();
        int imageWith = screenSize.x/getResources().getInteger(R.integer.num_col);
        int imageHeight = screenSize.y/getResources().getInteger(R.integer.num_row);

        mAdapter = new ImageAdapter(getActivity(), mResultList, imageWith, imageHeight);
        mAdapter.setEndListListener(this);
        mGridView.setAdapter(mAdapter);

        /*it handles user clicks and start DetailActivity/DetailFragment*/
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Result result = (Result) parent.getAdapter().getItem(position);
                mCallback.fragmentCallback(Command.SHOW_DETAIL, result.getId());
            }
        });

        mConnectionState = ConnectionStateReceiver.checkConnection(getActivity());

        /*It downloads a first page of movie data*/
        if (!mStartupFlag){
            mCurPage = 1;
            mSort = getTypeSort();
            mStartupFlag = true;
            downloadMovies(mCurPage, mSort);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_sort_by_popularity:
                if (saveTypeSort(SORT_BY_POPULARITY)){
                    DownloadFirstPage();
                }
                return true;

            case R.id.action_sort_by_rating:
                if (saveTypeSort(SORT_BY_RATING)) {
                    DownloadFirstPage();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*if a user changes the type of sort a first page will
    be downloaded from the internet service*/
    private void DownloadFirstPage() {
        mCurPage = 1;
        mPagesList.clear();
        mResultList.clear();
        mAdapter.notifyDataSetChanged();
        downloadMovies(mCurPage, mSort);
    }

    /*Callback function that is involved by ImageAdapter*/
    @Override
    public void downloadNewData() {
        /*if the Internet is available it'll download a next page*/
        if (mConnectionState) {
            mCurPage++;
            downloadMovies(mCurPage, mSort);
            Log.d(TAG, "I'm loading page: " + Integer.toString(mCurPage));
        }
        else{
            /*if the Internet isn't available it'll set the failure flag and show the message*/
            if (!mFailureFlag){
                mCurPage++;
                Toast.makeText(getActivity(), getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
            }
            mFailureFlag = true;
        }
    }

    //***************************** user methods *******************************************//

    /*If typeSort is different from mSort - save and return true*/
    private boolean saveTypeSort(int typeSort){
        if (mSort != typeSort){
            mSort = typeSort;
            SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREF_TAG, Context.MODE_PRIVATE);
            sharedPref.edit().putInt(PREF_TYPE_SORT, typeSort).apply();
            return true;
        }
        return false;
    }

    private int getTypeSort(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREF_TAG, Context.MODE_PRIVATE);
        return sharedPref.getInt(PREF_TYPE_SORT, SORT_BY_POPULARITY);
    }

    /*It downloads movie data from the internet service.
    * At first the function checks its parameters and then it sends a request by Retrofit */
    private void downloadMovies(final int page, int typeSort){

        if (page >= MAX_AMOUNT_PAGES){
            return;
        }

        PageState pageState = mPagesList.get(page);
        if ((pageState == PageState.DONE) || (pageState == PageState.DOWNLOADING)){
            Log.d(TAG, "This page has already been loaded");
            return;
        }

        if (!mConnectionState){
            Toast.makeText(getActivity(), getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
            mFailureFlag = true;
            return;
        }

        String sortPar;
        if (typeSort == SORT_BY_POPULARITY){
            sortPar = MovieClient.POPULARITY;
        }
        else if (typeSort == SORT_BY_RATING){
            sortPar = MovieClient.VOTE_AVERAGE;
        }
        else{
            return;
        }

        Log.d(TAG, "The page is downloading");
        mPagesList.put(page, PageState.DOWNLOADING);
        mCallback.fragmentCallback(Command.START_DOWNLOADING, 0);
        mClient.getMovieList(sortPar, page, new retrofit.Callback<MovieList>() {

            @Override
            public void success(MovieList movieObj, Response response) {
                if (movieObj != null) {
                    mPagesList.put(page, PageState.DONE);
                    Log.d(TAG, "The page is done");
                    mResultList.addAll(movieObj.getResults());
                    if (mAdapter != null){
                        mAdapter.notifyDataSetChanged();
                    }
                }

                mCallback.fragmentCallback(Command.STOP_DOWNLOADING, 0);
                Log.d(TAG, response.getHeaders().toString());
            }

            @Override
            public void failure(RetrofitError error) {
                mPagesList.remove(page);
                mCallback.fragmentCallback(Command.STOP_DOWNLOADING, 0);
                mFailureFlag = true;
                Log.d(TAG, "retrofit error: " + error.getMessage());
            }
        });
    }


}
