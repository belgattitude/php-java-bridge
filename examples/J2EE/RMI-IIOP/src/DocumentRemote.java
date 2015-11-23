import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.math.*;

// This interface defines the "business" methods
public interface DocumentRemote extends EJBObject {
    public void addPage(Page page) throws RemoteException;
    public String analyze() throws RemoteException;
}
