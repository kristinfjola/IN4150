import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

/*
    Class that creates a node P1 and broadcasts the message "hello2" to other nodes
 */

public class P1 {
    int id = 1;
    int port = 1099;

    public static void main(String[] args) {
        P1 p = new P1();
        p.createProcess();
    }

    public void createProcess(){
        try {
            LocateRegistry.createRegistry(this.port);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            RmiObject p1 = new RmiObject(this.id, this.port, 3);
            new Thread(p1).start();

            try {
                Thread.sleep(1000*5);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            RmiInterface p0 = (RmiInterface) Naming.lookup("rmi://localhost:1098/P0");
            RmiInterface p2 = (RmiInterface) Naming.lookup("rmi://localhost:1097/P2");
            List<RmiInterface> receivers = new ArrayList<RmiInterface>();
            receivers.add(p2);
            receivers.add(p0);
            p1.broadCastMessage("hello1", receivers);

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
