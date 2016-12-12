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
        int total = 100;
        int local = total / 2;
        int port = 1090;
        String myIP = "145.94.164.89";
        String rickyIP = "145.94.230.65";
        System.setProperty("java.rmi.server.hostname", myIP);
        List<RmiInterface> processes = new ArrayList<RmiInterface>();
        int numFaulty = (int) (Math.random() * local/5);
        numFaulty = 12;

        System.out.println("Starting to create " + total + " processes with " + numFaulty + " faulty");

        // create registries
        for(int i = 0; i < local; i++) {
            int currentPort = port+i;
            try {
                LocateRegistry.createRegistry(currentPort);
                int value = Math.random() < 0.5 ? 0 : 1;
                boolean isFaulty = i < numFaulty ? true : false;
                RmiComponent p = new RmiComponent(i, myIP, currentPort, local, isFaulty, value, numFaulty);
                processes.add(p);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (AlreadyBoundException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000*2);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        // collect all together
        for(int i = 0; i < local; i++) {    // LOCAL for one computer, TOTAL for distributed
            String ip = i < local ? myIP : rickyIP;
            int currentPort = port+i;
            try {
                RmiInterface p = (RmiInterface) Naming.lookup("rmi://" + ip + ":" + currentPort + "/" + i);
                p.addNeighbours(processes);
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000*2);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        // start processes
        for(RmiInterface p : processes) {
            new Thread((RmiComponent) p).start();
        }

    }
}
