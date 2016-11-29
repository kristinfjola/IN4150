import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        createProcess();
    }

    public static void createProcess(){
        try {
            LocateRegistry.createRegistry(1091);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            RmiComponent p1 = new RmiComponent(1, "localhost", 1091, 2);
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
