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

        // Check if class declaration contains "abstract" keyword
        Pattern pattern = Pattern.compile("\\b(public|private|protected)?\\s*abstract\\s+class\\s+\\w+");
        Matcher matcher = pattern.matcher(cleaned);
        return matcher.find();
    }

    private boolean isInterface(String content) {
        // Remove comments and strings to avoid false matches
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

        Pattern extendsPattern = Pattern.compile("\\bextends\\s+([A-Z]\\w+)");
        Matcher extendsMatcher = extendsPattern.matcher(cleanedContent);
        while (extendsMatcher.find()) {
            potentialClasses.add(extendsMatcher.group(1));
        }

        Pattern implementsPattern = Pattern.compile("\\bimplements\\s+([A-Z][\\w,\\s]+)");
        Matcher implementsMatcher = implementsPattern.matcher(cleanedContent);
        while (implementsMatcher.find()) {
            String interfaces = implementsMatcher.group(1);
            for (String iface : interfaces.split(",")) {
                String trimmed = iface.trim();
                if (trimmed.matches("[A-Z]\\w+")) {
                    potentialClasses.add(trimmed);
                }
            }
        }

        // Pattern for: ClassName varName (field/variable declarations)
        // Matches: "ClassName varName;" or "ClassName varName =" or "ClassName varName)"
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