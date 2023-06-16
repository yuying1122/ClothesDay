package com.example.clothesday.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.clothesday.Adapter.recyclerView.FollowRecyclerViewAdapter;
import com.example.clothesday.DAO.UserDTO;
import com.example.clothesday.R;
import com.example.clothesday.common.fragment.onBackPressedListener;
import com.example.clothesday.request.follow.FollowerGetRequest;
import com.example.clothesday.request.follow.FollowingGetRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FollowFragment extends Fragment implements onBackPressedListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public FollowFragment() {
    }

    private Activity activity;
    private Context context;

    @Override
    public void onAttach(Context context) {
        this.context = context;

        if (context instanceof Activity) {
            activity = (Activity)context;
        }
        super.onAttach(context);
    }

    public static FollowFragment newInstance(String param1, String param2) {
        FollowFragment fragment = new FollowFragment();
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

    //뷰
    private TextView follow_word;
    private RecyclerView recyclerView;

    //데이터
    private String type, ME_ID, ME_NICK = "";

    //프래그먼트 매니저
    private FragmentManager fragmentManager;

    //리사이클러뷰
    private FollowRecyclerViewAdapter followRecyclerViewAdapter;
    private ArrayList<UserDTO> mList = new ArrayList<UserDTO>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_follow, container, false);
        // 뷰 연결
        follow_word = root.findViewById(R.id.follow_fragment_word);
        recyclerView = root.findViewById(R.id.follow_fragment_recycler);

        //프래그먼트 매니저
        fragmentManager = getParentFragmentManager();

        // 리사이클러뷰
        LinearLayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        //초기상태
        Bundle bundle = getArguments();

        if (bundle != null) {
            ME_ID = bundle.getString("ME_ID");
            type = bundle.getString("type");
            ME_NICK = bundle.getString("ME_NICK");
        }

        if (type.equals("follower")) {
            follow_word.setText(ME_NICK +"님의 팔로워");
        } else
            follow_word.setText(ME_NICK +"님의 팔로잉");

        getFollow();

        return root;
    }

    private void getFollow() {

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mList = new ArrayList<UserDTO>();
                String resultResponse = response;
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    JSONArray jsonArray = result.getJSONArray("follow");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        UserDTO userDTO = new UserDTO();
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        if (type.equals("follower")) {
                            userDTO.setME_ID(jObject.getString("FO_FOL_ID"));
                        } else
                            userDTO.setME_ID(jObject.getString("FO_ME_ID"));
                        userDTO.setME_NICK(jObject.getString("ME_NICK"));
                        userDTO.setME_PIC(jObject.getString("ME_PIC"));
                        mList.add(userDTO);
                    }
                    followRecyclerViewAdapter = new FollowRecyclerViewAdapter(context, mList, activity,fragmentManager, 1);
                    recyclerView.setAdapter( followRecyclerViewAdapter);


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

        if (type.equals("follower")) {

            FollowerGetRequest FollowerGetRequest = new FollowerGetRequest(ME_ID, responseListener, errorListener);
            RequestQueue queue = Volley.newRequestQueue(activity);
            queue.add(FollowerGetRequest);
        } else {
            FollowingGetRequest FollowingGetRequest = new FollowingGetRequest(ME_ID, responseListener, errorListener);
            RequestQueue queue = Volley.newRequestQueue(activity);
            queue.add(FollowingGetRequest);
        }

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