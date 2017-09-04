package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by ukanth on 3/9/17.
 */

public class IconPreviewActivity extends AppCompatActivity {


    private MaterialDialog plsWait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Prefs.isDarkTheme(getApplicationContext())) {
            setTheme(R.style.AppTheme_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.iconpreview);


        Bundle bundle = getIntent().getExtras();
        String pkgName = bundle.getString("pkg");

        IconsPreviewLoader previewLoader = new IconsPreviewLoader(IconPreviewActivity.this, pkgName);

        if (plsWait == null && (previewLoader.getStatus() == AsyncTask.Status.PENDING ||
                previewLoader.getStatus() == AsyncTask.Status.FINISHED)) {
            previewLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class IconsPreviewLoader extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private String packageName;
        private Set<Icon> icons;

        private IconsPreviewLoader(Context context, String packageName) {
            this.mContext = context;
            this.packageName = packageName;
        }

        @Override
        protected void onPreExecute() {
            plsWait = new MaterialDialog.Builder(mContext).cancelable(false).title(mContext.getString(R.string.loading_preview)).content(R.string.please_wait).progress(true, 0).show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    IconPackUtil packUtil = new IconPackUtil();
                    icons = packUtil.getListIcons(mContext, packageName);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            try {
                if (plsWait != null && plsWait.isShowing()) {
                    plsWait.dismiss();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                plsWait.dismiss();
                plsWait = null;
            }

            if (icons != null) {
                List<Icon> list = new ArrayList<Icon>(icons);
                Collections.sort(list, new Comparator<Icon>() {
                    public int compare(Icon o1, Icon o2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getTitle(), o2.getTitle());
                    }
                });
                GridLayout layout = (GridLayout) findViewById(R.id.iconpreview);

                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth/5, screenWidth/5);

                Resources res = mContext.getResources();
                for (final Icon icon : list) {
                    if (icon.getIconBitmap() != null) {
                        ImageView image = new ImageView(mContext);
                        image.setLayoutParams(params);
                        image.setPadding(8, 8, 8, 8);
                        image.setImageDrawable(new BitmapDrawable(res, icon.getIconBitmap()));
                        image.setOnClickListener(new ImageView.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(mContext, icon.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        image.setAdjustViewBounds(true);
                        layout.addView(image);
                    }
                }
            }
        }
    }
}
