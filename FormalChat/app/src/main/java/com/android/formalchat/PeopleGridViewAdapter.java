package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.formalchat.profile.ProfileActivityRemote;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Sve on 3/26/15.
 */
public class PeopleGridViewAdapter extends BaseAdapter {
    private BaseActivity activity;
    private Context context;
    private List<ParseUser> usersList;

    public PeopleGridViewAdapter(BaseActivity activity, Context context, List<ParseUser> usersList) {
        this.activity = activity;
        this.context = context;
        this.usersList = usersList;
    }

    public void updateUsers(List<ParseUser> users) {
        this.usersList = users;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int position) {
        return usersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ppl_grid_card, parent, false);
            setViewOnClickListener(convertView, position);

            viewHolder.profileImg = (RoundedImageView) convertView.findViewById(R.id.picture);
            viewHolder.userName = (TextView) convertView.findViewById(R.id.text);
            viewHolder.userLocation = (TextView) convertView.findViewById(R.id.location);
            viewHolder.userAge = (TextView) convertView.findViewById(R.id.age);
            viewHolder.zodiacSign = (ImageView) convertView.findViewById(R.id.zodiac_sign);
            viewHolder.onlineDot = (ImageView) convertView.findViewById(R.id.online_dot);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.position = position;


//        ParseUser user = usersList.get(position);
//
//
//        viewHolder.userName.setText(user.get("username").toString());
//        if(user.containsKey("profileImg") && user.getParseFile("profileImg") != null) {
//            Picasso.with(context).load(user.getParseFile("profileImg").getUrl()).into(viewHolder.profileImg);
//        }

        new DownloadProfileGridImage(context, viewHolder, usersList, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new DownloadProfileInfo(activity, context, viewHolder, usersList, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return convertView;
    }

    public static class ViewHolder {
        RoundedImageView profileImg;
        TextView userName;
        TextView userLocation;
        TextView userAge;
        ImageView zodiacSign;
        ImageView onlineDot;
        int position;
    }

    private static class DownloadProfileGridImage extends AsyncTask<ParseUser, Void, ParseUser> {
        private Context context;
        private ViewHolder viewHolder;
        private List<ParseUser> usersList;
        private int position;

        public DownloadProfileGridImage(Context context, ViewHolder viewHolder,List<ParseUser> usersList, int position) {
            this.context = context;
            this.viewHolder = viewHolder;
            this.usersList = usersList;
            this.position = position;
        }

        @Override
        protected ParseUser doInBackground(ParseUser... params) {
            return usersList.get(position);
        }

        @Override
        protected void onPostExecute(ParseUser user) {
            if(viewHolder.position == position) {
//                viewHolder.userName.setText(user.get("username").toString());
                if(user.containsKey("profileImg") && user.getParseFile("profileImg") != null) {
                    Picasso.with(context).load(user.getParseFile("profileImg").getUrl()).into(viewHolder.profileImg);
                }
            }
        }
    }

    private static class DownloadProfileInfo extends AsyncTask<ParseUser, Void, String> {
        private BaseActivity activity;
        private Context context;
        private ViewHolder viewHolder;
        private List<ParseUser> usersList;
        private int position;

        public DownloadProfileInfo(BaseActivity activity, Context context, ViewHolder viewHolder,List<ParseUser> usersList, int position) {
            this.activity = activity;
            this.context = context;
            this.viewHolder = viewHolder;
            this.usersList = usersList;
            this.position = position;
        }

        @Override
        protected String doInBackground(ParseUser... params) {
            ParseUser user = usersList.get(position);

            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
            parseQuery.whereEqualTo("loginName", user.getUsername());
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e == null && list.size() > 0) {
                        ParseObject userInfo = list.get(0);
                        String userName = userInfo.get("loginName").toString();
                        String userLocation = userInfo.get("location").toString();
                        String userAge = userInfo.get("age").toString();

                        viewHolder.userName.setText(userName);
                        viewHolder.userLocation.setText(getShortLocationTxt(userLocation));
                        viewHolder.userAge.setText(getFullAgeTxt(userAge));
                        setZodiacalSign(userInfo.get("birthday").toString());
                    }
                    else {
                        viewHolder.onlineDot.setVisibility(View.INVISIBLE);
                    }
                }
            });


            return "Success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseUser user = usersList.get(position);
            updateUserOnlineDot(activity, user);
        }

        private void updateUserOnlineDot(BaseActivity activity, ParseUser user) {
            if((activity).isUserOnline(user)) {
                viewHolder.onlineDot.setImageDrawable(context.getResources().getDrawable(R.drawable.oval_btn));
            }
            else {
                viewHolder.onlineDot.setImageDrawable(context.getResources().getDrawable(R.drawable.oval_btn_gray));
            }
        }

        private String getShortLocationTxt(String userLocation) {
            if(userLocation == null || userLocation.length() <= 0){
                return "";
            }
            return "@"+userLocation.substring(userLocation.lastIndexOf(",") + 2, userLocation.length());
        }

        private String getFullAgeTxt(String userAge) {
            return userAge + " years";
        }

        private void setZodiacalSign(String birthdayValue) {
            ZodiacCalculator zodiacCalculator = new ZodiacCalculator(context);
            ZodiacSign zodiacSignEnum = zodiacCalculator.calculateZodiacSign(birthdayValue);

            if(zodiacSignEnum != null) {
                viewHolder.zodiacSign.setVisibility(View.VISIBLE);
                viewHolder.zodiacSign.setBackgroundResource(zodiacSignEnum.getImageId());
            }
        }
    }

    private void setViewOnClickListener(View convertView, final int position) {
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = usersList.get(position);
                Intent intent = new Intent(context, ProfileActivityRemote.class);
                intent.putExtra("userNameMain", user.getUsername());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }
}
