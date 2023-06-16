package com.example.clothesday.Adapter.recyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.clothesday.Adapter.viewPager2.ImageSliderAdapter;
import com.example.clothesday.DAO.PostDTO;
import com.example.clothesday.DAO.UserDTO;
import com.example.clothesday.R;
import com.example.clothesday.ReplyActivity;
import com.example.clothesday.common.time.DayOfWeek;
import com.example.clothesday.common.viewPager2.Indicator;
import com.example.clothesday.fragment.MypageFragment;
import com.example.clothesday.fragment.OtherUserPageFragment;
import com.example.clothesday.fragment.PostFragment;
import com.example.clothesday.request.like.CheckLikeRequest;
import com.example.clothesday.request.like.LikePostRequest;
import com.example.clothesday.request.post.PostGetRequest;
import com.example.clothesday.request.scrap.CheckScrapRequest;
import com.example.clothesday.request.scrap.ScrapRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class OneLineLongRecyclerViewAdapter extends RecyclerView.Adapter<OneLineLongRecyclerViewAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<PostDTO> postData = new ArrayList<PostDTO>();
    private ArrayList<UserDTO> userData = new ArrayList<UserDTO>();
    private int PO_ID;
    private String ME_ID;
    private Activity activity;
    private int res = 5;
    private int check = 1;
    private  FragmentManager fm;
    private SharedPreferences pref;
    private int count = -8;


    public OneLineLongRecyclerViewAdapter(Context context, ArrayList<PostDTO> PostDTO, ArrayList<UserDTO> UserDTO, Activity activity, FragmentManager fm)  {
        this.context = context;
        this.postData = PostDTO;
        this.userData = UserDTO;
        this.activity = activity;
        this.fm = fm;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_one_line_long, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Indicator indicator = new Indicator();
        ArrayList<Uri> uriList = new ArrayList<>();
        PostDTO post = postData.get(position);
        UserDTO user = userData.get(position);
        pref = activity.getSharedPreferences("MEMBER", Context.MODE_PRIVATE);
        ME_ID = pref.getString("ME_ID", null);


        StringTokenizer stk;
        int image_count = 0;
        //뷰페이저

        if (post.getPO_PIC() != null) {
            stk = new StringTokenizer(post.getPO_PIC(), ",");
            image_count = stk.countTokens();

            // 이미지 url을 얻어서 리스트에 넣기
            while (stk.hasMoreTokens()) {
                uriList.add(Uri.parse("http://dlsxjsptb.cafe24.com/post/image/" + stk.nextToken()));
            }
            holder.layoutIndicator = indicator.deleteIndicator(holder.layoutIndicator);

            ImageSliderAdapter imageslideradapter = new ImageSliderAdapter(context, uriList, activity);
            holder.viewPager2.setOffscreenPageLimit(1);
            holder.viewPager2.setAdapter(imageslideradapter);
            holder.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    indicator.setCurrentIndicator(position, holder.layoutIndicator, context);
                }
            });
            indicator.setupIndicators(image_count, holder.layoutIndicator, context);
        }

        //프로필 이름
        holder.profile_name.setText(user.getME_NICK());


        PO_ID = post.getPO_ID();

        //프로필 사진
        Uri profile = Uri.parse("http://dlsxjsptb.cafe24.com/profile/image/" + user.getME_PIC());
        Glide.with(context).load(profile).apply(new RequestOptions().circleCrop().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).into(holder.profile_pic);

        //스크랩 아이콘
        checkScrap(post.getPO_ID(),ME_ID, activity, holder);

        //카테고리
        holder.month.setText(post.getPO_CATE());

        //게시글 내용
        holder.post_content.setText(post.getPO_CON());

        //좋아요수
        if (count == -8) {
            holder.like_count.setText(String.valueOf(post.getPO_LIKE()) + "개");
        } else {
            holder.like_count.setText(String.valueOf(count + "개"));
        }

        //좋아요 여부
       checkLike(post.getPO_ID(),ME_ID,activity,holder);
        //등록 일자
        try {
            DayOfWeek dayOfWeek = new DayOfWeek();
            String reg = post.getPO_REG_DA() + " " + dayOfWeek.getDayOfWeek(post.getPO_REG_DA());
            holder.reg_date.setText(reg + "요일");
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.reg_date.setTag(position);

    }

    @Override
    public int getItemCount() { return postData.size(); }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView profile_name, month, reply_show_text, like_count, reg_date, post_content, reply_text;
        ImageView profile_pic, scrap_icon, like_icon;
        ViewPager2 viewPager2;
        View reply_icon;
        LinearLayout layoutIndicator;
        ConstraintLayout background;
        LottieAnimationView lottieAnimationView;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            month = itemView.findViewById(R.id.recycler_one_line_long_month);
            viewPager2 = itemView.findViewById(R.id.recycler_one_line_long_viewpager2);
            profile_name = itemView.findViewById(R.id.recycler_one_line_long_profile_name);
            scrap_icon = itemView.findViewById(R.id.recycler_one_line_long_scrap_icon);
            profile_pic = itemView.findViewById(R.id.recycler_one_line_long_profile_picture);
            layoutIndicator = itemView.findViewById(R.id.recycler_one_line_long_indicator);
            background = itemView.findViewById(R.id.recycler_one_line_long_layout);
            reply_show_text = itemView.findViewById(R.id.recycler_one_line_long_reply_show_text);
            like_count = itemView.findViewById(R.id.recycler_one_line_long_like_text);
            like_icon = itemView.findViewById(R.id.recycler_one_line_long_like_icon);
            reg_date = itemView.findViewById(R.id.recycler_one_line_long_reg_text);
            post_content = itemView.findViewById(R.id.recycler_one_line_long_content);
            lottieAnimationView = itemView.findViewById(R.id.recycler_one_line_long_like_animation);
            reply_icon = itemView.findViewById(R.id.recycler_one_line_long_reply_show_btn);
            reply_text = itemView.findViewById(R.id.recycler_one_line_long_reply_show_text);

            profile_name.setOnClickListener(this);
            profile_pic.setOnClickListener(this);
            scrap_icon.setOnClickListener(this::onClick);
            viewPager2.setOnClickListener(this::onClick);
            background.setOnClickListener(this);
            like_icon.setOnClickListener(this);
            reply_text.setOnClickListener(this);

        }
        @Override
        public void onClick(View v) {
            int pos = getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                PostFragment postFragment = new PostFragment();
                OtherUserPageFragment otherUserPageFragment = new OtherUserPageFragment();
                MypageFragment mypageFragment = new MypageFragment();
                PostDTO PostDTO = postData.get(pos);
                pref = activity.getSharedPreferences("MEMBER", Context.MODE_PRIVATE);
                ME_ID = pref.getString("ME_ID", null);
                Bundle result = new Bundle();
                switch (v.getId()) {
                    case R.id.recycler_one_line_long_profile_name:
                        if (PostDTO.getPO_ME_ID().equals(ME_ID)) {
                            fm.beginTransaction().detach(mypageFragment).attach(mypageFragment).replace(R.id.container, mypageFragment).addToBackStack(null).commit();
                        } else{
                            result.putString("ME_ID", PostDTO.getPO_ME_ID());
                            fm.setFragmentResult("requestKey", result);
                            otherUserPageFragment.setArguments(result);
                            fm.beginTransaction().detach(otherUserPageFragment).attach(otherUserPageFragment).replace(R.id.container, otherUserPageFragment).addToBackStack(null).commit();
                        }
                        break;
                    case R.id.recycler_one_line_long_profile_picture:
                        if (PostDTO.getPO_ME_ID().equals(ME_ID)) {
                            fm.beginTransaction().detach(mypageFragment).attach(mypageFragment).replace(R.id.container, mypageFragment).addToBackStack(null).commit();
                        } else{
                            result.putString("ME_ID", PostDTO.getPO_ME_ID());
                            fm.setFragmentResult("requestKey", result);
                            otherUserPageFragment.setArguments(result);
                            fm.beginTransaction().detach(otherUserPageFragment).attach(otherUserPageFragment).replace(R.id.container, otherUserPageFragment).addToBackStack(null).commit();
                        }
                        break;
                    case R.id.recycler_one_line_long_scrap_icon:

                        scrapRequest(PostDTO.getPO_ID(),  ME_ID, activity, pos);

                        break;
                    case R.id.recycler_one_line_long_viewpager2:
                        result.putInt("PO_ID", PostDTO.getPO_ID());
                        fm.setFragmentResult("requestKey", result);
                        postFragment.setArguments(result);
                        fm.beginTransaction().detach(postFragment).attach(postFragment).replace(R.id.container, postFragment).addToBackStack(null).commit();
                        break;
                    case R.id.recycler_one_line_long_layout:
                        result.putInt("PO_ID", PostDTO.getPO_ID());
                        fm.setFragmentResult("requestKey", result);
                        postFragment.setArguments(result);
                        fm.beginTransaction().detach(postFragment).attach(postFragment).replace(R.id.container, postFragment).addToBackStack(null).commit();
                        break;
                    case R.id.recycler_one_line_long_like_icon:
                        postLike(activity, ME_ID,PostDTO.getPO_ID(),context,pos);
                        break;
                    case R.id.recycler_one_line_long_reply_show_text:
                        Intent suIntent = new Intent(activity, ReplyActivity.class);
                        suIntent.putExtra("PO_ID",PO_ID);
                        activity.startActivity(suIntent);
                        break;
                }
            }
        }

    }

    private int checkScrap(int PS_PO_ID, String PS_ME_ID, Activity activity, MyViewHolder holder)  {
        RequestQueue queue = Volley.newRequestQueue(activity);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject result = new JSONObject(response);
                    res = result.getInt("check"); // 1 : 스크랩 되어있음 0: 스크랩 x

                    if (String.valueOf(res).equals("1")) {
                        holder.scrap_icon.setImageResource(R.drawable.scrap_icon);
                    } else {
                        holder.scrap_icon.setImageResource(R.drawable.scrap_icon_inactive);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("3 : " + res);

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        };

        CheckScrapRequest checkScrapRequest = new CheckScrapRequest(String.valueOf(PS_PO_ID), PS_ME_ID, responseListener, errorListener);

        queue.add( checkScrapRequest );

        return res;
    }

    // 스크랩 추가, 삭제
    private int scrapRequest(int PS_PO_ID, String PS_ME_ID, Activity activity, int pos) {

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String resultResponse = response;
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    res = result.getInt("result");
                    check = result.getInt("check");
                    if (res == 1){
                        if (check == 1)
                            Toast.makeText(activity.getApplicationContext(), "스크랩을 취소하였습니다.", Toast.LENGTH_SHORT).show();
                        else if (check == 0)
                            Toast.makeText(activity.getApplicationContext(), "게시글을 스크랩하였습니다.", Toast.LENGTH_SHORT).show();
                    } else  {
                        Toast.makeText(activity.getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                    notifyItemChanged(pos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        };
        ScrapRequest ScrapRequest = new ScrapRequest(PS_PO_ID,  PS_ME_ID, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add( ScrapRequest );

        return res;
    }

    private int checkLike(int PS_PO_ID, String PS_ME_ID, Activity activity, MyViewHolder holder)  {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public  void onResponse(String response) {
                try {
                    JSONObject result = new JSONObject(response);

                    int check = result.getInt("check");

                        if (check == 1) {
                            System.out.println(1);
                            holder.like_icon.setImageResource(0);
                            holder.lottieAnimationView.setVisibility(View.VISIBLE);
                            holder.lottieAnimationView.playAnimation();
                        }
                        else  {
                            System.out.println(1);
                            holder.lottieAnimationView.setVisibility(View.GONE);
                            holder.like_icon.setImageResource(R.drawable.like_inactive);
                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        };

        CheckLikeRequest CheckLikeRequest = new CheckLikeRequest(String.valueOf(PS_PO_ID), PS_ME_ID, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add( CheckLikeRequest );


        return res;
    }



    private void postLike(Activity activity, String ME_ID, int PO_ID, Context context, int pos) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String resultResponse = response;
                try {
                    JSONObject result = new JSONObject(response);
                    int res = result.getInt("result");
                    int check = result.getInt("check");
                    count = result.getInt("count");

                    notifyItemChanged(pos);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message + " Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        };
        PostGetRequest PostGetRequest = null;
        try {
            LikePostRequest LikePostRequest = new LikePostRequest(PO_ID, ME_ID, responseListener, errorListener );
            RequestQueue queue = Volley.newRequestQueue(activity);
            queue.add(LikePostRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}


