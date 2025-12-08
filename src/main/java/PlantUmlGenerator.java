import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Turns a list of Square objects into a PlantUML string.
 *
 * @author Aiden Rodriguez - GH Aiden-Rodriguez
 * @author Brandon Powell - GH Bpowell5184
 * @version 1.4
 */
public class PlantUmlGenerator {

    public static String generateDiagram(List<Square> squares) {
        StringBuilder puml = new StringBuilder();
        puml.append("@startuml\n");
        puml.append("skinparam backgroundColor #FEFEFE\n");
        puml.append("skinparam classAttributeIconSize 0\n");
        puml.append("left to right direction\n\n");

        Set<String> classNames = new HashSet<>();
        Map<String, Square> classMap = new HashMap<>();
        for (Square square : squares) {
            String className = square.getName().replace(".java", "");
            classNames.add(className);
            classMap.put(className, square);
        }

        for (Square square : squares) {
            String className = square.getName().replace(".java", "");

            if (square.isInterface()) {
                puml.append("interface ").append(className).append(" {\n");
            } else if (square.isAbstract()) {
                puml.append("abstract class ").append(className).append(" {\n");
            } else {
                puml.append("class ").append(className).append(" {\n");
            }

            puml.append("  .. Metrics ..\n");
            puml.append("  Lines: ").append(square.getLinesOfCode()).append("\n");
            puml.append("  Complexity: ").append(square.getComplexity()).append("\n");

            puml.append("}\n\n");
        }

        Set<String> addedRelationships = new HashSet<>();

        for (Square square : squares) {
            String className = square.getName().replace(".java", "");

            String extendsClass = square.getExtendsClass();
            if (extendsClass != null && classNames.contains(extendsClass)) {
                String key = className + "--|>" + extendsClass;
                if (!addedRelationships.contains(key)) {
                    puml.append(className).append(" --|> ").append(extendsClass).append("\n");
                    addedRelationships.add(key);
                }
            }

            for (String interfaceName : square.getImplementsInterfaces()) {
                if (classNames.contains(interfaceName)) {
                    String key = className + "..|>" + interfaceName;
                    if (!addedRelationships.contains(key)) {
                        puml.append(className).append(" ..|> ").append(interfaceName).append("\n");
                        addedRelationships.add(key);
                    }
                }
            }

            for (String compositionType : square.getCompositionDependencies()) {
                if (classNames.contains(compositionType)) {
                    String key = className + "*--" + compositionType;
                    String reverseKey = compositionType + "*--" + className;
                    if (!addedRelationships.contains(key) &&
                            !addedRelationships.contains(reverseKey) &&
                            !compositionType.equals(extendsClass) &&
                            !square.getImplementsInterfaces().contains(compositionType)) {
                        puml.append(className).append(" *-- ").append(compositionType).append("\n");
                        addedRelationships.add(key);
                    }
                }
            }

            for (String aggregationType : square.getAggregationDependencies()) {
                if (classNames.contains(aggregationType)) {
                    String key = className + "o--" + aggregationType;
                    String reverseKey = aggregationType + "o--" + className;
                    if (!addedRelationships.contains(key) &&
                            !addedRelationships.contains(reverseKey) &&
                            !aggregationType.equals(extendsClass) &&
                            !square.getImplementsInterfaces().contains(aggregationType)) {
                        puml.append(className).append(" o-- ").append(aggregationType).append("\n");
                        addedRelationships.add(key);
                    }
                }
            }

            Set<String> alreadyShown = new HashSet<>();
            if (extendsClass != null) alreadyShown.add(extendsClass);
            alreadyShown.addAll(square.getImplementsInterfaces());
            alreadyShown.addAll(square.getCompositionDependencies());
            alreadyShown.addAll(square.getAggregationDependencies());

            for (String dependency : square.getEfferentDependencies()) {
                if (classNames.contains(dependency) && !alreadyShown.contains(dependency)) {
                    String key = className + "-->" + dependency;
                    if (!addedRelationships.contains(key)) {
                        puml.append(className).append(" --> ").append(dependency).append("\n");
                        addedRelationships.add(key);
                    }
                }
            }
        }

        puml.append("\n@enduml\n");
        return puml.toString();
    }
}