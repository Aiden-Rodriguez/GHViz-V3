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
 * @version 1.3
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
            int fileCount = 0;

            for (String path : allFromUrl) {
                if (path.endsWith(".java")) {
                    String content = gh.getFileContentFromUrl(convertToBlobUrl(url, path));
                    int lines = countNonEmptyLines(content);
                    int complexity = countComplexity(content);
                    Square square = new Square(path, lines, complexity);

                    square.setAbstract(isAbstractClass(content));
                    square.setInterface(isInterface(content));

                    Set<String> dependencies = extractDependencies(content, path);
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
        // Remove single-line comments
        content = content.replaceAll("//.*", "");

        // Remove multi-line comments
        content = content.replaceAll("/\\*.*?\\*/", "");

        // Remove string literals
        content = content.replaceAll("\".*?\"", "");

        return content;
    }

    private boolean isAbstractClass(String content) {
        Pattern pattern = Pattern.compile("\\babstract\\s+class\\s+\\w+");
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    private boolean isInterface(String content) {
        Pattern pattern = Pattern.compile("\\binterface\\s+\\w+");
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    private Set<String> extractDependencies(String content, String currentPath) {
        Set<String> dependencies = new HashSet<>();

        // Extract class name from path
        String currentClassName = currentPath.substring(currentPath.lastIndexOf("/") + 1).replace(".java", "");

        // Extract import statements
        Pattern importPattern = Pattern.compile("import\\s+[\\w.]+\\.(\\w+);");
        Matcher importMatcher = importPattern.matcher(content);
        while (importMatcher.find()) {
            String importedClass = importMatcher.group(1);
            // Only add if it's not a standard Java library
            if (!isStandardJavaClass(importedClass)) {
                dependencies.add(importedClass);
            }
        }

        // Look for direct class usage (new ClassName(), ClassName.method(), etc.)
        String cleanedContent = removeCommentsAndStrings(content);

        // Pattern for: new ClassName(
        Pattern newPattern = Pattern.compile("\\bnew\\s+(\\w+)\\s*\\(");
        Matcher newMatcher = newPattern.matcher(cleanedContent);
        while (newMatcher.find()) {
            String className = newMatcher.group(1);
            if (!isStandardJavaClass(className) && !className.equals(currentClassName)) {
                dependencies.add(className);
            }
        }

        // Pattern for: ClassName.method() or ClassName.field
        Pattern staticPattern = Pattern.compile("\\b([A-Z]\\w+)\\.\\w+");
        Matcher staticMatcher = staticPattern.matcher(cleanedContent);
        while (staticMatcher.find()) {
            String className = staticMatcher.group(1);
            if (!isStandardJavaClass(className) && !className.equals(currentClassName)) {
                dependencies.add(className);
            }
        }

        return dependencies;
    }

    private boolean isStandardJavaClass(String className) {
        // Common Java standard library prefixes and classes
        return className.startsWith("java") ||
                className.startsWith("javax") ||
                className.equals("String") ||
                className.equals("Integer") ||
                className.equals("Double") ||
                className.equals("Boolean") ||
                className.equals("List") ||
                className.equals("Set") ||
                className.equals("Map") ||
                className.equals("ArrayList") ||
                className.equals("HashMap") ||
                className.equals("HashSet") ||
                className.equals("Vector") ||
                className.equals("Thread") ||
                className.equals("Exception") ||
                className.equals("Object") ||
                className.equals("System") ||
                className.equals("Math");
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