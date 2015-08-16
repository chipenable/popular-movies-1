package ru.chipenable.popularmovies.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;

import retrofit.RestAdapter;
import ru.chipenable.popularmovies.client.MovieClient;
import ru.chipenable.popularmovies.connection.ConnectionStateReceiver;

/**
 * BaseFragment. It implements several common functions for MainFragment and DetailFragment
 */
public abstract class BaseFragment  extends Fragment implements ConnectionStateReceiver.ConnectionListener{

    protected static final String TAG = "BaseFragment";

    protected FragmentCallback mCallback;
    protected MovieClient mClient;
    protected ConnectionStateReceiver mReceiver;

    /*interface to communicate with Activity*/
    public interface FragmentCallback {
        void fragmentCallback(int command, long arg);
    }

    private static FragmentCallback mDummyCallback = new FragmentCallback() {
        @Override
        public void fragmentCallback(int command, long arg) {}
    };

    public abstract void updateData(boolean connectionState);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");

        /*attach callback function*/
        try {
            mCallback = (FragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FragmentCallback");
        }

        /*Retrofit web client*/
        if (mClient == null){
            mClient = new RestAdapter.Builder()
                    .setEndpoint(MovieClient.ENDPOINT)
                    .build()
                    .create(MovieClient.class);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();

        /*register receiver to control the network state*/
        mReceiver = new ConnectionStateReceiver(BaseFragment.this);
        getActivity().registerReceiver(mReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();

        /*uregister receiver*/
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = mDummyCallback;
        Log.d(TAG, "onDetach");
    }

    /*This function is used to calculate image sizes*/
    public Point getScreenSize(){

        /*get full size of the display*/
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(screenSize);
        } else {
            display.getSize(screenSize);
        }

        /*get height of StatusBar*/
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = (resourceId > 0)? getResources().getDimensionPixelSize(resourceId) : 0;

        //it doesn't work. I don't know how to get a height of ActionBar
        int actionBarHeight = getActivity().getActionBar().getHeight();

        /*calculate available screen size*/
        screenSize.y -= (statusBarHeight + actionBarHeight);
        return screenSize;
    }
}
