package christine.moldovan.recycle;

public class UserFavMarkers {
    String userUID;
    Double latitude, longitude;

    public UserFavMarkers(String userUID, Double latitude, Double longitude) {
        this.userUID = userUID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public UserFavMarkers() {
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
