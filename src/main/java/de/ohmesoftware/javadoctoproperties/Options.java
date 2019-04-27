package de.ohmesoftware.javadoctoproperties;

import com.sun.javadoc.DocErrorReporter;
import com.sun.tools.doclets.standard.Standard;

import java.util.Arrays;

/**
 * Options.
 *
 * @author Karsten Ohme
 */
public class Options {

    public static final String PREFIX_OPTION = "-prefix";
    public static final String OUTPUT_OPTION = "-output";

    private static final String EMPTY = "";

    private String propertiesPrefix = EMPTY;

    private String output = "javadoc.properties";

    public static int optionLength(String option) {
        switch ( option ) {
            case PREFIX_OPTION:
            case OUTPUT_OPTION:
                return 2;
        }

        return Standard.optionLength(option);
    }

    /**
     * Returns the property prefix.
     * @return the property prefix.
     */
    public String getPropertiesPrefix() {
        return propertiesPrefix;
    }


    public boolean load(String[][] options, DocErrorReporter errorReporter) {
        if (options != null && options.length > 0) {
            String[] prefixOption = Arrays.stream(options).filter(s -> s.length > 0 && s[0].equals(PREFIX_OPTION)).findFirst().orElse(null);
            if (prefixOption != null) {
                if (prefixOption.length != 2) {
                    errorReporter.printError(String.format("%s option can only take one argument as properties prefix.", PREFIX_OPTION));
                    return false;
                }
                propertiesPrefix = prefixOption[1];
            }
            String[] outputOption = Arrays.stream(options).filter(s -> s.length > 0 && s[0].equals(OUTPUT_OPTION)).findFirst().orElse(null);
            if (outputOption != null) {
                if (outputOption.length != 2) {
                    errorReporter.printError(String.format("%s option can only take one argument as output file.", OUTPUT_OPTION));
                    return false;
                }
                output = outputOption[1];
            }
        }
        return true;
    }

    /**
     * Gets the output file.
     *
     * @return the output file.
     */
    public String getOutput() {
        return output;
    }
}
