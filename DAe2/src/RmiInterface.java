import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


public interface RmiInterface extends Remote {
    void receiveMessage(Message msg) throws RemoteException;
    void receiveAck(RmiInterface sender) throws RemoteException;
    int getId() throws RemoteException;
}
