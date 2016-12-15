import java.rmi.*;
import java.util.List;


public interface RmiInterface extends Remote {
    void addNeighbours(List<RmiInterface> processes) throws RemoteException;
    void broadcast(Message msg) throws RemoteException;
    void receive(Message msg) throws RemoteException;
    void increaseNumberOfFaulty(int f) throws RemoteException;
}
