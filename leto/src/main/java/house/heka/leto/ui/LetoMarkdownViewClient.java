package house.heka.leto.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import house.heka.leto.HermesActivity;
import house.heka.leto.LetoActivity;
import house.heka.leto.R;

/**
 * Created by aron2 on 3/20/2017.
 */

public class LetoMarkdownViewClient extends WebViewClient {

    private final LetoActivity mActive;

    public LetoMarkdownViewClient(LetoActivity active) {
        mActive = active;
    }
    // you tell the webclient you want to catch when a url is about to load
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, final WebResourceRequest url){
        String linkbase = url.getUrl().getHost();
        linkbase = linkbase.replaceAll("(.+?)\\/.*","$1");
        new AlertDialog.Builder(mActive)
                .setTitle("Open Link")
                .setMessage("Are you sure you want to view the link from "+linkbase+"?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.getUrl().toString()));
                        mActive.startActivity(browserIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.launcher)
                .show();
        return true;
    }

    // you tell the webclient you want to catch when a url is about to load
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, final String  url){
        String linkbase = url.replaceAll("https?:\\/\\/","");
        linkbase = linkbase.replaceAll("(.+?)\\/.*","$1");
        new AlertDialog.Builder(mActive)
                .setTitle("Open Link")
                .setMessage("Are you sure you want to view the link from "+linkbase+"?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        mActive.startActivity(browserIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.launcher)
                .show();
        return true;
    }
    // here you execute an action when the URL you want is about to load
    @Override
    public void onLoadResource(WebView  view, final String  url){

    }
}
