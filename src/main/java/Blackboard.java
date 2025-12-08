import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Blackboard class to manage squares and notify listeners about state changes.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.5
 */
public class Blackboard extends PropertyChangeSupport {

    private static final Logger logger = LoggerFactory.getLogger(Blackboard.class);
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
        logger.info("Blackboard initialized");
    }

    public static Blackboard getInstance() {
        if (instance == null) {
            instance = new Blackboard();
        }
        return instance;
    }

    public void loadFromUrl(String url) {
        try {
            logger.info("Starting load from URL: {}", url);
            setStatusMessage("Initiating load from URL...");
            Delegate delegate = new Delegate(url);
            Thread t = new Thread(delegate);
            t.start();
        } catch (Exception e) {
            logger.error("Failed to load from URL: {}", url, e);
            setStatusMessage("Error: Failed to start loading");
            e.printStackTrace();
        }
    }

    public void addSquare(Square square) {
        squares.add(square);
        logger.debug("Added square: {}", square.getName());
    }

    public void setReady() {
        calculateAfferentDependencies();
        ready = true;
        loading = false;
        logger.info("Blackboard ready with {} squares", squares.size());
        firePropertyChange("blackboardReady", false, true);
    }

    private void calculateAfferentDependencies() {
        logger.debug("Calculating afferent dependencies");
        Map<String, Square> classMap = new HashMap<>();
        for (Square square : squares) {
            String className = square.getName().replace(".java", "");
            classMap.put(className, square);
        }

        int dependencyCount = 0;
        for (Square square : squares) {
            String className = square.getName().replace(".java", "");
            for (String dependency : square.getEfferentDependencies()) {
                Square dependentSquare = classMap.get(dependency);
                if (dependentSquare != null) {
                    dependentSquare.addAfferentDependency(className);
                    dependencyCount++;
                }
            }
        }
        logger.debug("Calculated {} afferent dependencies", dependencyCount);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        ready = false;
        logger.info("Loading state changed to: {}", loading);
        if (loading) {
            setStatusMessage("Loading...");
        }
        firePropertyChange("blackboardLoading", !loading, loading);
    }

    public void setStatusMessage(String message) {
        String oldMessage = this.statusMessage;
        this.statusMessage = message;
        logger.info("Status: {}", message);
        firePropertyChange("statusMessage", oldMessage, message);
    }

    public void setSelectedFileName(String fileName) {
        String oldName = this.selectedFileName;
        this.selectedFileName = fileName;
        logger.debug("Selected file changed to: {}", fileName);
        if (!fileName.isEmpty()) {
            setStatusMessage("Selected: " + fileName);
        }
        firePropertyChange("selectedFileName", oldName, fileName);
    }

    public void setSelectedFolderPath(String folderPath) {
        String oldPath = this.selectedFolderPath;
        this.selectedFolderPath = folderPath;
        logger.debug("Selected folder changed to: {}", folderPath);

        List<Square> filteredSquares = getFilteredSquares();
        if (!folderPath.isEmpty()) {
            setStatusMessage("Folder: " + folderPath + " (" + filteredSquares.size() + " files)");
        }

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

    public List<Square> getFilteredSquares() {
        if (selectedFolderPath == null || selectedFolderPath.isEmpty()) {
            return new Vector<>();
        }

        List<Square> filtered = squares.stream()
                .filter(square -> {
                    String squarePath = square.getPath();
                    if (squarePath.startsWith(selectedFolderPath)) {
                        String remaining = squarePath.substring(selectedFolderPath.length());
                        if (remaining.startsWith("/")) {
                            remaining = remaining.substring(1);
                        }
                        return !remaining.contains("/");
                    }
                    return false;
                })
                .collect(Collectors.toList());

        logger.debug("Filtered {} squares for path: {}", filtered.size(), selectedFolderPath);
        return filtered;
    }

    public String getFolderFromPath(String filePath) {
        int lastSlash = filePath.lastIndexOf("/");
        if (lastSlash > 0) {
            return filePath.substring(0, lastSlash);
        }
        return "";
    }

    public List<Square> getSquaresInSameFolder(String filePath) {
        String folderPath = getFolderFromPath(filePath);

        return squares.stream()
                .filter(square -> {
                    String squarePath = square.getPath();
                    String squareFolder = getFolderFromPath(squarePath);
                    return squareFolder.equals(folderPath);
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
        logger.info("Clearing blackboard - {} squares removed", squares.size());
        squares.clear();
        ready = false;
        loading = false;
        statusMessage = "";
        selectedFileName = "";
        selectedFolderPath = "";
        setStatusMessage("Cleared");
        firePropertyChange("blackboardCleared", false, true);
    }
}