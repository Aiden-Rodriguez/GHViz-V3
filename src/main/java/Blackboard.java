import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Blackboard class to manage squares and notify listeners about state changes.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.3
 */
public class Blackboard extends PropertyChangeSupport {

    private static Blackboard instance;
    private Vector<Square> squares;
    private boolean ready = false;
    private boolean loading = false;
    private String statusMessage = "";
    private String selectedFileName = "";
    private String selectedFolderPath = "";

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

    public void setSelectedFolderPath(String folderPath) {
        String oldPath = this.selectedFolderPath;
        this.selectedFolderPath = folderPath;
        firePropertyChange("selectedFolderPath", oldPath, folderPath);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getSelectedFileName() {
        return selectedFileName;
    }

    public String getSelectedFolderPath() {
        return selectedFolderPath;
    }

    public List<Square> getSquares() {
        return squares;
    }

    //Get filtered squares based on the currently selected folder path.
    public List<Square> getFilteredSquares() {
        if (selectedFolderPath == null || selectedFolderPath.isEmpty()) {
            return new Vector<>();
        }

        return squares.stream()
                .filter(square -> {
                    String squarePath = square.getPath();
                    // Check if the square's path starts with the selected folder path
                    // and is directly within that folder (not in a subfolder)
                    if (squarePath.startsWith(selectedFolderPath)) {
                        String remaining = squarePath.substring(selectedFolderPath.length());
                        if (remaining.startsWith("/")) {
                            remaining = remaining.substring(1);
                        }
                        // Only include if there are no more slashes (file is directly in this folder)
                        return !remaining.contains("/");
                    }
                    return false;
                })
                .collect(Collectors.toList());
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
        selectedFolderPath = "";
        firePropertyChange("blackboardCleared", false, true);
    }
}