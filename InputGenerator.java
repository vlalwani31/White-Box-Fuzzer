package main.java.ec504.group15.whiteBoxFuzzer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;

import java.util.*;

public class InputGenerator {

    public InputGenerator(String file) {
        FileParser parser = new FileParser(file);

        /* Get a guess for the type/style of input */
        argTypes = parser.FindArgType();

        /* Find variables dependant on args[0] */
        dependantArgNames = parser.FindInputDependentVariables();

        /* Find all conditional branches that we can manipulate */
        conditions = parser.FindExpressions();

        strLiterals = parser.FindStr();

        intLiterals = parser.FindInt();


        return;
    }
    public void displayParsedFileInfo() {
        System.out.println("argTypes");
        argTypes.forEach(at -> {
            System.out.println("   " + at);
        });

        System.out.println("DependantArgNames:");
        dependantArgNames.forEach((dan -> {
            System.out.println("   " + dan);
        }));

        System.out.println("conditions");
        conditions.forEach(c -> {
            System.out.println("   " + c);
        });


        System.out.println("String literals");
        strLiterals.forEach(c -> {
            System.out.println("   " + c);
        });

        System.out.println("String literals");
        intLiterals.forEach(c -> {
            System.out.println("   " + c);
        });
    }

    public String createInput() {
        String input = "";

        /*We have some general test cases that we want to make sure get generated.
         * Try these first
         */
        //

        //strLit is an array list of all string literal occurences in the test file.
        ArrayList<String> strLit = new ArrayList<>();
        strLit.addAll(strLiterals);
        int temp1 = generalTestCases.size() + strLit.size();

        if (g == 0) {
           doubles  = treeMapEditor();
        }


        int temp2 = temp1 + doubles.size();



        if (g < generalTestCases.size()) {
            g++;
            return generalTestCases.get(g-1);
        }

        else if (g < temp1) {
            g++;

            return strLit.get(g - generalTestCases.size() - 1);
        }

        else if (g < temp2){
            g++;

            return doubles.get(g - temp1 - 1).toString();
        }

        else if(g < (temp2 + conditions.size()))
        {
            Expression see = conditions.get(g - temp2);
            if ((see.toString().contains("compareTo") &&
                    see.toString().contains("args[0]") &&
                    ((BinaryExpr)(see)).getRight().toString().equals("0"))
                    || (see.toString().contains("equals") &&
                    see.toString().contains("args[0]")))
            {
                Node see1 = see.getChildNodes().get(0).getChildNodes().get(0);
                Node see2 = see.getChildNodes().get(0).getChildNodes().get(2);
                if(see1.toString().equals("args[0]"))
                {
                    g++;
                    if(see2 instanceof BinaryExpr && (!(see2.toString().contains("args[0]")))) // Doesn't have args[0]
                    {
                        String a = solvestring((BinaryExpr) see2,"",1); // 1 is for concatenation
                        return a;
                    }
                    else if(see2 instanceof BinaryExpr && see2.toString().contains("args[0]"))// because unsolvable
                    {
                        return null;
                    }
                    String a = see2.toString();
                    return a.substring(1,a.length()-1);
                }
                else if(((see1 instanceof EnclosedExpr) || (see1 instanceof BinaryExpr)) && see1.toString().contains("args[0]"))
                {
                    g++;
                    String a = see2.toString();
                    if(see2 instanceof EnclosedExpr)
                    {
                        a = solvestring(((EnclosedExpr) see2).getInner().asBinaryExpr(),"",1);
                    }
                    if(see1 instanceof EnclosedExpr)
                        return solvestring(((EnclosedExpr)(see1)).getInner().asBinaryExpr(), a,0);// 0 is for deletion
                    else
                        return solvestring((BinaryExpr)(see2), a,0);// 0 is for deletion
                }
                else if(see2.toString().equals("args[0]"))
                {
                    g++;
                    if(see1 instanceof BinaryExpr && (!(see1.toString().contains("args[0]")))) // Doesn't have args[0]
                    {
                        String a = solvestring((BinaryExpr) see1,"",1); // 1 is for concatenation
                        return a;
                    }
                    else if(see1 instanceof BinaryExpr && see1.toString().contains("args[0]"))// because unsolvable
                    {
                        return null;
                    }
                    String a = see1.toString();
                    return a.substring(1,a.length()-1);
                }
                else if(((see2 instanceof EnclosedExpr) || (see2 instanceof BinaryExpr)) && (see2.toString().contains("args[0]")))
                {
                    g++;
                    String a = see1.toString();
                    if(see1 instanceof EnclosedExpr)
                    {
                        if(((EnclosedExpr) see1).getInner() instanceof BinaryExpr)
                        {
                            a = solvestring(((EnclosedExpr) see1).getInner().asBinaryExpr(), "", 1);
                        }
                        else
                            a = ((EnclosedExpr) see1).getInner().toString();
                    }
                    if(see2 instanceof EnclosedExpr)
                        return solvestring(((EnclosedExpr)(see2)).getInner().asBinaryExpr(), a,0);// 0 is for deletion
                    else
                        return solvestring((BinaryExpr)(see2), a,0);// 0 is for deletion
                }
            }
            else if(see.toString().contains("matches"))
                looker = 1;
            else if(see.toString().contains("==") && (looker == 1))
            {
                g++;
                /*if(conditions.get(g - generalTestCases.size()).getChildNodes().get(0).toString().contains("Integer.valueOf")||
                        conditions.get(g - generalTestCases.size()).getChildNodes().get(0).toString().contains("Integer.parseInt")||
                        conditions.get(g - generalTestCases.size()).getChildNodes().get(0).toString().contains("Long.parseLong")||
                        conditions.get(g - generalTestCases.size()).getChildNodes().get(0).toString().contains("Double.parseDouble")||
                        conditions.get(g - generalTestCases.size()).getChildNodes().get(0).toString().contains("Double.parseDouble")||
                        conditions.get(g - generalTestCases.size()).getChildNodes().get(0).toString().contains("Short.parseShort"))
                {

                }*/

            }
            g++;
        }
        else if (g < (generalTestCases.size() + conditions.size() + 5))
        {
            /* Fuzz 5 completely random inputs in case there is a condition we missed */

        }

        return input;

    }

    private String solvestring(BinaryExpr b, String s, int how) {
        String updated = s;
        if((!(b.getLeft() instanceof BinaryExpr)) && (!(b.getRight() instanceof BinaryExpr))) {
            if(b.getLeft().toString().compareTo("args[0]") != 0) {
                String useless = b.getLeft().toString();
                if(how == 1)
                    updated = updated.concat(useless.substring(1,useless.length()-1));
                else
                    updated = updated.replaceFirst(useless.substring(1,useless.length()-1),"");
            }
            if(b.getRight().toString().compareTo("args[0]") != 0) {
                String useless = b.getRight().toString();
                if(how == 1)
                    updated = updated.concat(useless.substring(1,useless.length()-1));
                else
                    updated = updated.replaceFirst(useless.substring(1,useless.length()-1),"");
            }
            return updated;
        }
        else if(b.getLeft() instanceof BinaryExpr)
        {
            updated = solvestring(b.getLeft().asBinaryExpr(),s,how);
            if(b.getRight().toString().compareTo("args[0]")!=0)
            {
                String useless = b.getRight().toString();
                if(how == 1)
                    updated = updated.concat(useless.substring(1,useless.length()-1));
                else
                    updated = updated.replaceFirst(useless.substring(1,useless.length()-1),"");
            }
        }
        else if(b.getRight() instanceof BinaryExpr) {
            updated = solvestring(b.getRight().asBinaryExpr(),s,how);
            if(b.getLeft().toString().compareTo("args[0]") != 0) {
                String useless = b.getLeft().toString();
                if(how == 1)
                    updated = updated.concat(useless.substring(1,useless.length()-1));
                else
                    updated = updated.replaceFirst(useless.substring(1,useless.length()-1),"");
            }
        }
        return updated;
    }

    private ArrayList<Double> treeMapEditor() {

        ArrayList<Double> initKeys = new ArrayList<>();
        initKeys.addAll(intLiterals);

        for (Double i: initKeys) {
            intLiterals.add(-i);
        }

        initKeys.clear();
        initKeys.addAll(intLiterals);

        Random rand = new Random();

        for (int i = 0; i < initKeys.size(); i++) {
            if (i == 0) {
                intLiterals.add(initKeys.get(i) - (double)rand.nextInt(1000));
            }

            else if (i == initKeys.size() - 1) {
                intLiterals.add(initKeys.get(i) + (double)rand.nextInt(1000));
            }

            else {
                if (initKeys.get(i) >= 0) {
                    intLiterals.add((double)rand.nextInt(initKeys.get(i + 1).intValue() + 1) + initKeys.get(i));
                }
                else {
                    intLiterals.add(- 1 * ((double)rand.nextInt(-1 * initKeys.get(i + 1).intValue() + 1) + (-1 * initKeys.get(i))));
                }

            }
        }

        initKeys.clear();
        initKeys.addAll(intLiterals);

        System.out.println(initKeys);

        return initKeys;


    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Class Fields */
    private final ArrayList<String> generalTestCases = new ArrayList<>(Arrays.asList("0", null, "  ", "", "a", "-1", "a1", "1 a"));
    private int g = 0;
    ArrayList<Double> doubles;
    private ArrayList<String> argTypes;
    private ArrayList<String> dependantArgNames;
    private ArrayList<Expression> conditions;
    private HashSet<String> strLiterals;
    private TreeSet<Double> intLiterals;
    private int looker = 0;
}