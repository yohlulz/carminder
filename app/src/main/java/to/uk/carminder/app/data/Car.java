package to.uk.carminder.app.data;

/**
 * Created by ovidiu on 3/21/15.
 */
public class Car {
    private final String plate;
    private String name;
    private String picture;

    public Car(String plate) {
        this.plate = plate;
    }

    public String getPlate() {
        return plate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
