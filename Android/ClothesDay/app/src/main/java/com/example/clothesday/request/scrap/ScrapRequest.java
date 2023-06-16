package com.example.clothesday.request.scrap;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ScrapRequest extends StringRequest {

    final static private String URL = "http://dlsxjsptb.cafe24.com/postScrap/scrap.jsp";
    private Map<String, String> map;

    private Response.Listener<String> mListener;
    private Response.ErrorListener mErrorListener;

    public ScrapRequest(int PS_PO_ID, String PS_ME_ID, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, URL, listener, null);
        this.mListener = listener;
        this.mErrorListener = errorListener;

        map = new HashMap<>();
        map.put("PS_PO_ID", String.valueOf(PS_PO_ID));
        map.put("PS_ME_ID", PS_ME_ID);
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return map;
    }

}
