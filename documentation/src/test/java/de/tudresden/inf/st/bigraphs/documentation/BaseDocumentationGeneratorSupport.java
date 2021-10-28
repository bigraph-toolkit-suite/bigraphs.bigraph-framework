package de.tudresden.inf.st.bigraphs.documentation;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Assertions;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Base class to use for automating the generation of code samples that are going to be included in the user manual.
 * <p>
 * The user has to implement these abstract methods:
 * <ul>
 *     <li>{@code acceptedMethods}</li>
 *     <li>{@code generateCodeBlockOutput}</li>
 * </ul>
 * <p>
 * The generation procedure can then be initiated by calling {@link BaseDocumentationGeneratorSupport#runParser(BaseDocumentationGeneratorSupport, String)}.
 *
 * @author Dominik Grzelak
 */
public abstract class BaseDocumentationGeneratorSupport {

    Queue<Pair<Position, Position>> positionStack = new LinkedBlockingDeque<>();
    Pair<Position, Position> currentPosition;
    List<CodeBlock> codeBlocks = new ArrayList<>();

    public static Path getMavenModuleRoot(Class<? extends BaseDocumentationGeneratorSupport> clazz) {
        return CodeGenerationUtils.mavenModuleRoot(clazz);
    }

    private static SourceRoot getSourceRoot(Class<? extends BaseDocumentationGeneratorSupport> clazz) {
        SourceRoot sourceRoot = new SourceRoot(getMavenModuleRoot(clazz).resolve("src/test/java"));
        return sourceRoot;
    }

    public static final String CODE_FENCE_START = ""; //""```java";
    public static final String CODE_FENCE_END = ""; //""```";

    /**
     * Start the code sample generation procedure.
     *
     * @param instance          the instance of the test class
     * @param javaClassFilename the java filename of the test instance
     */
    public static void runParser(BaseDocumentationGeneratorSupport instance,
                                 String javaClassFilename) {
        SourceRoot sourceRoot = getSourceRoot(instance.getClass());
        CompilationUnit cu = sourceRoot.parse(instance.getClass().getPackage().getName(), javaClassFilename);
        Optional<ClassOrInterfaceDeclaration> gettingStartedGuide = cu.getClassByName(instance.getClass().getSimpleName());
        Assertions.assertTrue(gettingStartedGuide.isPresent());
        instance.parseCodeBlocks(gettingStartedGuide);
    }

    public abstract List<String> acceptedMethods();

    public abstract void generateCodeBlockOutput(List<CodeBlock> codeBlocks, MethodDeclaration methodDeclaration);

    public void parseCodeBlocks(Optional<ClassOrInterfaceDeclaration> declaration) {
        declaration.get().accept(new MethodVisitor(), "");
    }

    public static class CodeBlock {
        final List<String> lines;

        public static CodeBlock create(List<String> lines) {
            return new CodeBlock(lines);
        }

        private CodeBlock(List<String> lines) {
            this.lines = lines;
        }

        public List<String> getLines() {
            return lines;
        }

        public void addLine(String line) {
            this.lines.add(line);
        }
    }


    private class MethodVisitor extends VoidVisitorAdapter<String> {
        @Override
        public void visit(MethodDeclaration n, String arg) {
            if (acceptedMethods().contains(n.getNameAsString())) {
                codeBlocks.clear();
                positionStack.clear();
                n.getBody().ifPresent(x -> {
                    List<Comment> allContainedComments = n.getBody().get().getAllContainedComments();
                    List<Position> collect = allContainedComments.stream()
                            .sorted(
                                    Comparator.comparing(comment1 -> Integer.valueOf(((Comment) comment1).getBegin().get().line))
                            )
                            .map(c -> c.getBegin().get()).collect(Collectors.toList());

                    IntStream.range(0, collect.size())
                            .filter(i -> i % 2 == 0)
                            .mapToObj(i -> new Pair(collect.get(i), collect.get(i + 1)))
                            .map(x2 -> (Pair<Position, Position>) x2)
                            .forEach(x2 -> positionStack.add(x2));
                    x.getStatements().forEach(stmt -> {
                        stmt.toExpressionStmt().ifPresent(exprStmt -> {
                            exprStmt.accept(new StatementVisitor(), stmt);
                        });
                    });
                });
                generateCodeBlockOutput(codeBlocks, n);
            }
        }
//                super.visit(n, arg);
    }

    private class StatementVisitor extends VoidVisitorAdapter<Node> {

        @Override
        public void visit(ExpressionStmt declarator, Node arg) {
            if (Objects.isNull(currentPosition) ||
                    !(declarator.getBegin().get().line > currentPosition.a.line &&
                            declarator.getBegin().get().line < currentPosition.b.line)) {
                currentPosition = positionStack.poll();
                codeBlocks.add(new CodeBlock(new ArrayList<>()));
            }
//            if (declarator.getBegin().get().line > currentPosition.a.line &&
//                    declarator.getBegin().get().line < currentPosition.b.line) {
//            System.out.println("vardeck:\t" + declarator.getExpression().toString());
            codeBlocks.get(codeBlocks.size() - 1).addLine(declarator.getExpression().toString());
//            }
        }
    }

}
