package com.frazzled.resplendentsquirrelscroller;

import android.os.AsyncTask;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private String mFeedURL;
    private SwipeRefreshLayout mSwipeLayout;
    private List<RssFeedModel> mFeedModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SetUPToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setLogo(R.drawable.app_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        mSwipeLayout =  findViewById(R.id.swipeRefreshLayout);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        FetchFreshFeeds();


        mSwipeLayout.setOnRefreshListener(() -> FetchFreshFeeds());
    }

    private void FetchFreshFeeds(){
        //FETCH FEEDS
        mFeedModelList = new ArrayList<>();

        // This one breaks shit: mFeedURL = "https://medium.com/feed/tag/android";
        //mFeedURL = "https://maximumfun.org/feed";
        //FetchFeedTask f = (FetchFeedTask) new FetchFeedTask().execute((Void)null);

        mFeedURL = "https://newsroom.pinterest.com/en/feed/news.xml";
        FetchFeedTask f = (FetchFeedTask) new FetchFeedTask().execute((Void)null);

        mFeedURL = "http://feed.androidauthority.com";
        f = (FetchFeedTask) new FetchFeedTask().execute((Void)null);


        //mFeedURL = "https://commonsware.com/blog/feed.atom";
        //f = (FetchFeedTask) new FetchFeedTask().execute((Void)null);

        //mFeedURL = "https://gizmodo.com/tag/android/rss";
        //f = (FetchFeedTask) new FetchFeedTask().execute((Void)null);

        //mFeedURL = "https://news.google.com/rss/search?q=android+developer&hl=en-US&gl=US&ceid=US%3Aen&x=1571747148.1512";
       //f = (FetchFeedTask) new FetchFeedTask().execute((Void)null);
    }


    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            urlLink = mFeedURL;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "https://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();

//                mFeedModelList = parseFeed(inputStream);
                mFeedModelList.addAll(parseFeed(inputStream));

                mFeedModelList.sort(new Comparator<RssFeedModel>() {
                    @Override
                    public int compare(RssFeedModel item1, RssFeedModel item2) {
                        Date date1 = item1.getPubDateDate();
                        Date date2 = item2.getPubDateDate();
                        if (date1 == null && date2 == null || date1.equals(date2)) {
                            return 0;
                        } else if (date1 == null) {
                            return -1;
                        } else if (date2 == null) {
                            return 1;
                        } else if(date1.before(date2)){
                            return 1;
                        }else {//(date2.before(date1)){
                            return -1;
                        }

                    }

                });
                return true;
            } catch (IOException | XmlPullParserException e) {
                Log.e(TAG, "Parse Feed Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {
                // Fill RecyclerView
                Log.d("PostOnExecute", "RSS Feed Filled Successfully");
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            } else {
                Toast.makeText(MainActivity.this,
                        "Enter a valid Rss feed url",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {

        String title = null;
        String link = null;
        String description = null;
        String imgLink = null;
        String pubDate = null;
        Date pubDateDate = null;
        boolean isItem = false;

        List<RssFeedModel> items = new ArrayList<>();
        SimpleDateFormat[] sdPatterns = {new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH), new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss +SSSS", Locale.ENGLISH)};
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;

                    if (description.contains("<p")){
                        Document doc = Jsoup.parse(description);
                        imgLink = doc.select("img").first().attr("src");
                        //description = doc.select("p").last().text();
                        description = doc.text();
                        //Log.d("description", description);
                    }

                } else if(name.equalsIgnoreCase("pubDate")){
                    pubDate = result;
                }

                if (title != null && link != null && description != null) {
                    if(isItem) {

                        if (pubDate != null){

                            for(SimpleDateFormat sd : sdPatterns){
                                try{
                                    pubDateDate = sd.parse(pubDate);

                                }catch (ParseException pe) {
                                    //pe.printStackTrace();
                                }
                            }
                            //Log.d("MOreDateFOrmaitting", pubDate + " " + pubDateDate);
                            RssFeedModel item = new RssFeedModel(title, imgLink, link, description, pubDateDate);
                            items.add(item);
                        } else{
                            RssFeedModel item = new RssFeedModel(title, link, description);
                            items.add(item);
                        }
                    }


                    title = null;
                    imgLink = null;
                    link = null;
                    description = null;
                    pubDate = null;
                    isItem = false;
                }
            }
            return items;
        } finally {
            inputStream.close();
        }


    }
}