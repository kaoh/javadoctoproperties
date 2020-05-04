package de.ohmesoftware.javadoctoproperties;


import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * Converts the passes source path and sub directories and stores the Javadocs as properties.
 *
 * @author Karsten Ohme
 */
public class Converter implements Doclet {

    private static final String EQUALS = "=";
    private static final String SPACE = " ";
    private static final String PROPERTY_SEPARATOR = ".";
    private static final String NAME = "JavaDoc to Properties";

    private static final String PREFIX_OPTION = "-prefix";
    private static final String OUTPUT_OPTION = "-output";
    private static final String EMPTY = "";
    private static final String DEFAULT_OUTPUT = "javadoc.properties";

    private Reporter reporter;

    private String output = DEFAULT_OUTPUT;

    private String propertiesPrefix = EMPTY;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of(new Option() {
                          @Override
                          public int getArgumentCount() {
                              return 1;
                          }

                          @Override
                          public String getDescription() {
                              return String.format("The output file name including the path. Default: %s", DEFAULT_OUTPUT);
                          }

                          @Override
                          public Kind getKind() {
                              return Kind.STANDARD;
                          }

                          @Override
                          public List<String> getNames() {
                              return List.of(OUTPUT_OPTION, "-o");
                          }

                          @Override
                          public String getParameters() {
                              return output != null ? output : DEFAULT_OUTPUT;
                          }

                          @Override
                          public boolean process(String s, List<String> list) {
                              if (list.size() != 1) {
                                  return false;
                              }
                              list.stream().findFirst().ifPresent(f -> output = f);
                              return true;
                          }
                      },
                new Option() {

                    @Override
                    public int getArgumentCount() {
                        return 1;
                    }

                    @Override
                    public String getDescription() {
                        return String.format("The property prefix used for all keys. Default: %s", EMPTY);
                    }

                    @Override
                    public Kind getKind() {
                        return Kind.STANDARD;
                    }

                    @Override
                    public List<String> getNames() {
                        return List.of(PREFIX_OPTION, "-p");
                    }

                    @Override
                    public String getParameters() {
                        return propertiesPrefix != null ? propertiesPrefix : EMPTY;
                    }

                    @Override
                    public boolean process(String s, List<String> list) {
                        if (list.size() != 1) {
                            return false;
                        }
                        list.stream().findFirst().ifPresent(f -> propertiesPrefix = f);
                        return true;
                    }
                }
        );

    }

    /**
     * Doclet start method.
     *
     * @param docletEnvironment The documentation root.
     * @return <code>true</code> if the execution was successful.
     */
    @Override
    public boolean run(DocletEnvironment docletEnvironment) {
        DocTrees docTrees = docletEnvironment.getDocTrees();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        String propertyPrefix = buildPrefix(propertiesPrefix);
        Set<TypeElement> typeElements = ElementFilter.typesIn(docletEnvironment.getSpecifiedElements());
        for (TypeElement typeDoc : typeElements) {
            printProperty(printWriter, propertyPrefix, typeDoc, docTrees);
            for (VariableElement fieldDoc : ElementFilter.fieldsIn(typeDoc.getEnclosedElements())) {
                printProperty(printWriter, propertyPrefix + buildPropertyName(typeDoc), fieldDoc, docTrees);
            }
        }
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(output))) {
            reporter.print(Diagnostic.Kind.NOTE, String.format("Writing to %s", new File(output).getAbsolutePath()));
            outputStreamWriter.write(stringWriter.toString());
        } catch (IOException e) {
            reporter.print(Diagnostic.Kind.ERROR, String.format("Could not write to properties file: %s", e.getMessage()));
            return false;
        }
        return true;
    }

    private static String buildPropertyName(Element memberDoc) {
        return memberDoc.getSimpleName().toString().substring(0, 1).toLowerCase() + memberDoc.getSimpleName().toString().substring(1);
    }

    private static String buildPrefix(String propertiesPrefix) {
        if (!propertiesPrefix.endsWith(PROPERTY_SEPARATOR) && !propertiesPrefix.equals(EMPTY)) {
            propertiesPrefix += PROPERTY_SEPARATOR;
        }
        return propertiesPrefix;
    }

    private static void printProperty(PrintWriter printWriter, String propertiesPrefix, Element memberDoc,  DocTrees docTrees) {
        printWriter.print(buildPrefix(propertiesPrefix));
        printWriter.print(buildPropertyName(memberDoc));
        printWriter.print(EQUALS);
        printWriter.println(cleanComment(docTrees.getDocCommentTree(memberDoc).getFullBody().toString()));
    }

    private static String cleanComment(String comment) {
        return comment.replaceAll("\\n", SPACE).replaceAll("\\s+", SPACE);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

}
