import java.io.Serializable;

/**
 * A class to contain the message sent from candidates to ordinary processes
 */
public class Message implements Serializable {
    public int id;
    public int level;
    public RmiInterface sender;
    private static final long serialVersionUID = 7526471155622776147L;

    public Message(int id, int level, RmiInterface sender) {
        this.id = id;
        this.level = level;
        this.sender = sender;
    }
}
