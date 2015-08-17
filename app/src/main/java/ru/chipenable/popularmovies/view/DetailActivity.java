package ru.chipenable.popularmovies.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.model.Command;

public class DetailActivity extends Activity implements BaseFragment.FragmentCallback{

    public static final String TAG = "DetailActivity";
    private static final String MOVIE_ID = "movie_id";

    @Bind(R.id.progress_bar) ProgressBar mProgressBar;

    public static Intent makeIntent(Context context, long movieId){
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(MOVIE_ID, movieId);
        return intent;
    }

    private long getMovieId(){
        long id = 0;
        Intent intent = getIntent();
        if (intent != null){
            id = intent.getLongExtra(MOVIE_ID, 0);
        }
        return id;
    }

    /**********************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            DetailFragment fragment = DetailFragment.newInstance(getMovieId());
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_frame, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void fragmentCallback(int command, long arg) {
        switch(command) {
            case Command.START_DOWNLOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                break;

            case Command.STOP_DOWNLOADING:
                mProgressBar.setVisibility(View.GONE);
                break;
            default:
        }
    }
}
