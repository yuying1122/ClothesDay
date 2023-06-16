package com.example.clothesday.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.example.clothesday.common.fragment.onBackPressedListener;
import com.example.clothesday.common.time.DayOfWeek;
import com.example.clothesday.common.viewPager2.Indicator;
import com.example.clothesday.request.like.LikePostRequest;
import com.example.clothesday.request.post.PostGetRequest;
import com.example.clothesday.request.report.ReportRequest;
import com.example.clothesday.request.scrap.ScrapRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class PostFragment extends Fragment implements View.OnClickListener, onBackPressedListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public PostFragment() {
    }

    private Activity mActivity;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        mContext = context;

        if (context instanceof Activity) {
            mActivity = (Activity)context;
        }
        super.onAttach(context);

    }
    // detach에서 leak방지
    @Override
    public void onDetach() {
        mActivity = null;
        mContext = null;
        super.onDetach();
    }

    public static PostFragment newInstance(String param1, String param2) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }
    private Context Context;

    // 뷰
    private ImageView profile_picture, scrap_icon, like_icon;
    private TextView profile_name, reply_show, reg_date, post_content, like_count, report_text, month;
    private LottieAnimationView animationView;

    int res= 0;
    int check =0;

    //DAO
    private PostDTO postdao;
    private UserDTO userdao;

    //프리퍼런스
    private SharedPreferences pref;

    //초기상태
    private String RP_VIL_ID = "", RP_CON = "";
    private String ME_ID = "";
    private int PO_ID = 0;

    //다이얼로그
    private List<String> selected;
    private AlertDialog.Builder builder;

    private int stack;

    private FragmentManager fragmentManager;

    //뷰페이저
    private ViewPager2 viewPager2;
    private Indicator indicator;
    private LinearLayout layoutIndicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_post ,container,false);
        // 뷰 연결
        profile_name = root.findViewById(R.id.post_fragment_profile_name);
        profile_picture = root.findViewById(R.id.post_fragment_profile_picture);
        scrap_icon = root.findViewById(R.id.post_fragment_scrap_icon);
        like_icon = root.findViewById(R.id.post_fragment_like_icon);
        reply_show = root.findViewById(R.id.post_fragment_reply_show_text);
        reg_date = root.findViewById(R.id.post_fragment_reg_text);
        post_content = root.findViewById(R.id.post_content);
        like_count = root.findViewById(R.id.post_fragment_like_text);
        viewPager2 = root.findViewById(R.id.post_fragment_viewpager2);
        layoutIndicator = root.findViewById(R.id.post_fragment_indicator);
        report_text = root.findViewById(R.id.post_fragment_report);
        animationView = root.findViewById(R.id.post_fragment_like_animation);
        month = root.findViewById(R.id.post_fragment_month);

        //초기상태
        Bundle bundle = getArguments();
        if (bundle != null) {
            PO_ID = bundle.getInt("PO_ID");
            stack = bundle.getInt("stack");
        }

        //리스너등록
        scrap_icon.setOnClickListener(this);
        like_icon.setOnClickListener(this);
        reply_show.setOnClickListener(this);
        report_text.setOnClickListener(this);
        if (stack == 0) {
            profile_picture.setOnClickListener(this);
            profile_name.setOnClickListener(this);
        }

        //프리퍼런스
        pref = getActivity().getSharedPreferences("MEMBER", Context.MODE_PRIVATE);
        ME_ID = pref.getString("ME_ID", null);

        //프래그먼트매니저
        fragmentManager = getActivity().getSupportFragmentManager();

        getPost();

        return root;
    }

    private void getPost() {

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                postdao = new PostDTO();
                userdao = new UserDTO();
                String resultResponse = response;
                try {
                    indicator = new Indicator();
                    JSONObject result = new JSONObject(resultResponse);
                    JSONArray jsonArray = result.getJSONArray("post");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        postdao.setPO_ID(jObject.getInt("PO_ID"));
                        postdao.setPO_REG_DA(jObject.getString("PO_REG_DA")); // 완
                        postdao.setPO_CATE(jObject.getString("PO_CATE")); //완
                        postdao.setPO_TAG(jObject.getString("PO_TAG"));
                        postdao.setPO_PIC(jObject.getString("PO_PIC")); //완
                        postdao.setPO_CON(jObject.getString("PO_CON")); //완
                        postdao.setPO_ME_ID(jObject.getString("PO_ME_ID"));
                        postdao.setPO_LIKE(jObject.getInt("PO_LIKE")); // 완
                        userdao.setME_NICK(jObject.getString("ME_NICK")); //완
                        userdao.setME_PIC(jObject.getString("ME_PIC")); //완
                    }
                    RP_VIL_ID = postdao.getPO_ME_ID();
                    int scrapCheck = result.getInt("scrap"); // 완
                    int likeCheck = result.getInt("like"); // 완

                    //like 좋아요
                    if (likeCheck == 1) {
                        animationView.setVisibility(View.VISIBLE);
                        animationView.playAnimation();
                    }
                    else
                        like_icon.setImageResource(R.drawable.like_inactive);
                    //PO_CATE
                    month.setText(postdao.getPO_CATE());
                    //ME_NICK
                    profile_name.setText(userdao.getME_NICK());
                    if (userdao.getME_NICK().equals("옷날"))
                        report_text.setVisibility(View.GONE);
                    //PO_LIKE
                    like_count.setText(String.valueOf(postdao.getPO_LIKE()) + "개");
                    //scrap 스크랩
                    if (scrapCheck == 1) {
                        scrap_icon.setImageResource(R.drawable.scrap_icon);
                    } else {
                        scrap_icon.setImageResource(R.drawable.scrap_icon_inactive);
                    }
                    //PO_CON
                    post_content.setText(postdao.getPO_CON());
                    //ME_PIC
                    Glide.with(getActivity().getApplicationContext()).load(Uri.parse("http://dlsxjsptb.cafe24.com/profile/image/" + userdao.getME_PIC())).apply(new RequestOptions().circleCrop().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).into(profile_picture);
                    //PO_PIC, 뷰페이저
                    StringTokenizer stk;
                    int image_count = 0;
                    ArrayList<Uri> uriList = new ArrayList<>();
                 
                    if (postdao.getPO_PIC() != null) {
                        stk = new StringTokenizer(postdao.getPO_PIC(), ",");
                        image_count = stk.countTokens();
                 
                        while(stk.hasMoreTokens()){
                            uriList.add(Uri.parse("http://dlsxjsptb.cafe24.com/post/image/" +stk.nextToken()));
                        }
                    }
                    Context context = getActivity().getApplicationContext();

                    ImageSliderAdapter imageslideradapter = new ImageSliderAdapter(getActivity().getApplicationContext(),uriList, getActivity());
                    viewPager2.setOffscreenPageLimit(1);
                    viewPager2.setAdapter(imageslideradapter);
                    viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            indicator.setCurrentIndicator(position, layoutIndicator, context);
                        }
                    });
                    indicator.setupIndicators(image_count, layoutIndicator, context);

                    //PO_REG_DA
                    try {
                        DayOfWeek dayOfWeek = new DayOfWeek();
                        String reg = postdao.getPO_REG_DA() + " " + dayOfWeek.getDayOfWeek(postdao.getPO_REG_DA());
                        reg_date.setText(reg + "요일");
                    } catch (Exception e) {
                        e.printStackTrace();
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
            PostGetRequest = new PostGetRequest(String.valueOf(PO_ID), ME_ID, responseListener, errorListener );
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(PostGetRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.post_fragment_like_icon:
                postLike(getActivity(), ME_ID, PO_ID, getActivity().getApplicationContext());
                break;
            case R.id.post_fragment_scrap_icon:
                scrapRequest(PO_ID, ME_ID, getActivity());
                break;
            case R.id.post_fragment_reply_show_text:
                Intent suIntent = new Intent(getActivity(), ReplyActivity.class);
                suIntent.putExtra("PO_ID",PO_ID);
                startActivity(suIntent);
                break;
            case R.id.post_fragment_report:
                showDialog();
                break;
            case R.id.post_fragment_profile_name:
                gotoProfile();
                break;
            case R.id.post_fragment_profile_picture:
                gotoProfile();
                break;
        }
    }

    private void gotoProfile() {
        OtherUserPageFragment otherUserPageFragment = new OtherUserPageFragment();
        MypageFragment mypageFragment = new MypageFragment();
        pref = getActivity().getSharedPreferences("MEMBER", Context.MODE_PRIVATE);
        Bundle result = new Bundle();

        if (postdao.getPO_ME_ID().equals(ME_ID)) {
            fragmentManager.beginTransaction().detach(mypageFragment).attach(mypageFragment).replace(R.id.container, mypageFragment).addToBackStack(null).commit();
        } else{
            result.putString("ME_ID", postdao.getPO_ME_ID());
            fragmentManager.setFragmentResult("requestKey", result);
            otherUserPageFragment.setArguments(result);
            fragmentManager.beginTransaction().detach(otherUserPageFragment).attach(otherUserPageFragment).replace(R.id.container, otherUserPageFragment).addToBackStack(null).commit();
        }
    }

    private void showDialog() {
        selected = new ArrayList<>();
        builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("신고");
        builder.setMultiChoiceItems(R.array.report, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                //데이터 리스트담기
                String[] items = getResources().getStringArray(R.array.report);
                //선택된 리스트 담기
                if (isChecked) {
                    selected.remove(items[which]);
                    selected.add(items[which]);
                } else if(selected.contains(items[which])) {
                    selected.remove(items[which]);
                }
            }
        });
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                report();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });
        AlertDialog alertDialog =builder.create();
        alertDialog.show();

    }

    private void report() {
        RP_CON = "";
        for (int i = 0; i < selected.size(); i++) {
            RP_CON += selected.get(i);
            if (selected.size() -1 > i)
                RP_CON += ",";
        }
        if (RP_CON.equals("")) {
            Toast.makeText(getActivity().getApplicationContext(), "신고 사유를 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String resultResponse = response;
                try {
                    JSONObject result = new JSONObject(resultResponse);

                    int res = result.getInt("result");
                    if (res == 1){
                        Toast.makeText(getActivity().getApplicationContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                    } else  {
                        Toast.makeText(getActivity().getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
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
        ReportRequest ReportRequest = new ReportRequest(RP_VIL_ID, ME_ID, RP_CON ,String.valueOf(PO_ID), responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add( ReportRequest );

    }

    //좋아요
    private void postLike(Activity activity, String ME_ID, int PO_ID, Context context) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String resultResponse = response;
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    int res = result.getInt("result");
                    int check = result.getInt("check");
                    int count = result.getInt("count");
                    if (res == 1){
                        if (check == 1) {
                            animationView.setVisibility(View.GONE);
                            like_count.setText(count + "개");
                            like_icon.setImageResource(R.drawable.like_inactive);
                        }
                        else if (check == 0) {
                            like_icon.setImageResource(0);
                            like_count.setText(count + "개");
                            animationView.setVisibility(View.VISIBLE);
                            animationView.playAnimation();
                        }
                    } else  {
                        Toast.makeText(getActivity().getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
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
    
    //스크랩
    private void scrapRequest(int PS_PO_ID, String PS_ME_ID, Activity activity) {

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String resultResponse = response;
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    res = result.getInt("result");
                    check = result.getInt("check");
                    if (res == 1){
                        if (check == 1) {
                            Toast.makeText(activity.getApplicationContext(), "스크랩을 취소하였습니다.", Toast.LENGTH_SHORT).show();
                            scrap_icon.setImageResource(R.drawable.scrap_icon_inactive);
                        }
                        else if (check == 0) {
                            Toast.makeText(activity.getApplicationContext(), "게시글을 스크랩하였습니다.", Toast.LENGTH_SHORT).show();
                            scrap_icon.setImageResource(R.drawable.scrap_icon);
                        }
                    } else  {
                        Toast.makeText(activity.getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
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
        ScrapRequest ScrapRequest = new ScrapRequest(PS_PO_ID,  PS_ME_ID, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add( ScrapRequest );
    }

    @Override
    public void onBackPressed() {
        goToMain();
    }

    //프래그먼트 종료
    private void goToMain(){
        fragmentManager.beginTransaction().remove(this).commit();
        fragmentManager.popBackStack();
    }

}