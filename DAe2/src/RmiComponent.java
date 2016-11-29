import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RmiComponent extends UnicastRemoteObject implements RmiInterface, Runnable {

    public int id;
    public int n;

    // candidate
    private Thread candidateThread;

    // ordinary
    public Message currentMaxMsg;
    public RmiInterface link;
    public List<Message> messages;
    public int ordinaryLevel;
    private Thread ordinaryThread;

    protected RmiComponent(int id, String host, int port, int n) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.id = id;
        this.n = n;

        // ordinary
        this.ordinaryLevel = -1;
        this.currentMaxMsg = new Message(id, ordinaryLevel, null);

        String urlName = "rmi://" + host + ":" + port + "/" + id;
        Naming.bind(urlName, this);
    }

    public void ordinary() {
        if(link != null) link.receiveAck();
        sortMessages();

        while(true) {
            ordinaryLevel++;
            Message maxMsg = messages.get(messages.size()-1);
            if(compareMessages(maxMsg, currentMaxMsg) > 0 ) { // maxMsg is bigger
                currentMaxMsg = maxMsg;
                link = maxMsg.sender;
            } else {
                link = null;
            }

            messages.clear();
        }

    }

    @Override
    public void method() {
        System.out.println(id + " hello");
    }

    @Override
    public void receiveMessage(Message msg) {
        //if(ordinaryThread == null)
        this.messages.add(msg);
    }

    private int compareMessages(Message m1, Message m2) {
        if(m1.level < m2.level) return -1;
        else if(m1.level < m2.level) return 1;
        else if(m1.level == m2.level) return ((Integer) m1.id).compareTo((Integer) m2.id);
        return 0;
    }

    private void sortMessages() {
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                return compareMessages(m1, m2);
            }
        });
    }

    @Override
    public void receiveAck() {

    }

    @Override
    public void run() {
        System.out.println(id + " running");
    }
}
