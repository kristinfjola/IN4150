/**
 * Contains information about a message and the clock vector from the sender of the message.
 */
public class MessageEvent {
    public Event event;
    public String message;

    public MessageEvent(Event event, String message) {
        this.event = event;
        this.message = message;
    }
}
