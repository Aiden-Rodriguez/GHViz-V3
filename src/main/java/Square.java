import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a square with a file path, number of lines of code, and complexity.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.4
 */
public class Square {

    private String path;
    private int lines;
    private int complexity;
    private boolean isAbstract;
    private boolean isInterface;

    private String extendsClass;
    private Set<String> implementsInterfaces;

    // Composition and Aggregation
    private Set<String> compositionDependencies;
    private Set<String> aggregationDependencies;

    // General dependencies
    private Set<String> efferentDependencies;
    private Set<String> afferentDependencies;

    public Square(String path, int lines, int complexity) {
        this.path = path;
        this.lines = lines;
        this.complexity = complexity;
        this.isAbstract = false;
        this.isInterface = false;
        this.extendsClass = null;
        this.implementsInterfaces = new HashSet<>();
        this.compositionDependencies = new HashSet<>();
        this.aggregationDependencies = new HashSet<>();
        this.efferentDependencies = new HashSet<>();
        this.afferentDependencies = new HashSet<>();
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

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean isInterface) {
        this.isInterface = isInterface;
    }

    public String getExtendsClass() {
        return extendsClass;
    }

    public void setExtendsClass(String extendsClass) {
        this.extendsClass = extendsClass;
    }

    public Set<String> getImplementsInterfaces() {
        return implementsInterfaces;
    }

    public void addImplementsInterface(String interfaceName) {
        implementsInterfaces.add(interfaceName);
    }

    public Set<String> getCompositionDependencies() {
        return compositionDependencies;
    }

    public void addCompositionDependency(String className) {
        compositionDependencies.add(className);
    }

    public Set<String> getAggregationDependencies() {
        return aggregationDependencies;
    }

    public void addAggregationDependency(String className) {
        aggregationDependencies.add(className);
    }

    public Set<String> getEfferentDependencies() {
        return efferentDependencies;
    }

    public void addEfferentDependency(String className) {
        efferentDependencies.add(className);
    }

    public Set<String> getAfferentDependencies() {
        return afferentDependencies;
    }

    public void addAfferentDependency(String className) {
        afferentDependencies.add(className);
    }

    public int getEfferentCoupling() {
        return efferentDependencies.size();
    }

    public int getAfferentCoupling() {
        return afferentDependencies.size();
    }
}