public class Message {
    public int id;
    public int level;
    public RmiInterface sender;

    public Message(int id, int level, RmiInterface sender) {
        this.id = id;
        this.level = level;
        this.sender = sender;
    }
}
