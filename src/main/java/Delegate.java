import javiergs.tulip.GitHubHandler;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;
import java.util.HashSet;

/**
 * Delegate class to load Java files from a GitHub repository URL.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.4
 */
public class Delegate implements Runnable {

    private String url;

    public Delegate(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        Blackboard.getInstance().clear();
        Blackboard.getInstance().setStatusMessage("Fetching...");
        try {
            Dotenv dotenv = Dotenv.load();
            String token = dotenv.get("GITHUB_TOKEN");

            if (token == null || token.isEmpty()) {
                throw new IllegalStateException("GitHub token not found in .env file");
            }

            GitHubHandler gh = new GitHubHandler(token);
            List<String> allFromUrl = gh.listFilesRecursive(url);

            // First pass: collect all class names from all Java files
            Set<String> allProjectClasses = new HashSet<>();
            for (String path : allFromUrl) {
                if (path.endsWith(".java")) {
                    String className = path.substring(path.lastIndexOf("/") + 1).replace(".java", "");
                    allProjectClasses.add(className);
                }
            }

            int fileCount = 0;

            // Second pass: analyze each file
            for (String path : allFromUrl) {
                if (path.endsWith(".java")) {
                    String content = gh.getFileContentFromUrl(convertToBlobUrl(url, path));
                    int lines = countNonEmptyLines(content);
                    int complexity = countComplexity(content);
                    Square square = new Square(path, lines, complexity);

                    square.setAbstract(isAbstractClass(content));
                    square.setInterface(isInterface(content));

                    // Extract extends and implements relationships
                    String extendsClass = extractExtendsClass(content);
                    if (extendsClass != null && allProjectClasses.contains(extendsClass)) {
                        square.setExtendsClass(extendsClass);
                    }

                    Set<String> implementsInterfaces = extractImplementsInterfaces(content);
                    for (String iface : implementsInterfaces) {
                        if (allProjectClasses.contains(iface)) {
                            square.addImplementsInterface(iface);
                        }
                    }

                    String currentClassName = path.substring(path.lastIndexOf("/") + 1).replace(".java", "");

                    // Extract self-references (Singleton pattern) - should be aggregation
                    Set<String> selfReferences = extractSelfReferences(content, currentClassName);
                    for (String selfRef : selfReferences) {
                        square.addAggregationDependency(selfRef);
                    }

                    // Extract aggregation relationships (collections)
                    Set<String> aggregationTypes = extractAggregationTypes(content, allProjectClasses);
                    for (String aggrType : aggregationTypes) {
                        square.addAggregationDependency(aggrType);
                    }

                    // Extract composition relationships (direct field references)
                    Set<String> fieldTypes = extractFieldTypes(content, allProjectClasses);
                    for (String fieldType : fieldTypes) {
                        if (!aggregationTypes.contains(fieldType) && !selfReferences.contains(fieldType)) {
                            square.addCompositionDependency(fieldType);
                        }
                    }

                    // Extract general dependencies (method parameters, local variables, etc.)
                    Set<String> dependencies = extractDependencies(content, path, allProjectClasses);
                    for (String dep : dependencies) {
                        square.addEfferentDependency(dep);
                    }

                    Blackboard.getInstance().addSquare(square);
                    fileCount++;
                }
            }

            Blackboard.getInstance().setStatusMessage(fileCount + " files analyzed");
            Blackboard.getInstance().setReady();
            Thread.sleep(1000);
        } catch (Exception e) {
            Blackboard.getInstance().setStatusMessage("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int countNonEmptyLines(String content) {
        return (int) content.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .count();
    }

    private int countComplexity(String content) {
        int complexity = 0;

        String cleaned = removeCommentsAndStrings(content);

        Pattern ifPattern = Pattern.compile("\\bif\\s*\\(");
        Matcher ifMatcher = ifPattern.matcher(cleaned);
        while (ifMatcher.find()) complexity++;

        Pattern switchPattern = Pattern.compile("\\bswitch\\s*\\(");
        Matcher switchMatcher = switchPattern.matcher(cleaned);
        while (switchMatcher.find()) complexity++;

        Pattern forPattern = Pattern.compile("\\bfor\\s*\\(");
        Matcher forMatcher = forPattern.matcher(cleaned);
        while (forMatcher.find()) complexity++;

        Pattern whilePattern = Pattern.compile("\\bwhile\\s*\\(");
        Matcher whileMatcher = whilePattern.matcher(cleaned);
        while (whileMatcher.find()) complexity++;

        return complexity;
    }

    private String removeCommentsAndStrings(String content) {
        content = content.replaceAll("//.*", "");
        content = content.replaceAll("/\\*.*?\\*/", "");
        content = content.replaceAll("\".*?\"", "");

        return content;
    }

    private boolean isAbstractClass(String content) {
        String cleaned = removeCommentsAndStrings(content);
        Pattern pattern = Pattern.compile("\\b(public|private|protected)?\\s*abstract\\s+class\\s+\\w+");
        Matcher matcher = pattern.matcher(cleaned);
        return matcher.find();
    }

    private boolean isInterface(String content) {
        String cleaned = removeCommentsAndStrings(content);
        Pattern pattern = Pattern.compile("\\b(public|private|protected)?\\s*interface\\s+\\w+");
        Matcher matcher = pattern.matcher(cleaned);

        if (matcher.find()) {
            int start = matcher.start();
            String before = cleaned.substring(Math.max(0, start - 20), start);
            if (before.contains("implements")) {
                return false;
            }
            return true;
        }
        return false;
    }

    private String extractExtendsClass(String content) {
        String cleaned = removeCommentsAndStrings(content);
        Pattern pattern = Pattern.compile("\\bextends\\s+([A-Z]\\w+)");
        Matcher matcher = pattern.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Set<String> extractImplementsInterfaces(String content) {
        Set<String> interfaces = new HashSet<>();
        String cleaned = removeCommentsAndStrings(content);
        Pattern pattern = Pattern.compile("\\bimplements\\s+([A-Z][\\w,\\s]+)");
        Matcher matcher = pattern.matcher(cleaned);

        while (matcher.find()) {
            String interfaceList = matcher.group(1);
            // Split by comma and clean up
            for (String iface : interfaceList.split(",")) {
                String trimmed = iface.trim();
                // Remove any generic type parameters
                if (trimmed.contains("<")) {
                    trimmed = trimmed.substring(0, trimmed.indexOf("<"));
                }
                if (trimmed.matches("[A-Z]\\w+")) {
                    interfaces.add(trimmed);
                }
            }
        }
        return interfaces;
    }

    private Set<String> extractFieldTypes(String content, Set<String> allProjectClasses) {
        Set<String> fieldTypes = new HashSet<>();
        String cleaned = removeCommentsAndStrings(content);

        // Collection type names to exclude
        Set<String> collectionTypes = Set.of("List", "Vector", "Set", "ArrayList",
                "HashSet", "Collection", "Map", "HashMap",
                "LinkedList", "TreeSet", "TreeMap");

        // Look for field declarations: "private/public/protected Type fieldName"
        Pattern fieldPattern = Pattern.compile("\\b(private|public|protected)\\s+(?:static\\s+)?(?:final\\s+)?([A-Z]\\w+)(?:<[^>]+>)?\\s+\\w+\\s*[;=]");
        Matcher fieldMatcher = fieldPattern.matcher(cleaned);

        while (fieldMatcher.find()) {
            String type = fieldMatcher.group(2);
            // Only add if it's a project class AND not a collection type
            // Self-references are handled separately
            if (allProjectClasses.contains(type) && !collectionTypes.contains(type)) {
                fieldTypes.add(type);
            }
        }

        return fieldTypes;
    }

    private Set<String> extractSelfReferences(String content, String currentClassName) {
        Set<String> selfRefs = new HashSet<>();
        String cleaned = removeCommentsAndStrings(content);

        // Look for static fields of the same type (Singleton pattern)
        Pattern selfRefPattern = Pattern.compile("\\b(private|public|protected)\\s+static\\s+(?:final\\s+)?" +
                currentClassName + "\\s+\\w+\\s*[;=]");
        Matcher matcher = selfRefPattern.matcher(cleaned);

        if (matcher.find()) {
            selfRefs.add(currentClassName);
        }

        return selfRefs;
    }

    private Set<String> extractAggregationTypes(String content, Set<String> allProjectClasses) {
        Set<String> aggregationTypes = new HashSet<>();
        String cleaned = removeCommentsAndStrings(content);

        // Look for collection types with generics: List<Type>, Vector<Type>, Set<Type>, etc.
        Pattern collectionPattern = Pattern.compile("\\b(private|public|protected)\\s+(?:static\\s+)?(?:final\\s+)?(List|Vector|Set|ArrayList|HashSet|Collection|Map|HashMap|LinkedList|TreeSet|TreeMap)<\\s*([A-Z]\\w+)\\s*>");
        Matcher collectionMatcher = collectionPattern.matcher(cleaned);

        while (collectionMatcher.find()) {
            String type = collectionMatcher.group(3);
            if (allProjectClasses.contains(type)) {
                aggregationTypes.add(type);
            }
        }

        return aggregationTypes;
    }

    private Set<String> extractDependencies(String content, String currentPath, Set<String> allProjectClasses) {
        Set<String> dependencies = new HashSet<>();

        String currentClassName = currentPath.substring(currentPath.lastIndexOf("/") + 1).replace(".java", "");

        String cleanedContent = removeCommentsAndStrings(content);

        Set<String> potentialClasses = new HashSet<>();

        Pattern newPattern = Pattern.compile("\\bnew\\s+([A-Z]\\w+)\\s*[<(]");
        Matcher newMatcher = newPattern.matcher(cleanedContent);
        while (newMatcher.find()) {
            potentialClasses.add(newMatcher.group(1));
        }

        Pattern staticPattern = Pattern.compile("\\b([A-Z]\\w+)\\.\\w+\\s*\\(");
        Matcher staticMatcher = staticPattern.matcher(cleanedContent);
        while (staticMatcher.find()) {
            potentialClasses.add(staticMatcher.group(1));
        }

        // Pattern for: ClassName varName (variable declarations)
        Pattern declPattern = Pattern.compile("\\b([A-Z]\\w+)\\s+[a-z]\\w*\\s*[;=),]");
        Matcher declMatcher = declPattern.matcher(cleanedContent);
        while (declMatcher.find()) {
            potentialClasses.add(declMatcher.group(1));
        }

        // Pattern for: method parameters - Type paramName
        Pattern paramPattern = Pattern.compile("\\(([^)]*?)\\)");
        Matcher paramMatcher = paramPattern.matcher(cleanedContent);
        while (paramMatcher.find()) {
            String params = paramMatcher.group(1);
            Pattern typePattern = Pattern.compile("\\b([A-Z]\\w+)\\s+\\w+");
            Matcher typeMatcher = typePattern.matcher(params);
            while (typeMatcher.find()) {
                potentialClasses.add(typeMatcher.group(1));
            }
        }

        // Pattern for: return types - "ClassName methodName("
        Pattern returnPattern = Pattern.compile("\\b([A-Z]\\w+)\\s+\\w+\\s*\\(");
        Matcher returnMatcher = returnPattern.matcher(cleanedContent);
        while (returnMatcher.find()) {
            potentialClasses.add(returnMatcher.group(1));
        }

        for (String className : potentialClasses) {
            if (allProjectClasses.contains(className) && !className.equals(currentClassName)) {
                dependencies.add(className);
            }
        }

        return dependencies;
    }

    private String convertToBlobUrl(String url, String path) {
        if (url.contains("/tree/")) {
            String[] parts = url.split("/tree/");
            return parts[0] + "/blob/" + parts[1].split("/")[0] + "/" + path;
        } else {
            return url.replace("/tree/", "/blob/") + "/" + path;
        }
    }
}