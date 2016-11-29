import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the messaging algorithm
 */
public class RmiObject extends UnicastRemoteObject implements RmiInterface, Runnable {
    public int id;
    public String name;
    public int[] clock; // vector clock
    // the queue can be a list since there is no priority except for the condition canDeliver which is checked
    private List<MessageEvent> queue = new ArrayList<MessageEvent>();

    protected RmiObject(int id, int port, int totalProcesses) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.id = id;
        this.name = "P" + id;
        this.clock = new int[totalProcesses];

        String urlName = "rmi://localhost:" + port + "/" + name;
        Naming.bind(urlName, this);
    }

    /*
        Receives message and tries to deliver it and also other messages afterwards
     */
    @Override
    public void receiveMessage(String m, Event e) throws RemoteException {
        System.out.println(this.name + " received a message from P" + e.processId);
        printClocks(clock, e.clock);
        MessageEvent messageEvent = new MessageEvent(e, m);

        if (canDeliver(e)) {
            deliverMessage(messageEvent);
            deliverMessagesInQueue();

        } else {
            System.out.println("Added message in queue \n");
            queue.add(messageEvent);
        }
    }

    /*
        Broadcasts message to the receivers in receivers with a delay between messages
     */
    @Override
    public void broadCastMessage(String m, List<RmiInterface> receivers) throws RemoteException {
        clock[id]++;
        for(RmiInterface r : receivers) {
            broadCastMessageToSingleReceiver(m, r);
            try {
                Thread.sleep(1000*6);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /*
        Send a message to receiver
     */
    private void broadCastMessageToSingleReceiver(String m, RmiInterface receiver) throws RemoteException {
        receiver.receiveMessage(m, new Event(id, clock));
    }

    /*
        Delivers single message by printing it and updating vector clock
     */
    private void deliverMessage(MessageEvent me) {
        System.out.println("Delivering message \"" + me.message + "\"");
        clock[me.event.processId] += 1;
        queue.remove(me);
        printClocks(clock, me.event.clock);
        System.out.println("");
    }

    /*
        Returns true if the vector clock is up to date with the one from the event, else false
     */
    private boolean canDeliver(Event e) {
        for(int i = 0; i < clock.length; i++) {
            int val = i == e.processId ? clock[i] + 1 : clock[i];
            if(val < e.clock[i]) return false;
        }
        return true;
    }

    /*
        Delivers all messages in queue that are in time to be delivered
     */
    private void deliverMessagesInQueue() {
        List<MessageEvent> eventsToRemove = new ArrayList<MessageEvent>();
        for(MessageEvent me : queue) {
            if(canDeliver(me.event)) {
                eventsToRemove.add(me);
            }
        }
        for(MessageEvent me : eventsToRemove) {
            deliverMessage(me);
        }
    }

    /*
        Print clocks to be able to check the causal ordering and correctness
     */
    private void printClocks(int[] own, int[] sender) {
        System.out.println("My clock: ");
        for(int i = 0; i < own.length; i++) {
            System.out.print(own[i] + " ");
        }
        System.out.println("");
        System.out.println("Sender clock: ");
        for(int i = 0; i < sender.length; i++) {
            System.out.print(sender[i] + " ");
        }
        System.out.println("");
    }

    @Override
    public void run() {
        System.out.println("Node started: " + this.name);
    }
}
