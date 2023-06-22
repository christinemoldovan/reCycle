package christine.moldovan.recycle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Comparator;

public class MarkerOptionsComparator implements Comparator<MarkerOptions> {
    private LatLng currentLocation;

    public MarkerOptionsComparator(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public int compare(MarkerOptions m1, MarkerOptions m2) {
        LatLng pos1 = m1.getPosition();
        LatLng pos2 = m2.getPosition();

        double distance1 = getDistance(pos1.latitude, pos1.longitude, currentLocation.latitude, currentLocation.longitude);
        double distance2 = getDistance(pos2.latitude, pos2.longitude, currentLocation.latitude, currentLocation.longitude);

        if (distance1 < distance2) {
            return -1;
        } else if (distance1 > distance2) {
            return 1;
        } else {
            return 0;
        }
    }

    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6378; // in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
