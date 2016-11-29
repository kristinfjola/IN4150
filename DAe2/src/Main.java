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
            LocateRegistry.createRegistry(1092);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            RmiComponent p2 = new RmiComponent(2, "145.94.148.40", 1092, 2);
            new Thread(p2).start();




            try {
                Thread.sleep(1000*5);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            RmiInterface p1 = (RmiInterface) Naming.lookup("rmi://145.94.164.234:1091/1");
            p1.method();

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
