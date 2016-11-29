import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class RmiComponent extends UnicastRemoteObject implements RmiInterface {

    public int id;
    public int n;

    // candidate
    private Thread candidateThread;
    public boolean elected;
    public int acks;
    public int candidateLevel;

    // ordinary
    public Message currentMaxMsg;
    public RmiInterface link;
    public List<Message> messages = new ArrayList<Message>();
    public int ordinaryLevel;
    private Thread ordinaryThread;

    protected RmiComponent(int id, String host, int port, int n) throws RemoteException, AlreadyBoundException, MalformedURLException {
        System.out.println(id + " creating component");
        this.id = id;
        this.n = n;

        // candidate
        this.elected = false;
        this.candidateLevel = -1;
        this.acks = 0;

        // ordinary
        this.ordinaryLevel = -1;
        this.currentMaxMsg = new Message(id, ordinaryLevel, null);

        // bind
        String urlName = "rmi://" + host + ":" + port + "/" + id;
        Naming.bind(urlName, this);
    }

    public void startCandidate() {
        System.out.println(id + " creating candidate thread");
        candidateThread = new Thread() {
            public void run() {
                try {
                    candidateMethod();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        createOrdinaryThreadIfMissing();
        candidateThread.start();
    }

    private void createOrdinaryThreadIfMissing() {
        //System.out.println(id + " checking if should create ordinary thread");
        if (ordinaryThread == null){
            System.out.println(id + " creating ordinary thread");
            ordinaryThread = new Thread() {
                public void run () {
                    try {
                        ordinaryMethod();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            ordinaryThread.start();
        }
    }

    public void candidateMethod() throws RemoteException, NotBoundException, MalformedURLException, InterruptedException {
        System.out.println(id + " running candidateMethod");
        List<RmiInterface> links = getAllNodes();
        int k = 1;

        while(true){
            System.out.println(id + " candidate loop level: " + candidateLevel);
            candidateLevel++;
            if(candidateLevel % 2 == 0){
                k = (int) Math.pow(2, candidateLevel/2);
                System.out.println(id + " K: " + k);
                acks = 0;

                if(links.isEmpty()){
                   elected = true;
                    System.out.println(id + " I GOT ELECTED");
                    candidateThread.join();
                    return;
                } else {
                    if(k > links.size()) k = links.size();
                    System.out.println(id + " sending " + k + " messages");
                    List<RmiInterface> subLinks = new ArrayList<RmiInterface>();
                    for(int i = 0; i < k; i++){
                        RmiInterface p = links.get(i);
                        p.receiveMessage(new Message(id, candidateLevel, this));
                        subLinks.add(p);
                    }
                    links.removeAll(subLinks);
                }
                try {
                    Thread.sleep(1000*1);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } else {
                try {
                    Thread.sleep(1000*1);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                System.out.println(id + " hoping to receive " + k + " acks");
                if(acks < k) {
                    System.out.println(id + " Did not get all acknowlegdements, NOT ELECTED, acks received: " + acks);
                    candidateThread.join();
                    return;
                }
            }
        }
    }

    private List<RmiInterface> getAllNodes() throws RemoteException, NotBoundException, MalformedURLException {
        List<RmiInterface> links = new ArrayList<RmiInterface>();
        for(int i = 0; i < n; i++){
            if (i == id) continue;
            int port = 1090 + i;
            RmiInterface p = (RmiInterface) Naming.lookup("rmi://localhost:" + port + "/" + i);
            links.add(p);
        }
        return links;
    }

    public void ordinaryMethod() throws RemoteException {
        System.out.println(id + " running ordinary method");
        while(true) {
            System.out.println(id + " ordinary loop level: " + ordinaryLevel);
            if(link != null) link.receiveAck(this);

            try {
                Thread.sleep(1000*1);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            ordinaryLevel++;
            currentMaxMsg = new Message(currentMaxMsg.id, ordinaryLevel, currentMaxMsg.sender);

            if(!messages.isEmpty()) {
                sortMessages();
                Message maxMsg = messages.get(messages.size()-1);
                System.out.println(id + " comparing mine: (level: " + currentMaxMsg.level + ", id: " + currentMaxMsg.id + ") to (level: " + maxMsg.level + ", id: " + maxMsg.id + ")");
                if(compareMessages(maxMsg, currentMaxMsg) > 0 ) { // maxMsg is bigger
                    System.out.println(id + " updated my max: (level: " + currentMaxMsg.level + ", id: " + currentMaxMsg.id + ") to (level: " + maxMsg.level + ", id: " + maxMsg.id + ")");
                    currentMaxMsg = maxMsg;
                    ordinaryLevel = maxMsg.level;
                    link = maxMsg.sender;
                } else {
                    link = null;
                }

                messages.clear();
            } else {
                link = null;
            }
        }

    }

    @Override
    public void receiveMessage(Message msg) {
        System.out.println(id + " receiving message from: " + msg.id);
        createOrdinaryThreadIfMissing();
        this.messages.add(msg);
    }

    private int compareMessages(Message m1, Message m2) {
        if(m1.level < m2.level) return -1;
        else if(m1.level > m2.level) return 1;
        else return ((Integer) m1.id).compareTo((Integer) m2.id); // levels equal, go by id
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
    public void receiveAck(RmiInterface sender) throws RemoteException {
        System.out.println(id + " receiving ack from " + sender.getId());
        acks++;
    }

    @Override
    public int getId() throws RemoteException {
        return id;
    }
}
