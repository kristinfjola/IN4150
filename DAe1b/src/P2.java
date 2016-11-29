import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/*
    Class that creates a node P2
 */

public class P2 {
    int id = 2;
    int port = 1097;

    public static void main(String[] args) {
        P2 p = new P2();
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

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
