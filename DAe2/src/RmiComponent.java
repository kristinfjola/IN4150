import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;


public class RmiComponent extends UnicastRemoteObject implements RmiInterface, Runnable {

    public int id;
    public int n;
    public int level;
    public boolean elected;
    public int acks;
    Thread thread

    protected RmiComponent(int id, String host, int port, int n) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.id = id;
        this.n = n;
        this.elected = false;
        String urlName = "rmi://" + host + ":" + port + "/" + id;
        Naming.bind(urlName, this);


    }

    public void candidateMethod(){
        HashMap<Integer, RmiInterface> links = getAllNodes();
        level = -1;
        int k = 1;
        boolean stop = false;
        acks = 0;

        while(!stop){
            level++;
            k = 2^(level / 2);
            if(level % 2 == 0){
                if(links.isEmpty()){
                   elected = true;
                } else {
                    if(k > links.size()) k = links.size();
                    for(int i = 0; i < k; i++){
                        RmiInterface p = links.get(i);
                        p.sendMessage();
                        links.remove(i);
                    }
                }
            } else {
                if(acks < k) stop = true;
            }
        }
    }

    private HashMap<Integer, RmiInterface> getAllNodes(){
        HashMap<Integer, RmiInterface> links = new HashMap<Integer, RmiInterface>();
        for(int i = 0; i < n; i++){
            int port = 1090 + i;
            RmiInterface p = null;
            p = (RmiInterface) Naming.lookup("rmi://localhost:" + port + "/" + i);
            links.put(i, p);
        }
        return links;
    }

    @Override
    public void method() {
        System.out.println(id + " hello");
    }

    @Override
    public void run() {
        System.out.println(id + " running");
    }
}
