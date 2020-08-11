package main.java.ec504.group15.whiteBoxFuzzer;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.io.File;
import java.util.*;

public class FileParser {

    /* Constructor makes the AST with root of type CompilationUnit */
    public FileParser(String file) {
        /* Initialize to null so the try{}catch{} blocks done worry about it being uninitialized */
        CompilationUnit compU = null;

        /* Set the AST up for solving symbol types */
        TypeSolver typeSolver = new CombinedTypeSolver();
        JavaSymbolSolver symbolsolver = new JavaSymbolSolver(typeSolver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolsolver);

        /* Create the AST */
        try {
            compU = JavaParser.parse(new File(file));
        } catch (Exception e) {e.printStackTrace();}

        this.cu = compU;
        this.file = file;
        this.expectedTypes = new ArrayList<>();
        this.expressions = new ArrayList<>();
        this.inputDependantVars = new ArrayList<>();
        this.inputDependantVars.add("args[0]");
        this.strings = new HashSet<String>();
        this.ints = new TreeSet<>();
    }


    /* Finds all conditional expressions */
    public ArrayList<Expression> FindExpressions() {

        VoidVisitor<List<Expression>> expressionCollector = new BinaryExpressionCollector();

        /* Collect the expressions */
        expressionCollector.visit(cu, expressions);

        return expressions;
    }


    /* Uses JavaSymbolSolver to get expected type of args[0] */
    public ArrayList<String> FindArgType() {
        cu.findAll(AssignExpr.class).forEach(ae -> {
            if (ae.getTokenRange().get().toString().contains("args[0]")) {
                ResolvedType rt = ae.calculateResolvedType();
                expectedTypes.add(rt.describe());
                //System.out.println(ae.getTarget().toString() + " is a " + rt.describe());
            }
        });

        cu.findAll(VariableDeclarator.class).forEach(vd -> {
            if (vd.getTokenRange().get().toString().contains("args[0]")) {
                //System.out.println(vd.getName() + " is a " + vd.getType().toString());
                expectedTypes.add(vd.getType().toString());
            }
        });

        /*
        if (true){//expectedTypes.size() == 0) {
            TypeSolver reflectionSolver = new ReflectionTypeSolver();
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(reflectionSolver);
            JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
            CompilationUnit compU = null;
            try {
                compU = JavaParser.parse(new File(file));
            } catch (Exception e) {
                e.printStackTrace();
            }

            compU.findAll(MethodCallExpr.class).forEach(mce -> {
                System.out.println(mce.resolveInvokedMethod().getReturnType().toString());
            });
        }
        */

        return expectedTypes;
    }

    /* Finds all variables dependant on args[0] */
    public ArrayList<String> FindInputDependentVariables() {

        VoidVisitor<List<String>> dependencyCollector = new ArgdependencyCollector();
        dependencyCollector.visit(cu, inputDependantVars);

        return inputDependantVars;
    }

    public HashSet<String> FindStr() {

        VoidVisitor<HashSet<String>> stringCollector = new StringCollector();
        stringCollector.visit(cu, strings);

        return strings;

    }

    public TreeSet<Double> FindInt() {

        VoidVisitor<TreeSet<Double>> integerCollector = new intCollector();
        integerCollector.visit(cu, ints);

        return ints;

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Fields */
    CompilationUnit cu;
    String file;
    ArrayList<Expression> expressions;
    ArrayList<String> expectedTypes;
    ArrayList<String> inputDependantVars;

    HashSet<String> strings;
    TreeSet<Double> ints;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Private Classes */

    /* Collect the Binary Expressions (conditional statements) that depend on args[0] */
    private class BinaryExpressionCollector extends VoidVisitorAdapter<List<Expression>> {

        @Override
        public void visit (BinaryExpr b, List<Expression> list) {
            super.visit(b, list);
            if (b.getParentNode().get() instanceof IfStmt ||
                    b.getParentNode().get() instanceof WhileStmt ||
                    b.getParentNode().get() instanceof SwitchStmt ||
                    b.getParentNode().get() instanceof ForStmt) {

                for (int i=0; i<inputDependantVars.size(); i++){
                    if (b.getTokenRange().get().toString().contains(inputDependantVars.get(i))) {
                        list.add(b);
                    }
                }
            }
        }
    }

    /* Collect the names of variables that depend on args[0] */
    private class ArgdependencyCollector extends VoidVisitorAdapter<List<String>> {

        @Override
        public void visit (ExpressionStmt es, List<String> list) {
            super.visit(es, list);

            int i;
            int size;
            if (es.getExpression() instanceof VariableDeclarationExpr || es.getExpression() instanceof AssignExpr) {
                size = list.size();
                for (i=0; i<size; i++) {
                    if (es.getTokenRange().get().toString().contains(inputDependantVars.get(i))) {
                        if (es.getExpression() instanceof  AssignExpr) {
                            list.add(((AssignExpr) es.getExpression()).getTarget().toString());
                        } else if (es.getExpression() instanceof VariableDeclarationExpr) {
                            list.add(((VariableDeclarationExpr) es.getExpression()).getVariable(0).getNameAsString());
                        }
                    }
                }
            }
        }
    }

    private class StringCollector extends VoidVisitorAdapter<HashSet<String>> {

        @Override
        public void visit (StringLiteralExpr strLit, HashSet<String> list) {
            super.visit(strLit, list);

            list.add(strLit.toString());
        }
    }

    private class intCollector extends VoidVisitorAdapter<TreeSet<Double>> {

        @Override
        public void visit (IntegerLiteralExpr intLit, TreeSet<Double> list) {
            super.visit(intLit, list);

            list.add(new Double(intLit.asInt()));
        }
    }


}