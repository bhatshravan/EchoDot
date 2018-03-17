package bs.inc.echodot.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import bs.inc.echodot.R;


/**
 * Created by Shravan on 19-09-2016.
 */public class WEBSS extends Fragment {
    public WEBSS() {
    }


    String url = "",strtext="";
    WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_web, container,
                false);

        url="https://console.dialogflow.com/api-client/demo/embedded/8670b99d-5452-4eb3-939c-1480b1ad4b92";

        try {
            strtext = getArguments().getString("url");
            url=strtext;
        }
        catch (Exception E)
        {
            Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
        }
        if ( !isNetworkAvailable() ) {

            Toast.makeText(getActivity(), "No Internet!", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
            builder1.setMessage("Please connect to internet and then click reload");
            builder1.setCancelable(true);

            builder1.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder1.setPositiveButton(
                    "Reload",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Fragment Frog = new WEBSS();
                            Bundle ar = new Bundle();
                            ar.putString("url",url);
                            Frog.setArguments(ar);
                            FragmentManager frgManager = getFragmentManager();
                            frgManager.beginTransaction().replace(R.id.container_body, Frog)
                                    .commit();

                        }
                    }
            );
            AlertDialog alert11 = builder1.create();
            alert11.show();

        }

        webView = (WebView) view.findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
       webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.getSettings().setAllowFileAccess( true );
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        /*webView.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
                    if (webView.getUrl() == url){
                        return false;
                    }
                    webView.goBack();
                    return true;
                }
                return false;
            }
        });
        */

        return view;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService( Activity.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap
                favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String
                url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            super.onPageFinished(view, url);
        }
    }
}
