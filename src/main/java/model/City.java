package model;

/**
 * Represents a city entity with an ID and display name.
 */
public class City {
    /** Unique identifier for the city. */
    private final int cityId;
    /** Display name of the city. */
    private final String nameOfCity;

    /**
     * Constructs a City object.
     * @param cityId Unique city identifier.
     * @param nameOfCity Display name of the city.
     */
    public City(int cityId, String nameOfCity) {
        this.cityId     = cityId;
        this.nameOfCity = nameOfCity;
    }

    /**
     * @return the city's unique ID.
     */
    public int getCityId() {
        return cityId;
    }

    /**
     * @return the city's name.
     */
    public String getNameOfCity() {
        return nameOfCity;
    }

    /**
     * Returns the city's name when this object is printed.
     * @return nameOfCity
     */
    @Override
    public String toString() {
        return nameOfCity;
    }
}