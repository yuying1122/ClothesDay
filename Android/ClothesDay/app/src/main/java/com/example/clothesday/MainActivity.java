package com.example.clothesday;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.clothesday.common.fragment.onBackPressedListener;
import com.example.clothesday.fragment.FollowFragment;
import com.example.clothesday.fragment.HomeFragment;
import com.example.clothesday.fragment.MonthBoardFragment;
import com.example.clothesday.fragment.MypageFragment;
import com.example.clothesday.fragment.NoticeBoardFragment;
import com.example.clothesday.fragment.OtherUserPageFragment;
import com.example.clothesday.fragment.PostFragment;
import com.example.clothesday.fragment.PostManagementFragment;
import com.example.clothesday.fragment.SearchFragment;
import com.example.clothesday.request.fcm.TokenSetRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener {

    // 하단 메뉴
    private BottomNavigationView bottomView;

    //프래그먼트
    private HomeFragment home_fragment;
    private SearchFragment search_fragment;
    private PostManagementFragment post_management_fragment;
    private NoticeBoardFragment noticeBoard_fragment;
    private MypageFragment mypage_fragment;
    private MonthBoardFragment monthBoardFragment;

    //FCM 토큰을 저장할 변수
    private String token;

    //프리퍼런스
    private SharedPreferences pref;
    private SharedPreferences.Editor edit;
    
    //유저 이메일
    private String ME_ID;
    
    //액티비티
    private Activity activity = MainActivity.this;

    //인텐트
    private Intent intent;

    // 메뉴 액티비티에서 월별 게시판을 클릭해 메인 액티비티로 넘어온 경우 intent로 넘어온 월별 게시판 정보를 담을 변수
    private String month = "";

    // 상단 메뉴
    private Toolbar toolbar;

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시할 토스트
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 프래그먼트
        mypage_fragment = new MypageFragment();
        home_fragment = new HomeFragment();
        post_management_fragment = new PostManagementFragment();
        search_fragment = new SearchFragment();
        noticeBoard_fragment = new NoticeBoardFragment();


        //툴바 설정
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar); // 액션바를 툴바로 교체
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 툴바 제목 안보이게
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바에 홈 버튼 생성
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon); // 홈 버튼 아이콘을 메뉴 아이콘으로 교체

        //하단 메뉴
        bottomView = findViewById(R.id.main_bottomnavi);
        bottomView.setItemIconTintList(null); // 하단 메뉴 아이콘의 색상을 원래대로
        bottomView.setOnItemSelectedListener(this); // 하단 메뉴 선택시 동작할 리스너 등록

        //프리퍼런스
        pref = getSharedPreferences("MEMBER", Context.MODE_PRIVATE);
        ME_ID = pref.getString("ME_ID",null); // 유저 이메일
        token = pref.getString("ME_TOKEN", null); // 유저 FCM 토큰


        if (token == null) { // FCM 토큰이 없는 디바이스인 경우
            // FCM 토큰 받아오기
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) { // 토큰 등록 실패 시 return
                                return;
                            }

                            // Get new FCM registration token
                            token = task.getResult(); // String 변수에 받아온 FCM 토큰 넣기 
                            System.out.println(token);
                            setToken(); // db와 프리퍼런스에 FCM 토큰 저장하기
                        }
                    });
        }

        //인텐트
        intent = getIntent();
        month = intent.getStringExtra("month");
        if (month != null ) {  // 메뉴 액티비티에서 월별 게시판을 클릭해 넘어 왔을 경우 곧바로 월별 게시판 프래그먼트 띄우기
            goToMonthBoard(month); // 월별 게시판 프래그먼트 띄우는 메소드
        } else {
            // 그 외에 경우 메인 액티비티 실행 시 홈 프래그먼트 띄우기
            getSupportFragmentManager().beginTransaction().replace(R.id.container, home_fragment).commit();
        }
    }

    private void setToken() { // DB와 프리퍼런스에 FCM 토큰 저장하기

        Response.Listener<String> responseListener = new Response.Listener<String>() { // 서버로 요청할 때 쓰이는 변수
            @Override
            public void onResponse(String response) { // 서버의 응답을 처리하는 메소드

                try {
                    JSONObject result = new JSONObject(response);
                    boolean res = result.getBoolean("success");
                    if (res) { // DB에 FCM 토큰 저장 성공시 프리퍼런스에도 FCM 토큰 저장하기
                        pref = getSharedPreferences("MEMBER", Context.MODE_PRIVATE);
                        edit = pref.edit();
                        edit.putString("ME_TOKEN", token);
                        edit.commit();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() { // 웹 요청 에러 처리를 위한 변수
            @Override
            public void onErrorResponse(VolleyError error) { // 웹 요청 에러 발생시 작동하는 메소드
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
                        org.json.JSONObject response = new JSONObject(result);
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

        TokenSetRequest TokenSetRequest = new TokenSetRequest(ME_ID, token, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(TokenSetRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // 툴바에 미리 만들어둔 메뉴 xml 파일 부착
        getMenuInflater().inflate(R.menu.menu_top, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        //프래그먼트 onBackPressedListener사용
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments(); // 스택에 저장되어 있는 프래그먼트들 가져오기
//        for(Fragment fragment : fragmentList){
//            if(fragment instanceof onBackPressedListener){
//                ((onBackPressedListener)fragment).onBackPressed();
//                return;
//            }
//        }
        
        Fragment test = getSupportFragmentManager().findFragmentById(R.id.container); // 메인 액티비티 레이아웃에 있는 프래그먼트 VIEW 연결

        // 프래그먼트들
        PostFragment postFragment = new PostFragment();
        OtherUserPageFragment otherUserPageFragment = new OtherUserPageFragment();
        SearchFragment searchFragment = new SearchFragment();
        PostManagementFragment postManagementFragment = new PostManagementFragment();

        // 스택에 프래그먼트가 저장되어 있는지 확인 && 뒤로가기 메소드를 override한 프래그먼트인지 확인
        if (test != null && fragmentList.size() > 0 && (test.getClass().isInstance(postFragment)) || test.getClass().isInstance(otherUserPageFragment) ||test.getClass().isInstance(searchFragment) || test.getClass().isInstance(postManagementFragment) || test.getClass().isInstance(new FollowFragment()) ) {
            ((onBackPressedListener)test).onBackPressed();
            return;
        }

      //  super.onBackPressed(); 기존 뒤로가기 버튼 동작

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로가기\' 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();   // 현재 표시된 Toast 취소
            finishAffinity();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // 상단 메뉴 클릭시 작동하는 메소드
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: // 메뉴 버튼 클릭시 메뉴 액티비티(월별 게시판으로 이동할 수 있는 화면)로 이동
                Intent suIntent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(suIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) { // 하단 메뉴 클릭시 작동하는 메소드
        mypage_fragment = new MypageFragment();
        Bundle result = new Bundle();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (item.getItemId()) {
            case R.id.mypage_btn: // 마이페이지 버튼
                result.putString("bundleKey", "mypage"); // 내 게시글 목록을 바로 띄우기 위해 정보를 Bundle에 담기
                mypage_fragment.setArguments(result);
                if (Build.VERSION.SDK_INT >= 26) {
                    ft.setReorderingAllowed(false);
                }
                ft.detach(mypage_fragment).attach(mypage_fragment).replace(R.id.container, mypage_fragment).commit(); //마이페이지 프래그먼트로 교체
                return true;
            case R.id.home_btn: // 홈 버튼
                getSupportFragmentManager().beginTransaction().replace(R.id.container, home_fragment).commit(); //홈 프래그먼트로 교채
                return true;
            case R.id.search_btn: // 검색 버튼
                getSupportFragmentManager().beginTransaction().replace(R.id.container, search_fragment).commit(); // 검색 프래그먼트로 교체
                return true;
            case R.id.noticeboard_btn: // 게시판 버튼
                getSupportFragmentManager().beginTransaction().replace(R.id.container, noticeBoard_fragment).commit(); // 게시판 프래그먼트로 교체
                return true;
            case R.id.scrap_btn: // 스크랩 버튼
                result.putString("bundleKey", "scrap"); // 스크랩 게시글 목록을 바로 띄우기 위해 정보를 Bundle에 담기
                mypage_fragment.setArguments(result);
                if (Build.VERSION.SDK_INT >= 26) {
                   ft.setReorderingAllowed(false);
                }
                ft.detach(mypage_fragment).attach(mypage_fragment).replace(R.id.container, mypage_fragment).commit(); // 마이페이지(스크랩) 프래그먼트로 교체

                return true;
        }
        return false;
    }

    private void goToMonthBoard(String month) { // 월별 게시판 프래그먼트 띄우는 메소드
        monthBoardFragment = new MonthBoardFragment();
        Bundle result = new Bundle();
        switch (month) {
            case "1":  // 1월 게시판 클릭
                result.putString("month", "1"); // 1월 게시판을 클릭했음을 알려주는 정보를 Bundle에 넣기
                monthBoardFragment.setArguments(result); // 월별 게시판 프래그먼트로 교체할 때 Bundle을 통해 월 정보를 보냄
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit(); // 월별 게시판 프래그먼트를 프래그먼트 스택에 저장한 후 교체
                return;
            case "2": // 2월 게시판 클릭
                result.putString("month", "2");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "3": // 3월 게시판 클릭
                result.putString("month", "3");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "4":
                result.putString("month", "4");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "5":
                result.putString("month", "5");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "6":
                result.putString("month", "6");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "7":
                result.putString("month", "7");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "8":
                result.putString("month", "8");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "9":
                result.putString("month", "9");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "10":
                result.putString("month", "10");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "11":
                result.putString("month", "11");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            case "12":
                result.putString("month", "12");
                monthBoardFragment.setArguments(result);
                getSupportFragmentManager().beginTransaction().detach(monthBoardFragment).attach(monthBoardFragment).replace(R.id.container, monthBoardFragment).addToBackStack(null).commit();
                return;
            default:
                return;
        }

    }

}



