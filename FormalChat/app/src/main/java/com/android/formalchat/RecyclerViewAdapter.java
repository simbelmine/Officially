package com.android.formalchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
 * Created by Sve on 1/8/16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolders> {
    private static int ITEM_VIEW_TYPE_HEADER = 0;
    private static int ITEM_VIEW_TYPE_ITEM = 1;
    private BaseActivity activity;
    private Context context;
    private List<ParseUser> usersList;
    private boolean isMatches;

    public RecyclerViewAdapter(BaseActivity activity, Context context, List<ParseUser> usersList, boolean isMatches) {
//        Log.e(ApplicationOfficially.TAG, "*** RecyclerViewAdapter COnstructor ***");

        this.activity = activity;
        this.context = context;
        this.usersList = usersList;
        this.isMatches = isMatches;
    }

    public void updateUsersList(BaseActivity activity, Context context, List<ParseUser> usersList, boolean isMatches) {
        this.activity = activity;
        this.context = context;

        if(this.usersList != null && usersList != null) {
            this.usersList.addAll(usersList);
            this.isMatches = isMatches;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        if(this.usersList == null) {
            return 0;
        }
        return this.usersList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ?
                ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER) {
//            CardView cardView = getCardView();
            return new RecyclerViewHolders(getHeader());
        }

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_list, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        setViewOnClickListener(holder.itemView, position);

        if(!isHeader(position)) {
            new DownloadProfileGridImage(context, holder, usersList, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new DownloadProfileInfo(activity, context, holder, usersList, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public boolean isHeader(int position) {
        if(usersList.get(position) == null) {
            return true;
        }
        return false;
    }

    private static class DownloadProfileGridImage extends AsyncTask<ParseUser, Void, ParseUser> {
        private Context context;
        private RecyclerViewHolders viewHolder;
        private List<ParseUser> usersList;
        private int position;

        public DownloadProfileGridImage(Context context, RecyclerViewHolders viewHolder, List<ParseUser> usersList, int position) {
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
//            if(viewHolder.position == position) {
//                viewHolder.userName.setText(user.get("username").toString());
            if(user != null) {
                if (user.containsKey("profileImg") && user.getParseFile("profileImg") != null) {
                    Picasso.with(context).load(user.getParseFile("profileImg").getUrl()).into(viewHolder.profileImg);
                } else {
                    Picasso.with(context).load(R.drawable.profile_img).into(viewHolder.profileImg);
                }
            }
//            }
        }
    }

    private static class DownloadProfileInfo extends AsyncTask<ParseUser, Void, String> {
        private BaseActivity activity;
        private Context context;
        private RecyclerViewHolders viewHolder;
        private List<ParseUser> usersList;
        private int position;

        public DownloadProfileInfo(BaseActivity activity, Context context, RecyclerViewHolders viewHolder, List<ParseUser> usersList, int position) {
            this.activity = activity;
            this.context = context;
            this.viewHolder = viewHolder;
            this.usersList = usersList;
            this.position = position;
        }

        @Override
        protected String doInBackground(ParseUser... params) {
            ParseUser user = usersList.get(position);

            if(user != null) {
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
                        } else {
                            viewHolder.onlineDot.setVisibility(View.INVISIBLE);
                        }
                    }
                });
                return  "Success";
            }

            return "UnSuccess";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if("Success".equals(s)) {
                ParseUser user = usersList.get(position);
                updateUserOnlineDot(activity, user);
            }
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
                if(user != null) {
                    Intent intent = new Intent(context, ProfileActivityRemote.class);
                    intent.putExtra("userNameMain", user.getUsername());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }

    private TextView getHeader() {
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);

        TextView txtView = new TextView(context);
//        if(isMatches) {
//            txtView.setText("MATCHES");
//        }
//        else {
//            txtView.setText("ALL PPL");
//        }
        txtView.setGravity(Gravity.CENTER);
        txtView.setTypeface(Typeface.DEFAULT_BOLD);
        txtView.setLayoutParams(params);
        return txtView;
    }

    private CardView getCardView() {
        CardView cardView = new CardView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
        );
        int margin = 16;
        params.setMargins(0, margin, 0, margin);

        cardView.setRadius(9);
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_gray));
        cardView.setUseCompatPadding(true);
        cardView.setLayoutParams(params);

//        cardView.addView(getHeader());

        return cardView;
    }
}
