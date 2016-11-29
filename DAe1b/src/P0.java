import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

/*
    Class that creates a node P0 and broadcasts the message "hello0" to other nodes
 */

public class P0 {
    int id = 0;
    int port = 1098;

    public static void main(String[] args) {
        P0 p = new P0();
        p.createProcess();
    }

    public void createProcess(){
        try {
            LocateRegistry.createRegistry(this.port);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            RmiObject p0 = new RmiObject(this.id, this.port, 3);
            new Thread(p0).start();

            try {
                Thread.sleep(1000*6);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            RmiInterface p1 = (RmiInterface) Naming.lookup("rmi://localhost:1099/P1");
            RmiInterface p2 = (RmiInterface) Naming.lookup("rmi://localhost:1097/P2");
            List<RmiInterface> receivers = new ArrayList<RmiInterface>();
            receivers.add(p1);
            receivers.add(p2);
            p0.broadCastMessage("hello0", receivers);


        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
