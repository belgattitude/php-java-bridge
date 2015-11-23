import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

// This interface defines the methods to create a DocumentBean.
public interface DocumentHome extends EJBHome {
    DocumentRemote create() throws RemoteException, CreateException;
}
