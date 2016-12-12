import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
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
    public boolean isFaulty;

    protected RmiComponent(int id, String host, int port, int n, boolean isFaulty, int value, int f) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.id = id;
        this.n = n;
        this.isFaulty = isFaulty;
        this.value = value;
        this.decided = false;
        this.f = f;

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
                randomDelay();
                broadcast(new Message(MessageType.NOTIFICATION, round, value));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            waitForMessages(MessageType.NOTIFICATION, round);

            // proposal phase
            int proposedVal = 0;
            try {
                randomDelay();
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
            randomDelay();
            decideValue(round);
            round++;
        }
    }

    @Override
    public void addNeighbours(List<RmiInterface> processes) throws RemoteException {
        neighbours.addAll(processes);
    }

    @Override
    public void broadcast(Message msg) throws RemoteException {
        System.out.println(id + " broadcasting " +  msg.type.toString() + " value: " + value);
        if (!isFaulty || Math.random() > 0.2) {
            for(RmiInterface p : neighbours) {
                if (!isFaulty || Math.random() > 0.3) {
                    p.receive(msg);
                } else {
                    System.out.println(id + " faulty did not send message");
                }
            }
        } else {
            System.out.println(id + " faulty did not broadcast any messages");
        }
    }

    @Override
    public void receive(Message msg) throws RemoteException {
        lock.lock();
        try {
            messages.add(msg);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getId() throws RemoteException {
        return id;
    }

    @Override
    public void increaseNumberOfFaulty(int f) throws RemoteException {
        this.f += f;
    }

    private void waitForMessages(MessageType type, int round) {
        System.out.println(id + " starting to wait for messages");
        int limit = n - f;
        int messageCount = countMessages(type, round);
        while(messageCount < limit) {
            messageCount = countMessages(type, round);
        }
        System.out.println(id + " finishing waiting for messages");
    }

    /*
        Counts messages of a specific types for the current round
        and deletes messages from older rounds
     */
    private int countMessages(MessageType type, int round) {
        int count = 0;
        List<Message> currentMessages = new ArrayList<Message>(messages);
        for(Message m : currentMessages) {
            if (m == null) {
                System.out.println("Error, message is null, try again");
                return count;
            }
            if(m.type == type && m.round == round) count++;

        }
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
        double chanceOfWrongValue = 1;
        if(zeros > limit) {
            int value = 0;
            if (isFaulty) {
                value = Math.random() > chanceOfWrongValue ? value : (Math.random() < 0.5 ? 1 : 2);
                System.out.println(id + " faulty sending value " + value + " instead of 0");
            }
            broadcast(new Message(MessageType.PROPOSAL, round, value));
            return 0;
        } else if (ones > limit) {
            int value = 1;
            if (isFaulty) {
                value = Math.random() > chanceOfWrongValue ? value : (Math.random() < 0.5 ? 0 : 2);
                System.out.println(id + " faulty sending value " + value + " instead of 1");
            }
            broadcast(new Message(MessageType.PROPOSAL, round, value));
            return 1;
        } else {
            int value = 2;
            if (isFaulty) {
                value = Math.random() > chanceOfWrongValue ? value : (Math.random() < 0.5 ? 0 : 1);
                System.out.println(id + " faulty sending value " + value + " instead of 2");
            }
            broadcast(new Message(MessageType.PROPOSAL, round, value));
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

    public void randomDelay() {
        if (false) return;  // control whether to use delays in general

        double rand = Math.random();
        if(rand < 0.3) {
            rand = Math.random();
            long delay = (long) (1000*rand);
            System.out.println("Doing delay of " + delay);
            try {
                Thread.sleep(delay);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
