import javiergs.tulip.GitHubHandler;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Delegate class to load Java files from a GitHub repository URL.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5186
 * @version 1.2
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

    private String convertToBlobUrl(String url, String path) {
        if (url.contains("/tree/")) {
            String[] parts = url.split("/tree/");
            return parts[0] + "/blob/" + parts[1].split("/")[0] + "/" + path;
        } else {
            return url.replace("/tree/", "/blob/") + "/" + path;
        }
    }

}