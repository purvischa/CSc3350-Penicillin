package model;

/**
 * Represents a state entity with an ID and display name.
 */
public class State {
    /** Unique identifier for the state. */
    private final int stateId;
    /** Full name of the state. */
    private final String nameOfState;

    /**
     * Constructs a State object.
     * @param stateId Unique state identifier.
     * @param nameOfState Display name of the state.
     */
    public State(int stateId, String nameOfState) {
        this.stateId     = stateId;
        this.nameOfState = nameOfState;
    }

    /**
     * @return the state’s unique ID.
     */
    public int getStateId() {
        return stateId;
    }

    /**
     * @return the state’s name.
     */
    public String getNameOfState() {
        return nameOfState;
    }

    /**
     * Returns the state's name when this object is printed.
     * @return nameOfState
     */
    @Override
    public String toString() {
        return nameOfState;
    }
}