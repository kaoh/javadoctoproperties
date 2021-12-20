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
import java.util.*;


/**
 * Converts the passes source path and sub directories and stores the Javadocs as properties.
 *
 * @author Karsten Ohme
 */
public class Converter9 implements Doclet {

    private static final String EQUALS = "=";
    private static final String SPACE = " ";
    private static final String PROPERTY_SEPARATOR = ".";
    private static final String NAME = "JavaDoc to Properties";

    private static final String PREFIX_OPTION = "-prefix";
    private static final String OUTPUT_OPTION = "-output";
    private static final String INCLUDE_OPTION = "-includes";
    private static final String EXCLUDE_OPTION = "-excludes";
    private static final String EMPTY = "";
    private static final String DEFAULT_OUTPUT = "javadoc.properties";

    private Reporter reporter;

    private String output = DEFAULT_OUTPUT;

    private String propertiesPrefix = EMPTY;

    private List<String> includes;

    private List<String> excludes;

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
                              output = list.get(0);
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
                        propertiesPrefix = list.get(0);
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
                        return String.format("The includes classes separated by a colon (:). Default: %s", EMPTY);
                    }

                    @Override
                    public Kind getKind() {
                        return Kind.STANDARD;
                    }

                    @Override
                    public List<String> getNames() {
                        return List.of(INCLUDE_OPTION, "-i");
                    }

                    @Override
                    public String getParameters() {
                        return includes != null ? String.join(":", includes) : EMPTY;
                    }

                    @Override
                    public boolean process(String s, List<String> list) {
                        includes = Arrays.asList(list.get(0).split(":"));
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
                        return String.format("The excludes classes separated by a colon (:). Default: %s", EMPTY);
                    }

                    @Override
                    public Kind getKind() {
                        return Kind.STANDARD;
                    }

                    @Override
                    public List<String> getNames() {
                        return List.of(EXCLUDE_OPTION, "-e");
                    }

                    @Override
                    public String getParameters() {
                        return excludes != null ? String.join(":", excludes) : EMPTY;
                    }

                    @Override
                    public boolean process(String s, List<String> list) {
                        excludes = Arrays.asList(list.get(0).split(":"));
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
        typeElements.stream().filter(this::matchFilter).forEach(
                t -> {
                    printProperty(printWriter, propertyPrefix, t, docTrees);
                    for (VariableElement fieldDoc : ElementFilter.fieldsIn(t.getEnclosedElements())) {
                        printProperty(printWriter, propertyPrefix + buildPropertyName(t), fieldDoc, docTrees);
                    }
                }
        );
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(output))) {
            reporter.print(Diagnostic.Kind.NOTE, String.format("Writing to %s", new File(output).getAbsolutePath()));
            outputStreamWriter.write(stringWriter.toString());
        } catch (IOException e) {
            reporter.print(Diagnostic.Kind.ERROR, String.format("Could not write to properties file: %s", e.getMessage()));
            return false;
        }
        return true;
    }

    private static String getRegEx(String pattern) {
        String _pattern = pattern.replace(".", "\\.");
        _pattern = _pattern.replace("**", ".*");
        _pattern = _pattern.replace("*", ".*");
        _pattern = _pattern.replace("?", ".");
        return _pattern;
    }

    private boolean matchFilter(TypeElement typeElement) {
        return (includes == null || includes.stream().anyMatch(
                include -> {
                            boolean matches = typeElement.getQualifiedName().toString().matches(getRegEx(include));
                            if (matches) {
                                reporter.print(Diagnostic.Kind.NOTE, String.format("Including class: '%s'", typeElement.getQualifiedName().toString()));
                            }
                            return matches;
                        }
                )
        )
                && (excludes == null ||
                excludes.stream().noneMatch(
                        exclude -> {
                            boolean matches = typeElement.getQualifiedName().toString().matches(getRegEx(exclude));
                            if (matches) {
                                reporter.print(Diagnostic.Kind.NOTE, String.format("Excluding class: '%s'", typeElement.getQualifiedName().toString()));
                            }
                            return matches;
                        })
        );
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

    private void printProperty(PrintWriter printWriter, String propertiesPrefix, Element memberDoc, DocTrees docTrees) {
        printWriter.print(buildPrefix(propertiesPrefix));
        printWriter.print(buildPropertyName(memberDoc));
        printWriter.print(EQUALS);
        if (docTrees.getDocCommentTree(memberDoc) == null) {
            reporter.print(Diagnostic.Kind.WARNING, String.format("Missing comment on property '%s.%s'",
                    memberDoc.getEnclosingElement().getSimpleName().toString(), memberDoc.getSimpleName().toString()));
            printWriter.println();
        }
        else {
            printWriter.println(cleanComment(docTrees.getDocCommentTree(memberDoc).getFullBody().toString()));
        }
    }

    private static String cleanComment(String comment) {
        return comment.replaceAll("\\n", SPACE).replaceAll("\\s+", SPACE).replaceAll(",<", "<").replaceAll(">,", ">");
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
