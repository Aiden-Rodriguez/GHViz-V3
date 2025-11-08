/**
 * This class represents a square with a file path, number of lines of code, and complexity.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.2
 */
public class Square {

    private String path;
    private int lines;
    private int complexity;

    public Square(String path, int lines, int complexity) {
        this.path = path;
        this.lines = lines;
        this.complexity = complexity;
    }

    public int getLinesOfCode() {
        return lines;
    }

    public int getComplexity() {
        return complexity;
    }

    public String getName() {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public String getPath() {
        return path;
    }
}