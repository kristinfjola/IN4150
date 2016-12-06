import java.io.Serializable;

public class Message implements Serializable  {
    public MessageType type;
    int round;
    int value;
    private static final long serialVersionUID = 7526471155622776147L;

    public Message(MessageType type, int round, int value) {
        this.type = type;
        this.round = round;
        this.value = value;
    }
}
