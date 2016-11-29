import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


public class RmiComponent extends UnicastRemoteObject implements RmiInterface, Runnable {

    public int id;
    public int n;

    protected RmiComponent(int id, String host, int port, int n) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.id = id;
        this.n = n;
        String urlName = "rmi://" + host + ":" + port + "/" + id;
        Naming.bind(urlName, this);
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
