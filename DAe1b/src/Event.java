import java.io.Serializable;

/**
 * Contains information about the clock vector for the sender of a message.
 */
public class Event implements Serializable {
    int processId;
    int[] clock;

    private static final long serialVersionUID = 7526471155622776147L;

    public Event(int processId, int[] clock) {
        this.processId = processId;
        this.clock = clock;
    }
}
