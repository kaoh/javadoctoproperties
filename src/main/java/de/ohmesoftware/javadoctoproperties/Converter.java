package de.ohmesoftware.javadoctoproperties;

import com.sun.javadoc.*;

import java.io.*;

/**
 * Converts the passes source path and sub directories and stores the Javadocs as properties.
 *
 * @author Karsten Ohme
 */
public class Converter {

    private static final String EQUALS = "=";
    private static final String SPACE = " ";
    private static final String PROPERTY_SEPARATOR = ".";


    /**
     * As specified by the Doclet specification.
     *
     * @return Java 1.5.
     *
     * @see com.sun.javadoc.Doclet#languageVersion()
     */
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    /**
     * As specified by the Doclet specification.
     *
     * @param option The option name.
     *
     * @return The length of the option.
     *
     * @see com.sun.javadoc.Doclet#optionLength(String)
     */
    public static int optionLength(String option) {
        return Options.optionLength(option);
    }

    /**
     * Validates the options.
     * @param options The options.
     * @param reporter The error reporter.
     * @return <code>true</code> if options are valid.
     */
    public static boolean validOptions(String options[][],
                                       DocErrorReporter reporter) {
        Options _options = new Options();
        boolean optionsLoaded = _options.load(options, reporter);
        if (!optionsLoaded) {
            return false;
        }
        return true;
    }

    /**
     * Doclet start method.
     * @param root The documentation root.
     * @return <code>true</code> if the execution was successful.
     */
    public static boolean start(RootDoc root) {
        Options options = new Options();
        boolean optionsLoaded = options.load(root.options(), root);
        if (!optionsLoaded) {
            return false;
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        String propertyPrefix = buildPrefix(options.getPropertiesPrefix());
        for (ClassDoc classDoc : root.classes()) {
            printProperty(printWriter, propertyPrefix, classDoc);
            for (FieldDoc fieldDoc : classDoc.fields()) {
                printProperty(printWriter, propertyPrefix+buildPropertyName(classDoc), fieldDoc);
            }
        }
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(options.getOutput()))) {
            root.printNotice(String.format("Writing to %s", new File(options.getOutput()).getAbsolutePath()));
            outputStreamWriter.write(stringWriter.toString());
        } catch (IOException e) {
           root.printError(String.format("Could not write to properties file: %s", e.getMessage()));
           return false;
        }
        return true;
    }

    private static String buildPropertyName(ProgramElementDoc memberDoc) {
        return memberDoc.name().substring(0,1).toLowerCase()+memberDoc.name().substring(1);
    }

    private static String buildPrefix(String propertiesPrefix) {
        if (!propertiesPrefix.endsWith(PROPERTY_SEPARATOR)) {
            propertiesPrefix +=PROPERTY_SEPARATOR;
        }
        return propertiesPrefix;
    }

    private static void printProperty(PrintWriter printWriter, String propertiesPrefix, ProgramElementDoc memberDoc) {
        printWriter.print(buildPrefix(propertiesPrefix));
        printWriter.print(buildPropertyName(memberDoc));
        printWriter.print(EQUALS);
        printWriter.println(cleanComment(memberDoc.commentText()));
    }

    private static String cleanComment(String comment) {
        return comment.replaceAll("\\n", SPACE).replaceAll("\\s+", SPACE);
    }

}
