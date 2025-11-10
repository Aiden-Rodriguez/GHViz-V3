import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FileTreePanel class to display repository structure as a tree.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.3
 */
public class FileTreePanel extends JPanel implements PropertyChangeListener {

    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private Map<String, DefaultMutableTreeNode> pathToNodeMap;

    public FileTreePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 0));

        root = new DefaultMutableTreeNode("Repository");
        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        pathToNodeMap = new HashMap<>();

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node != null) {
                    String folderPath = getNodePath(node);
                    Blackboard.getInstance().setSelectedFolderPath(folderPath);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);

        Blackboard.getInstance().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("blackboardReady")) {
            buildTree();
        } else if (evt.getPropertyName().equals("blackboardCleared")) {
            clearTree();
        }
    }

    private void buildTree() {
        root.removeAllChildren();
        pathToNodeMap.clear();
        List<Square> squares = Blackboard.getInstance().getSquares();

        Map<String, DefaultMutableTreeNode> folderNodes = new HashMap<>();
        folderNodes.put("", root);

        for (Square square : squares) {
            String path = square.getPath();
            String[] parts = path.split("/");

            String currentPath = "";
            DefaultMutableTreeNode parentNode = root;

            for (int i = 0; i < parts.length - 1; i++) {
                currentPath = currentPath.isEmpty() ? parts[i] : currentPath + "/" + parts[i];

                if (!folderNodes.containsKey(currentPath)) {
                    DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(parts[i]);
                    parentNode.add(folderNode);
                    folderNodes.put(currentPath, folderNode);
                    pathToNodeMap.put(currentPath, folderNode);
                    parentNode = folderNode;
                } else {
                    parentNode = folderNodes.get(currentPath);
                }
            }

            String fileName = parts[parts.length - 1];
            DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileName);
            parentNode.add(fileNode);
        }

        treeModel.reload();
        expandAllNodes();

        tree.clearSelection();
    }

    private void clearTree() {
        root.removeAllChildren();
        root.setUserObject("Repository");
        pathToNodeMap.clear();
        treeModel.reload();
        Blackboard.getInstance().setSelectedFolderPath("");
    }

    private void expandAllNodes() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private String getNodePath(DefaultMutableTreeNode node) {
        if (node == root) {
            return "";
        }

        StringBuilder path = new StringBuilder();
        Object[] nodes = node.getUserObjectPath();

        for (int i = 1; i < nodes.length; i++) {
            if (path.length() > 0) {
                path.append("/");
            }
            path.append(nodes[i].toString());
        }

        return path.toString();
    }
}