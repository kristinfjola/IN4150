import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class that creates the instances for the processes. Half of them are created on
 * each host (two computers). Therefore the code is a bit different in the main file of the other host
 * since the first creates processes with ids 0 to (n/2)-1, and the other with ids n/2 to n
 */

public class Main {

    public static void main(String[] args) {
        int total = 40;
        int local = total / 2;
        int port = 1090;
        String kristinIP = "145.94.164.222";
        String rickyIP = "145.94.228.126";
        System.setProperty("java.rmi.server.hostname", kristinIP);
        List<RmiInterface> processes = new ArrayList<RmiInterface>();
        int numFaulty = (int) (Math.random() * local/5);
        int numZeros = 0;
        int numOnes = 0;
        //numFaulty = 6;    // manually control for too many faulty processes

        System.out.println("Starting to create " + local + " processes with " + numFaulty + " faulty");

        // create registries
        for(int i = 0; i < local; i++) {
            int currentPort = port+i;
            try {
                LocateRegistry.createRegistry(currentPort);
                int value = Math.random() < 0.5 ? 0 : 1;
                if (value == 0) numZeros++;
                if (value == 1) numOnes++;
                boolean isFaulty = i < numFaulty ? true : false;
                RmiComponent p = new RmiComponent(i, kristinIP, currentPort, total, isFaulty, value, numFaulty);
                processes.add(p);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (AlreadyBoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Processes with value 0: " + numZeros);
        System.out.println("Processes with value 1: " + numOnes);

        try {
            Thread.sleep(1000*2);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        // collect all together and tell them about their neighbours
        for(int i = 0; i < total; i++) {    // LOCAL for one computer, TOTAL for distributed
            String ip = i < local ? kristinIP : rickyIP;
            int currentPort = port+i;
            try {
                RmiInterface p = (RmiInterface) Naming.lookup("rmi://" + ip + ":" + currentPort + "/" + i);
                p.addNeighbours(processes);
                if (ip == rickyIP) p.increaseNumberOfFaulty(numFaulty); // set the correct number of faulty processes
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
