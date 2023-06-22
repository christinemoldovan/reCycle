package christine.moldovan.recycle;

public class Article {
    private String url;
    private String headline;
    private String imageUrl;
    private long date;


    public Article(String url, String headline, String imageUrl, long date) {
        this.url = url;
        this.headline = headline;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public Article() {

    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

}
