package com.example.clothesday.request.user;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class FindPwRequest extends StringRequest {

    final static private String URL = "http://dlsxjsptb.cafe24.com/find/findPw.jsp";

    private Map<String, String> map;
    private Response.Listener<String> mListener;
    private Response.ErrorListener mErrorListener;

    public FindPwRequest(String ME_ID, String ME_PW, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, URL, listener, null);
        this.mListener = listener;
        this.mErrorListener = errorListener;

        map = new HashMap<>();
        map.put("ME_ID", ME_ID);
        map.put("ME_PW", ME_PW);
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return map;
    }
}
