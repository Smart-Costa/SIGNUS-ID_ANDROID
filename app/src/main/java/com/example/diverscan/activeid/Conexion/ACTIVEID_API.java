package com.example.diverscan.activeid.Conexion;

import android.content.Context;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ACTIVEID_API {
    private static final String BASE_URL = "http://3.85.211.90:70/WCF-IMAS/Service1.svc";
    //private static final String BASE_URL = "http://10.211.136.196/WCFActivos_EMERSON/Service1.svc";
    //private static final String BASE_URL = "http://172.16.1.198/WCFActiveIntegration/Service1.svc";


    private final AsyncHttpClient AsyncClient;

    public ACTIVEID_API() {
        this.AsyncClient = new AsyncHttpClient();
    }

    public void get(String url, AsyncHttpResponseHandler responseHandler) {
        this.AsyncClient.get(BASE_URL + url, responseHandler);
    }

    public void post(Context context, String url, StringEntity entity, AsyncHttpResponseHandler responseHandler) {
        this.AsyncClient.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}

