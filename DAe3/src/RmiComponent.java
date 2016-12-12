import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RmiComponent extends UnicastRemoteObject implements RmiInterface, Runnable {

    public int id;
    public int value;
    public List<RmiInterface> neighbours = new ArrayList<RmiInterface>();
    public List<Message> messages = new ArrayList<Message>();
    public int n;
    public int f;
    public boolean decided;
    public final Lock lock = new ReentrantLock();

    protected RmiComponent(int id, String host, int port, int n, int f, int value) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.id = id;
        this.n = n;
        this.f = f;
        this.value = value;
        this.decided = false;

        // bind
        String urlName = "rmi://" + host + ":" + port + "/" + id;
        Naming.bind(urlName, this);

        System.out.println(id + " created component with value: " + value);
    }

    @Override
    public void run() {
        System.out.println(id + " running");

        int round = 1;
        while(true) {
            System.out.println(id + " round: " + round);
            // notification phase
            try {
                broadcast(new Message(MessageType.NOTIFICATION, round, value));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            waitForMessages(MessageType.NOTIFICATION, round);

            // proposal phase
            int proposedVal = 0;
            try {
                proposedVal = proposeValue(round);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            System.out.println(id + " proposed value: " + proposedVal);
            if (decided) {
                System.out.println(id + " STOP, decided on value: " + proposedVal);
                return;
            } else {
                waitForMessages(MessageType.PROPOSAL, round);
            }

            // decision phase
            decideValue(round);
            round++;
        }
    }

    @Override
    public void addNeighbours(List<RmiInterface> processes) throws RemoteException {
        /*for(RmiInterface p : processes) {
            if(p.getId() != id) {
                neighbours.add(p);
            }
        }*/
        neighbours.addAll(processes);
    }

    @Override
    public void broadcast(Message msg) throws RemoteException {
        System.out.println(id + " broadcasting " +  msg.type.toString() + " value: " + value);
        for(RmiInterface p : neighbours) {
            p.receive(msg);
        }
    }

    @Override
    public void receive(Message msg) throws RemoteException {
        //System.out.println(id + " receiving message type " + msg.type.toString());
        try {
            Thread.sleep(10);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        lock.lock();
        try {
            messages.add(msg);
        } finally {
            lock.unlock();
        }
        //System.out.println(id + " messages size");
        try {
            Thread.sleep(10);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public int getId() throws RemoteException {
        return id;
    }

    private void waitForMessages(MessageType type, int round) {
        System.out.println(id + " starting to wait");
        int limit = n - f;
        int messageCount = countMessages(type, round);
        while(messageCount < limit) {
            messageCount = countMessages(type, round);
        }
        System.out.println(id + " finishing wait");
    }

    /*
        Counts messages of a specific types for the current round
        and deletes messages from older rounds
     */
    private int countMessages(MessageType type, int round) {
        int count = 0;
        //List<Message> oldMessages = new ArrayList<Message>();
        List<Message> currentMessages = new ArrayList<Message>(messages);
        for(Message m : currentMessages) {
            //if(m.round < round) oldMessages.add(m);
            if (m == null) {
                System.out.println("MESSAGE IS NULL");
            }
            if(m.type == type && m.round == round) count++;

        }
        //messages.removeAll(oldMessages);
        return count;
    }

    private int proposeValue(int round) throws RemoteException {
        int zeros = 0;
        int ones = 0;
        List<Message> currentMessages = new ArrayList<Message>(messages);
        for(Message m : currentMessages) {
            if (m.type == MessageType.NOTIFICATION && m.round == round) {
                if(m.value == 0) zeros ++;
                else if (m.value == 1) ones++;
            }
        }

        System.out.println(id + " counted notifications: zeros " + zeros + " ones " + ones);

        int limit = (n+f)/2;
        if(zeros > limit) {
            broadcast(new Message(MessageType.PROPOSAL, round, 0));
            return 0;
        } else if (ones > limit) {
            broadcast(new Message(MessageType.PROPOSAL, round, 1));
            return 1;
        } else {
            broadcast(new Message(MessageType.PROPOSAL, round, 2));
            return 2;
        }
    }

    private void decideValue(int round) {
        int zeros = 0;
        int ones = 0;
        List<Message> currentMessages = new ArrayList<Message>(messages);
        for(Message m : currentMessages) {
            if (m.type == MessageType.PROPOSAL && m.round == round) {
                if(m.value == 0) zeros ++;
                else if (m.value == 1) ones++;
            }
        }
        System.out.println(id + " counted proposals: zeros " + zeros + " ones " + ones);

        int limit = f;
        if (zeros > limit || ones > limit) {
            if (zeros > limit) {
                this.value = 0;
                if (zeros > 3*limit) {
                    decided = true;
                    System.out.println(id + " decided on 0");
                    return;
                }
            }
            else if (ones > limit) {
                this.value = 1;
                if (ones > 3*limit) {
                    decided = true;
                    System.out.println(id + " decided on 1");
                    return;
                }
            }
        } else {
            this.value = Math.random() < 0.5 ? 0 : 1;
            System.out.println(id + " set random value of: " + this.value);
        }
    }
}
