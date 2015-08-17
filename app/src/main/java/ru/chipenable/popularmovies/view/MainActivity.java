package ru.chipenable.popularmovies.view;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.client.MovieClient;
import ru.chipenable.popularmovies.model.Command;

/*  At first you must add API_KEY !!!!
 *  See MovieClient.java
 *
 *  p.s.: Sorry for my English.
 *  */

public class MainActivity extends Activity implements BaseFragment.FragmentCallback{

    public final static String PREF_TAG = "pref_tag";

    /*When I adapt my project for tablets both MainFragment and
    DetailFragment will use the same progress bar*/
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (MovieClient.KEY.equals("")){
            Toast.makeText(this, R.string.no_key, Toast.LENGTH_LONG).show();
            return;
        }

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_frame, new MainFragment())
                    .commit();
        }
    }

    /*Fragment's callback function. It is used to communicate with Activity:
    * - to control the progress bar
    * - to start DetailActivity*/
    @Override
    public void fragmentCallback(int command, long arg) {

        switch(command){
            case Command.START_DOWNLOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                break;

            case Command.STOP_DOWNLOADING:
                mProgressBar.setVisibility(View.GONE);
                break;

            case Command.SHOW_DETAIL:
                Intent intent = DetailActivity.makeIntent(MainActivity.this, arg);
                startActivity(intent);
                break;
        }
    }

}
