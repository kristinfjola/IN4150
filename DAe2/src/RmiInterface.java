import java.rmi.*;

public interface RmiInterface extends Remote {
    void receiveMessage(Message msg) throws RemoteException;
    void receiveAck(RmiInterface sender) throws RemoteException;
    int getId() throws RemoteException;
}
