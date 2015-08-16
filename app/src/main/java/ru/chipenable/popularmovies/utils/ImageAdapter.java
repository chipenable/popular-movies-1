package ru.chipenable.popularmovies.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.client.MovieClient;
import ru.chipenable.popularmovies.model.movielist.Result;

/**
 * ImageAdapter is used to show movie posters in GridView
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private List<Result> mMovieList;
    private Integer[] mThumbIds;
    private int mWidth;
    private int mHeight;
    private EndListListener mEndListener;


    public interface EndListListener{
        void downloadNewData();
    }

    /**
     * width - width of an ImageView
     * height - height of an ImageView
     * */
    public ImageAdapter(Context c, List<Result> list, int width, int height) {
        mContext = c;
        mMovieList = list;
        mWidth = width;
        mHeight = height;
     }

    public void setEndListListener(EndListListener l){
        mEndListener = l;
    }

    public int getCount() {
        return mMovieList.size();
    }

    public Object getItem(int position) {
        return mMovieList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        /*get ImageView reference*/
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(mWidth, mHeight));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else {
            imageView = (ImageView) convertView;
        }

        /*get Url of a poster*/
        Result movie = mMovieList.get(position);
        String url = MovieClient.BASE_IMG_PATH + movie.getPosterPath();

        /*Show poster by using Picasso library*/
        Picasso.with(mContext)
                .load(url)
                .fit()
                .error(R.drawable.noposter)
                .into(imageView);

        /*maybe it isn't the best solution to add data to GridView*/
        if ((position + 1) == getCount()){
            if (mEndListener != null){
                mEndListener.downloadNewData();
            }
        }

        return imageView;
    }

}
