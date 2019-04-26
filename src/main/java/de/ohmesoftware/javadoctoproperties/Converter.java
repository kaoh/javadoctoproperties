package de.ohmesoftware.javadoctoproperties;

import com.sun.javadoc.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Converts the passes source path and sub directories and stores the Javadocs as properties.
 *
 * @author Karsten Ohme
 */
public class Converter {

    private static final String PREFIX_OPTION = "prefix";
    private static final String EQUALS = "=";
    private static final String PROPERTY_SEPARATOR = ".";
    private static final String EMPTY = "";
    private static final String SPACE = " ";

    /**
     * Doclet start method.
     * @param root The documentation root.
     * @return <code>true</code> if the execution was successful.
     */
    public static boolean start(RootDoc root) {
        String propertiesPrefix = EMPTY;
        String[][] options = root.options();
        if (options != null && options.length > 0) {
            String[] prefixOption = Arrays.stream(options).filter(s -> s.length > 0 && s[0].equals(PREFIX_OPTION)).findFirst().orElse(null);
            if (prefixOption != null) {
                if (prefixOption.length != 2) {
                    root.printError(String.format("%s option can only take one argument as properties prefix.", PREFIX_OPTION));
                    return false;
                }
                propertiesPrefix = prefixOption[1];
                if (!propertiesPrefix.endsWith(PROPERTY_SEPARATOR)) {
                    propertiesPrefix +=PROPERTY_SEPARATOR;
                }
            }

        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        for (ClassDoc classDoc : root.classes()) {
            printProperty(printWriter, propertiesPrefix, classDoc);
            for (FieldDoc fieldDoc : classDoc.fields()) {
                printProperty(printWriter, propertiesPrefix, fieldDoc);
            }
        }
        System.out.println(stringWriter);
        return true;
    }

    private static void printProperty(PrintWriter printWriter, String propertiesPrefix, ProgramElementDoc memberDoc) {
        printWriter.print(memberDoc.name().substring(0,1).toLowerCase()+memberDoc.name().substring(1));
        printWriter.print(EQUALS);
        printWriter.print(propertiesPrefix);
        printWriter.println(cleanComment(memberDoc.commentText()));
    }

    private static String cleanComment(String comment) {
        return comment.replaceAll("\\n", SPACE).replaceAll("\\s+", SPACE);
    }

}
