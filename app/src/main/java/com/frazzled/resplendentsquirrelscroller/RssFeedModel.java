package com.frazzled.resplendentsquirrelscroller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class RssFeedModel {
    private String title;
    private String image;
    private String link;
    private String description;
    private Date pubDate;
    private SimpleDateFormat sdf;

    public RssFeedModel(String title, String image, String link, String description, Date pubDate) {
        this.title = title;
        this.link = link;
        this.image = image;
        this.description = description;
        this.pubDate = pubDate;
        sdf = new SimpleDateFormat("EEE, MMM dd yyyy hh:mma", Locale.ENGLISH);
    }
    public RssFeedModel(String title, String image, String link, String description) {
        this.title = title;
        this.link = link;
        this.image = image;
        this.description = description;
        this.pubDate = null;
    }
    public RssFeedModel(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPubDateString() {
        if(pubDate != null){
            //Log.d("DateFormatting", pubDate + " " + sdf.format(pubDate));
            return sdf.format(pubDate);
        }else{
            return "Not Able To Get PubDate";
        }

    }
    public Date getPubDateDate() {
        return pubDate;

    }
    public void setPubDate(String pubDate) {
        try{
        this.pubDate = sdf.parse(pubDate);
        }catch (ParseException pe){
            pe.printStackTrace();
        }
    }
}
