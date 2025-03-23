public class Node {
    private int longitude;
    private int latitude;
    private final int number;

    public Node(int longitude, int latitude, int number) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.number = number;
    }

    public int getLongitude() {
        return longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public int getNumber() {
        return number;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }
}