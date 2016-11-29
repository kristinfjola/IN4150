import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // 145.94.164.234

    public static void main(String[] args) {
        createProcess();
    }

    public static void createProcess(){
        try {
            LocateRegistry.createRegistry(1090);
            LocateRegistry.createRegistry(1091);
            LocateRegistry.createRegistry(1092);
            LocateRegistry.createRegistry(1093);
            LocateRegistry.createRegistry(1094);
            LocateRegistry.createRegistry(1095);
            LocateRegistry.createRegistry(1096);
            LocateRegistry.createRegistry(1097);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        int total = 4;
        try {
            RmiComponent p0 = new RmiComponent(0, "145.94.164.234", 1090, total);
            RmiComponent p1 = new RmiComponent(1, "145.94.164.234", 1091, total);
            RmiComponent p2 = new RmiComponent(2, "145.94.164.234", 1092, total);
            RmiComponent p3 = new RmiComponent(3, "145.94.164.234", 1093, total);
            RmiComponent p4 = new RmiComponent(4, "145.94.164.234", 1094, total);
            RmiComponent p5 = new RmiComponent(5, "145.94.164.234", 1095, total);
            RmiComponent p6 = new RmiComponent(6, "145.94.164.234", 1096, total);
            RmiComponent p7 = new RmiComponent(7, "145.94.164.234", 1097, total);

            p0.startCandidate();
            //p3.startCandidate();
            //p7.startCandidate();


            try {
                Thread.sleep(1000*5);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            //RmiInterface p2 = (RmiInterface) Naming.lookup("rmi://145.94.148.40:1092/2");
            //p2.method();

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
