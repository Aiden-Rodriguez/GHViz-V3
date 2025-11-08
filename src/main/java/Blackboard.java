import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Vector;

/**
 * Blackboard class to manage squares and notify listeners about state changes.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.2
 */
public class Blackboard extends PropertyChangeSupport {

    private static Blackboard instance;
    private Vector<Square> squares;
    private boolean ready = false;
    private boolean loading = false;
    private String statusMessage = "";
    private String selectedFileName = "";

    private Blackboard() {
        super(new Object());
        squares = new Vector<>();
    }

    public static Blackboard getInstance() {
        if (instance == null) {
            instance = new Blackboard();
        }
        return instance;
    }

    public void loadFromUrl(String url) {
        try {
            Delegate delegate = new Delegate(url);
            Thread t = new Thread(delegate);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addSquare(Square square) {
        squares.add(square);
    }

    public void setReady() {
        ready = true;
        loading = false;
        firePropertyChange("blackboardReady", false, true);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        ready = false;
        firePropertyChange("blackboardLoading", !loading, loading);
    }

    public void setStatusMessage(String message) {
        String oldMessage = this.statusMessage;
        this.statusMessage = message;
        firePropertyChange("statusMessage", oldMessage, message);
    }

    public void setSelectedFileName(String fileName) {
        String oldName = this.selectedFileName;
        this.selectedFileName = fileName;
        firePropertyChange("selectedFileName", oldName, fileName);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getSelectedFileName() {
        return selectedFileName;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isLoading() {
        return loading;
    }

    public void clear() {
        squares.clear();
        ready = false;
        loading = false;
        statusMessage = "";
        selectedFileName = "";
        firePropertyChange("blackboardCleared", false, true);
    }

}