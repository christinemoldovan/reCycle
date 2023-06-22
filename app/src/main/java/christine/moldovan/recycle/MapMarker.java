package christine.moldovan.recycle;

public class MapMarker {
    String primaryAddress, secondaryAddress, titleAddress, descriptionAddress;
    Double latitude, longitude;
    String recyclePoint;

    public MapMarker(Double latitude, Double longitude, String primaryAddress, String secondaryAddress, String titleAddress, String descriptionAddress, String recyclePoint) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.primaryAddress = primaryAddress;
        this.secondaryAddress = secondaryAddress;
        this.titleAddress = titleAddress;
        this.descriptionAddress = descriptionAddress;
        this.recyclePoint = recyclePoint;
    }

    public MapMarker() {
    }

    public String getRecyclePoint() {
        return recyclePoint;
    }

    public void setRecyclePoint(String recyclePoint) {
        this.recyclePoint = recyclePoint;
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

    public String getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(String primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public String getSecondaryAddress() {
        return secondaryAddress;
    }

    public void setSecondaryAddress(String secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
    }

    public String getTitleAddress() {
        return titleAddress;
    }

    public void setTitleAddress(String titleAddress) {
        this.titleAddress = titleAddress;
    }

    public String getDescriptionAddress() {
        return descriptionAddress;
    }

    public void setDescriptionAddress(String descriptionAddress) {
        this.descriptionAddress = descriptionAddress;
    }
}
