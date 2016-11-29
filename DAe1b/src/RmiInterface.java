import java.rmi.*;
import java.util.List;

public interface RmiInterface extends Remote {
    void receiveMessage(String m, Event e) throws java.rmi.RemoteException;
    void broadCastMessage(String m, List<RmiInterface> receivers) throws java.rmi.RemoteException;
}
