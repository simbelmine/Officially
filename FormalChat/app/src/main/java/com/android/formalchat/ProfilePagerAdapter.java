package com.android.formalchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 2/4/15.
 */
public class ProfilePagerAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<String> images = new ArrayList<>();
    /////// ***** For the Video File *****
    private static final int RESULT_OK = -1;
    private File dir = Environment.getExternalStorageDirectory();
    private String filePath = "/.formal_chat/";
    private String fileName;
    private File tmpFile;
    private ParseFile videoFile;
    private BroadcastReceiver broadcastReceiver;
    private Activity activity;
    private Bitmap thumbnail;
    private Uri videoUri;

    public ProfilePagerAdapter(Activity actvty, Context ctx, ArrayList<String> paths) {
        activity = actvty;
        context = ctx;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.images.addAll(paths);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void updateImages(List<String> paths) {
        this.images.clear();
        this.images.addAll(paths);
        this.notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View itemView = layoutInflater.inflate(R.layout.viewpager_item, container, false);
        final ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
        final VideoView videoView = (VideoView) itemView.findViewById(R.id.video);
        final ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);

        Picasso.with(context).load(images.get(position)).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                imageView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                if (isVideo(position)) {
                    initBroadcastReceiver(videoView);
                    context.registerReceiver(broadcastReceiver, new IntentFilter(VideoDownloadService.NOTIFICATION));
                    downloadVideoIfNotExists();

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    addTransparentPlayImage(itemView);
                    reCreateImageView(layoutParams, imageView);
                    setVideoImgOnClickListener(imageView);
                    progressBar.setVisibility(View.GONE);
                    context.unregisterReceiver(broadcastReceiver);
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, FullImageActivity.class);
                i.putExtra("url", images.get(position));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        container.addView(itemView);
        return itemView;
    }

    private void addTransparentPlayImage(View itemView) {
        ImageView playImageView = (ImageView) itemView.findViewById(R.id.image_play);
        playImageView.setVisibility(View.VISIBLE);
    }

    private void reCreateImageView(FrameLayout.LayoutParams layoutParams, ImageView imageView) {
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(thumbnail);
    }

    private void downloadVideoIfNotExists() {
        ParseUser user = ParseUser.getCurrentUser();
        videoFile = user.getParseFile("video");
        fileName = videoFile.getName();
        videoUri = Uri.parse(dir + filePath + fileName);

        File targetFolder = new File(dir + filePath);
        if(!targetFolder.exists()) {
            targetFolder.mkdir();
        }

        tmpFile = new File(dir, filePath + fileName);
        if(!tmpFile.exists()) {
            startVideoDownloadService();
        }
        else {
            thumbnail  = getVideoThumbnail();
        }
    }

    private void setVideoImgOnClickListener(ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoShowActivity();
            }
        });
    }

    private boolean isVideo(int position) {
        String path = images.get(position);
        String extension = getExtention(path);
        if("VID".equals(extension)) {
            return true;
        }
        return false;
    }

    private String getExtention(String path) {
        int startIdx = path.lastIndexOf("-")+1;
        int endIdx = startIdx + 3;
        return path.substring(startIdx, endIdx);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }


    /////////////////////////////
    /// ***** For Video ***** ///
    /////////////////////////////

    private void initBroadcastReceiver(final VideoView videoView) {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if(bundle != null) {
                    int resultCode = bundle.getInt(VideoDownloadService.RESULT);
                    if(resultCode == RESULT_OK) {
                        thumbnail  = getVideoThumbnail();
                    }
                    else {
                        Log.e("formalchat", "DoWnLoAd Failed .... !!!");
                    }
                }
            }
        };
    }

    private Bitmap getVideoThumbnail() {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(),
                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        return thumb;
    }

    private void openVideoShowActivity() {
        Intent intent = new Intent(activity, VideoShowActivity.class);
        intent.putExtra("videoUri", videoUri.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void startVideoDownloadService() {
        Intent intent = new Intent(context, VideoDownloadService.class);
        intent.putExtra(VideoDownloadService.DIRPATH, dir.getAbsolutePath());
        intent.putExtra(VideoDownloadService.FILEPATH, filePath);

        context.startService(intent);
    }
}
