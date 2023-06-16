package com.example.clothesday.fragment;

import static com.example.clothesday.common.location.Address.getAddress;
import static com.example.clothesday.common.weather.TransLocalPoint.convertGRID_GPS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.clothesday.Adapter.recyclerView.OneLineLongRecyclerViewAdapter;
import com.example.clothesday.DAO.PostDTO;
import com.example.clothesday.DAO.UserDTO;
import com.example.clothesday.R;
import com.example.clothesday.common.location.GpsTracker;
import com.example.clothesday.common.permission.PermissionSupport;
import com.example.clothesday.common.time.DayOfWeek;
import com.example.clothesday.common.weather.LatXLngY;
import com.example.clothesday.request.clothes.RecommendedClothesRequest;
import com.example.clothesday.request.weather.WeatherRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        
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

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private String endPoint2 =  "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"; //단기예보
    private String endPoint1 =  "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst"; //초단기예보
    private String serviceKey = ""; // api 서비스키
    private String pageNo = "1";
    private String numOfRows = "70";
    private String numOfRows2 = "250";
    private String base_date = "20220606"; //초단기
    private String base_date2 = "20220606"; //단기
    private String base_time2 = "2300";//단기
    private String base_time = "1500"; //초단기
    private String nx = "98";
    private String ny = "77";
    private String URL ="http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=&numOfRows=" + numOfRows + "&pageNo=1&base_date=" + base_date + "&base_time=" + base_time + "&nx=" + nx +"&ny=" + ny + "&dataType=JSON";
    private String URL2 ="http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=&numOfRows=" + numOfRows2 + "&pageNo=1&base_date=" + base_date + "&base_time=" + base_time + "&nx=" + nx +"&ny=" + ny + "&dataType=JSON";
    public static int TO_GRID = 0;
    public static int TO_GPS = 1;

    private int end = 0;

    //데이터
    private Object TMX, TMN, POP, T1H, REH, SKY, RAIN;
    private String addr; //주소
    private String today;
    private String PO_TAG;

    private RecyclerView recyclerView;
    private ArrayList<PostDTO> postData = new ArrayList<PostDTO>();
    private  ArrayList<UserDTO> userData = new ArrayList<UserDTO>();
    private OneLineLongRecyclerViewAdapter OneLineLongRecyclerViewAdapter;

    //뷰
    private TextView temp, high, low, humidity, rainper, location, weather_get_text;
    private ImageView sky;
    private ViewGroup rootView;
    private LottieAnimationView animationView;

    private FragmentManager fm;

    private PermissionSupport permission; // 권한을 위한 객체

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        temp = rootView.findViewById(R.id.main_weather_temp);
        high = rootView.findViewById(R.id.main_weather_high);
        low = rootView.findViewById(R.id.main_weather_low);
        humidity = rootView.findViewById(R.id.main_weather_humidity);
        rainper = rootView.findViewById(R.id.main_weather_rainper);
        sky = rootView.findViewById(R.id.main_weather_icon);
        location = rootView.findViewById(R.id.main_weather_location);
        animationView = rootView.findViewById(R.id.main_lottie_animation);
        weather_get_text = rootView.findViewById(R.id.main_weather_get_text);
        recyclerView = rootView.findViewById(R.id.main_recyclerview);

        //리사이클러뷰
        LinearLayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        fm = getParentFragmentManager();

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            permissionCheck();
        }
        getWeather();

        return rootView;
    }

    // 권한 체크
    private void permissionCheck() {
        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(activity, activity);

        // 권한 체크 후 리턴이 false로 들어오면
        if (!permission.checkPermission()){
            permission.requestPermission();
            getWeather();
        }
    }



    private void getWeather() { // 날씨 불러오기
        
        SimpleDateFormat sdf_today = new SimpleDateFormat("yyyy.MM.dd");

        long now = System.currentTimeMillis();
        today = sdf_today.format(now); 
        Date date = new Date(now);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        base_date = sdf.format(date);  //  현재 날짜
        base_date2 = sdf.format(date); //  현재 날짜

        SimpleDateFormat sdf2 = new SimpleDateFormat("HH00");
        String real_time = sdf2.format(cal.getTime()); // 현재 시각
        cal.add(Calendar.HOUR, -1);
        base_time = sdf2.format(cal.getTime()); // 현재 시각에서 1시간 전 시각

        if (base_time.equals("2300")) { // 00시일 경우 초단기 날짜 변경
            base_date = sdf.format(cal.getTime());
            cal.add(Calendar.DATE, -1);
            base_date2 = sdf.format(cal.getTime());
        } else {
            cal.add(Calendar.DATE, -2);
            base_date2 = sdf.format(cal.getTime());
        }

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            GpsTracker gpsTracker = new GpsTracker(activity);
            double latx = gpsTracker.getLatitude(); // 위도
            double laty = gpsTracker.getLongitude(); // 경도

            addr = getAddress(activity, latx, laty);

            LatXLngY tmp = convertGRID_GPS(TO_GRID, latx, laty);

            nx = String.valueOf((int) tmp.x);
            ny = String.valueOf((int) tmp.y);

            URL = endPoint1 + "?serviceKey=&numOfRows=" + numOfRows + "&pageNo=1&base_date=" + base_date + "&base_time=" + base_time + "&nx=" + nx + "&ny=" + ny + "&dataType=JSON";
            URL2 = endPoint2 + "?serviceKey=&numOfRows=" + numOfRows2 + "&pageNo=1&base_date=" + base_date2 + "&base_time=" + base_time2 + "&nx=" + nx + "&ny=" + ny + "&dataType=JSON";


            System.out.println(URL);
            System.out.println(URL2);

            animationView.setVisibility(View.VISIBLE);
            animationView.setSpeed(1f);
            animationView.playAnimation();
            weather_get_text.setVisibility(View.VISIBLE);

            new Thread(() -> {
                try {
                    //전날부터 데이터를 조회하여 오늘 날씨 얻기
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject obj = new JSONObject(response);

                                // response 키를 가지고 데이터를 파싱
                                JSONObject parse_response = (JSONObject) obj.get("response");
                                // response 로 부터 body 찾기
                                JSONObject parse_body = (JSONObject) parse_response.get("body");
                                // body 로 부터 items 찾기
                                JSONObject parse_items = (JSONObject) parse_body.get("items");

                                // items로 부터 itemlist 를 받기
                                JSONArray parse_item = (JSONArray) parse_items.get("item");
                                String category;
                                JSONObject weather; // parse_item은 배열형태이기 때문에 하나씩 데이터를 하나씩 가져올때 사용

                                String day = "";
                                String time = "";
                                for (int i = 0; i < parse_item.length(); i++) {
                                    weather = (JSONObject) parse_item.get(i);
                                    Object fcstValue = weather.get("fcstValue");
                                    Object fcstDate = weather.get("fcstDate");
                                    Object fcstTime = weather.get("fcstTime");

                                    category = (String) weather.get("category");
                                    // 출력
                                    if (!day.equals(fcstDate.toString())) {
                                        day = fcstDate.toString();
                                    }
                                    if (!time.equals(fcstTime.toString())) {
                                        time = fcstTime.toString();
                                    }
                                    if (category.equals("T1H") && fcstTime.equals(real_time)) {
                                        T1H = fcstValue;
                                    }
                                    if (category.equals("REH") && fcstTime.equals(real_time)) { //습도
                                        REH = fcstValue;
                                    }
                                    if (category.equals("SKY") && fcstTime.equals(real_time)) { // 하늘상태(SKY) 코드 : 맑음(1), 구름많음(3), 흐림(4)
                                        SKY = fcstValue;
                                    }if (category.equals("PTY") && fcstTime.equals(real_time)) {  //강수형태(PTY) 코드 : (초단기) 없음(0), 비(1), 비/눈(2), 눈(3), 빗방울(5), 빗방울눈날림(6), 눈날림(7)
                                        RAIN = fcstValue;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (end == 1 ){
                                setWeatherInfo();
                            } else {
                                end = 1;
                            }
                        }
                    };
                    WeatherRequest WeatherRequest = new WeatherRequest(URL, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(activity);
                    queue.add(WeatherRequest);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                try {
                    Response.Listener<String> responseListener2 = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject obj = new JSONObject(response);


                                JSONObject parse_response = (JSONObject) obj.get("response");

                                JSONObject parse_body = (JSONObject) parse_response.get("body");

                                JSONObject parse_items = (JSONObject) parse_body.get("items");

                                JSONArray parse_item = (JSONArray) parse_items.get("item");
                                String category;
                                JSONObject weather;

                                String day = "";
                                String time = "";
                                for (int i = 0; i < parse_item.length(); i++) {
                                    weather = (JSONObject) parse_item.get(i);
                                    Object fcstValue = weather.get("fcstValue");
                                    Object fcstDate = weather.get("fcstDate");
                                    Object fcstTime = weather.get("fcstTime");

                                    category = (String) weather.get("category");
                                    // 출력
                                    if (!day.equals(fcstDate.toString())) {
                                        day = fcstDate.toString();
                                    }
                                    if (!time.equals(fcstTime.toString())) {
                                        time = fcstTime.toString();
                                    }
                                    if (category.equals("TMX") ) { // 최고기온 TMX
                                        TMX = fcstValue;
                                    }
                                    if (category.equals("TMN")) { // 최저기온 TMN
                                        TMN = fcstValue;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (end == 1 ){
                               setWeatherInfo();
                            } else {
                                end = 1;
                            }
                        }
                    };
                    WeatherRequest WeatherRequest2 = new WeatherRequest(URL2, responseListener2);
                    RequestQueue queue2 = Volley.newRequestQueue(activity);
                    queue2.add(WeatherRequest2);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


            /*
             * 항목값	항목명	단위
             * POP	강수확률	 %
             * PTY	강수형태	코드값
             * R06	6시간 강수량	범주 (1 mm)
             * REH	습도	 %
             * S06	6시간 신적설	범주(1 cm)
             * SKY	하늘상태	코드값
             * T3H	3시간 기온	 ℃
             * TMN	일 최저기온	℃	10
             * TMP	1시간 기온	℃	10
             * TMN	아침 최저기온	 ℃
             * TMX	낮 최고기온	 ℃
             * UUU	풍속(동서성분)	 m/s
             * VVV	풍속(남북성분)	 m/s
             * WAV	파고	 M
             * VEC	풍향	 m/s
             * WSD	풍속	1
             */
        } else {
            weather_get_text.setText("위치 정보 접근 권한을 허용해 주세요.");
            weather_get_text.setVisibility(View.VISIBLE);
        }


    }

    private void setWeatherInfo() {
        DayOfWeek dayOfWeek = new DayOfWeek();
        if (T1H != null ) {
            temp.setText(String.valueOf(T1H));
        } else {
            temp.setText("정보 없음");
        }
        humidity.setText("습도\n" + REH + "%");
        // 하늘상태(SKY) 코드 : 맑음(1), 구름많음(3), 흐림(4)
        if (SKY != null) {
            if (SKY.equals("1"))
                sky.setImageResource(R.drawable.sun);
            else if (SKY.equals("3"))
                sky.setImageResource(R.drawable.sun_cloud);
            else if (SKY.equals("4"))
                sky.setImageResource(R.drawable.cloud);
        }

        if (RAIN != null) { //`강수형태(PTY) 코드 : (초단기) 없음(0), 비(1), 비/눈(2), 눈(3), 빗방울(5), 빗방울눈날림(6), 눈날림(7)
            if (RAIN.equals("1"))
                sky.setImageResource(R.drawable.rain);
            else if (RAIN.equals("3"))
                sky.setImageResource(R.drawable.snow);
        }
//        if (POP != null ) {
//            rainper.setText("강수 확률\n" + POP + "%");
//        } else {
//            rainper.setText("강수 확률\n" + "정보 없음");
//        }
        if (TMX != null ) {
                high.setText("최고기온\n" + TMX);
            getClothesRecommend();
        } else {
            high.setText("최고기온\n" + "정보 없음");
        }
        if (TMN != null ) {
                low.setText("최저기온\n" + TMN);
        } else {
            low.setText("최저기온\n" + "정보 없음");
        }

        try {
            location.setText(addr + "\n\n" + today + " " + dayOfWeek.getDayOfWeek(today));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        animationView.pauseAnimation();
        animationView.setVisibility(View.GONE);
        weather_get_text.setVisibility(View.GONE);
    }

    private void getClothesRecommend() {

        try {
            PO_TAG = String.valueOf((int)Double.parseDouble(TMX.toString()));
            System.out.println(TMX);
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
        System.out.println(PO_TAG);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    postData = new ArrayList<PostDTO>();

                    userData = new ArrayList<UserDTO>();

                    JSONObject result = new JSONObject(response);
                    JSONArray jsonArray = result.getJSONArray("post");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        PostDTO post = new PostDTO();
                        UserDTO user = new UserDTO();
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        post.setPO_ID(jObject.getInt("PO_ID"));
                        post.setPO_REG_DA(jObject.getString("PO_REG_DA")); // 완
                        post.setPO_CATE(jObject.getString("PO_CATE")); //완
                        post.setPO_TAG(jObject.getString("PO_TAG"));
                        post.setPO_PIC(jObject.getString("PO_PIC")); //완
                        post.setPO_CON(jObject.getString("PO_CON")); //완
                        post.setPO_ME_ID(jObject.getString("PO_ME_ID"));
                        post.setPO_LIKE(jObject.getInt("PO_LIKE")); // 완
                        user.setME_NICK(jObject.getString("ME_NICK")); //완
                        user.setME_PIC(jObject.getString("ME_PIC")); //완
                        userData.add(user);
                        postData.add(post);

                    }
                    OneLineLongRecyclerViewAdapter = new OneLineLongRecyclerViewAdapter(context, postData, userData, activity, (fm==null)?getParentFragmentManager():fm);
                    recyclerView.setAdapter(OneLineLongRecyclerViewAdapter);

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
        RecommendedClothesRequest RecommendedClothesRequest =  new RecommendedClothesRequest(PO_TAG, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(RecommendedClothesRequest);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
}