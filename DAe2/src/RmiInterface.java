import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


public interface RmiInterface extends Remote {
    void method() throws RemoteException;
    void receiveMessage(Message msg);
    void receiveAck();
}
