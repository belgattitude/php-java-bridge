import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.util.*;

// Implementation of a stateful session bean.
public class DocumentBean implements SessionBean {

    Vector pages;
    public void addPage(Page page) {
	pages.add(page);
    }

    public String analyze() {
        return "This document has " + pages.size() + " pages.";
    }

    // required by ejb spec
    public DocumentBean() {}
    public void ejbCreate() { pages = new Vector(); }
    public void ejbRemove() { pages = null; }
    public void ejbActivate() { /* retrieve vector from database */ }
    public void ejbPassivate() { /* store vector to database */ } 
    public void setSessionContext(SessionContext sc) {}
} 
