import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Main {

    public static void main(String[] args) {
        createProcess();
    }

    public static void createProcess(){
        try {
            LocateRegistry.createRegistry(1090);
            LocateRegistry.createRegistry(1091);
            LocateRegistry.createRegistry(1092);
            LocateRegistry.createRegistry(1093);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // we start 4 components on each host (2 hosts)
        int total = 8;
        String myIP = "145.94.166.120";
        System.setProperty("java.rmi.server.hostname", myIP);
        try {
            RmiComponent p0 = new RmiComponent(0, myIP, 1090, total);
            RmiComponent p1 = new RmiComponent(1, myIP, 1091, total);
            RmiComponent p2 = new RmiComponent(2, myIP, 1092, total);
            RmiComponent p3 = new RmiComponent(3, myIP, 1093, total);

            // delay to allow time on both sides for components to be created
            try {
                Thread.sleep(1000*3);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }



            // start the candidates
            p0.startCandidate();
            //p2.startCandidate();

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
