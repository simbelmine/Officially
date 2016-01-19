package com.android.formalchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static HashMap<String, UserInfoObj> usersInfoMap;
    private static HashMap<Integer, String> allUserNamesMap;

    public RecyclerViewAdapter(BaseActivity activity, Context context, List<ParseUser> usersList, boolean isMatches) {
        usersInfoMap = new HashMap<>();
        allUserNamesMap = new HashMap<>();
        if(usersList.size() > 0) {
            addToMap(usersList);
        }

        this.activity = activity;
        this.context = context;
        this.usersList = usersList;
        this.isMatches = isMatches;
    }

    public void updateUsersList(BaseActivity activity, Context context, List<ParseUser> usersList, boolean isMatches) {
        this.activity = activity;
        this.context = context;

        if(this.usersList != null && usersList != null) {
            if(usersList.size() > 0) {
                addToMap(usersList);
            }

            this.usersList.addAll(usersList);
            this.isMatches = isMatches;
            notifyDataSetChanged();
        }
    }

    private void addToMap(List<ParseUser> usersList) {
        int lastPosition;
        if(allUserNamesMap.size() == 0) {
            lastPosition = 0;
        }
        else {
            lastPosition = allUserNamesMap.size()+1;
        }

        for(int i = lastPosition, k = 0; k < usersList.size(); i++, k++) {
            if(usersList.get(k) != null) {
                allUserNamesMap.put(i, usersList.get(k).getUsername());
            }
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
    public long getItemId(int position) {
        return position+1;
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
        protected void onPostExecute(final ParseUser user) {
//            if(viewHolder.position == position) {
//                viewHolder.userName.setText(user.get("username").toString());
            if(user != null) {
                if (user.containsKey("profileImg") && user.getParseFile("profileImg") != null) {
                    Picasso.with(context)
                            .load(user.getParseFile("profileImg").getUrl())
                            .placeholder(R.drawable.profile_img)
                            .resize(50,50)
                            .tag(context)
                            .into(viewHolder.profileImg, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Picasso.with(context)
                                            .load(user.getParseFile("profileImg").getUrl())
                                            .placeholder(R.drawable.profile_img)
                                            .tag(context)
                                            .into(viewHolder.profileImg);
                                }

                                @Override
                                public void onError() {

                                }
                            });
                } else {
                    Picasso.with(context).load(R.drawable.profile_img).into(viewHolder.profileImg);
                }
            }
//            }
        }
    }


    // ****************** Download Profile Info *********************** //

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
            if(position > (usersInfoMap.size())) {
                if(usersList.size() > 0) {
                    ArrayList<String> userNames = new ArrayList<>();
                    for(int i = 0; i < usersList.size(); i++) {
                        if(usersList.get(i) != null) {
                            userNames.add(usersList.get(i).getUsername());
                        }
                    }

                    ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
                    parseQuery.whereContainedIn("loginName", userNames);
                    parseQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> resultList, ParseException e) {
                            if(e == null) {
                                if(resultList != null && resultList.size() > 0) {

                                    for(ParseObject po : resultList) {
                                        String userName = po.get("loginName").toString();
                                        String userLocation = po.get("location").toString();
                                        String userAge = po.get("age").toString();

                                        storeUserInfoToList(po);

                                        viewHolder.userName.setText(userName);
                                        viewHolder.userLocation.setText(getShortLocationTxt(userLocation));
                                        viewHolder.userAge.setText(getFullAgeTxt(userAge));
                                        setZodiacalSign(getCalculatedZodiacSign(context, po.get("birthday").toString()));
                                    }
                                }
                                else {
                                    viewHolder.onlineDot.setVisibility(View.INVISIBLE);
                                }
                            }
                        }
                    });
                    return "Success";
                }
                else {
                    Log.v(ApplicationOfficially.TAG, "RESULT LIST NONE ");
                }
            }

            return "UnSuccess";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if("Success".equals(s)) {
                updateUserOnlineDot(activity);
            }
            else {
                if (usersInfoMap != null && usersInfoMap.size() > 0) {
                    String name = allUserNamesMap.get(position);

                    UserInfoObj currentUserInfo = usersInfoMap.get(name);
                    if (currentUserInfo != null) {
                        viewHolder.userName.setText(currentUserInfo.getUserName());
                        viewHolder.userLocation.setText(getShortLocationTxt(currentUserInfo.getLocation()));
                        viewHolder.userAge.setText(getFullAgeTxt(currentUserInfo.getAge()));
                        setZodiacalSign(currentUserInfo.getZodiacSign());

                        updateUserOnlineDot(activity);
                    }
                }
            }
        }

        private void storeUserInfoToList(ParseObject userInfoObj) {
            UserInfoObj userInfo = new UserInfoObj();

            String userName = userInfoObj.get("loginName").toString();
            userInfo.setUserName(userName);
            String userLocation = userInfoObj.get("location").toString();
            userInfo.setLocation(getShortLocationTxt(userLocation));
            userInfo.setAge(userInfoObj.get("age").toString());
            userInfo.setZodiacSign(getCalculatedZodiacSign(context, userInfoObj.get("birthday").toString()));

            usersInfoMap.put(userName, userInfo);
        }

        private ZodiacSign getCalculatedZodiacSign(Context context, String birthdayValue) {
            ZodiacCalculator zodiacCalculator = new ZodiacCalculator(context);
            ZodiacSign zodiacSignEnum = zodiacCalculator.calculateZodiacSign(birthdayValue);

            return zodiacSignEnum;
        }

        private void updateUserOnlineDot(BaseActivity activity) {
            ParseUser user = usersList.get(position);

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

        private void setZodiacalSign(ZodiacSign zodiacalSign) {
//            ZodiacCalculator zodiacCalculator = new ZodiacCalculator(context);
//            ZodiacSign zodiacSignEnum = zodiacCalculator.calculateZodiacSign(birthdayValue);

            if(zodiacalSign != null) {
                viewHolder.zodiacSign.setVisibility(View.VISIBLE);
                viewHolder.zodiacSign.setBackgroundResource(zodiacalSign.getImageId());
            }
        }
    }

    // ****************** Download Profile Info *********************** //
    // ******************          END          *********************** //

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
