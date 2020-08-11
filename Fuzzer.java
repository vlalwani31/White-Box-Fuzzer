package main.java.ec504.group15.whiteBoxFuzzer;


import java.lang.Process;
import java.io.*;
import java.util.*;

public class Fuzzer {

    Fuzzer() {
        program = null;
    }

    Fuzzer(String prog) {
        program = prog;
        fileName = prog;
    }

    Fuzzer(String prog, String regExpression) {
        program = prog;
        fileName = prog;
        regExp = regExpression;
    }

    public void setRegExp(String regExpression) {
        regExp = regExpression;
    }

    public void setProgram(String prog) {
        program = prog;
        fileName = prog;
    }

    public String getRegExp() {
        return regExp;
    }

    public String getProgram() {
        return program;
    }


    public String runFuzzer() {
        String fuzzResults = "";
        String input = "";
        String pOut = "";
        int exitCode = 0;

        if (program == null) {
            return "ec504.group15.whiteBoxFuzzer.Fuzzer can not run without a valid file input.";
        }

        /* Make sure a java program was passed */
        if (!program.substring(program.length() - 5).equals(".java")) {
            return "File is not a .java file!";
        }


        /* Compile the test program */
        try {
            /* exec() the compile and make sure it completes */
            String compileArgs[] = {"javac", program};
            Process compile = Runtime.getRuntime().exec(compileArgs);
            //print current directory
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            compile.waitFor();
            if (compile.exitValue() != 0) {
                return "Error compiling the java program";
            } else {
                /* trim .java off the program name for exec'ing it */
                program = program.substring(0, program.length() - 5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Create the Input Generator */
        InputGenerator inputGenerator = new InputGenerator(fileName);
        inputGenerator.displayParsedFileInfo();

        /* get fuzzed input and run the code with it */
        String arg[] = {"java", program, null};
        String argNull[] = {"java", program};
        Process p;
        final long NANOSECONDS_IN_SECOND = 1_000_000_000;
        final long startTime = System.nanoTime();
        do {
            try {
                input = inputGenerator.createInput();
                if (input == null){
                    p = Runtime.getRuntime().exec(argNull);
                } else {
                    arg[2] = input;
                    p = Runtime.getRuntime().exec(arg);
                }
                p.waitFor();
                exitCode = p.exitValue();
                BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((pOut = is.readLine()) != null) {
                    System.out.println(pOut);
                }
            } catch (Exception e) {
                /*Handle exception*/
                e.printStackTrace();
            }

        } while (exitCode == 0 && ((System.nanoTime()-startTime) < 60*NANOSECONDS_IN_SECOND));

        if (exitCode != 0)
            fuzzResults = "Process failed with input: '" + input + "'";
        else
            fuzzResults = "Process fuzzed successfully.";

        /* Clean up the .class function created */
        try {
            String cleanArgs[] = {"rm", program + ".class"};
            Process clean = Runtime.getRuntime().exec(cleanArgs);
            clean.waitFor();
        } catch (Exception e) {
        }
        return fuzzResults;
    }

    /* opens and reads the source file
     * and returns a List of possible vulnerability
     * input templates.
     */
    private List<String> readSourceFile(String file) {
        String line = "";
        List<String> vulnerable = new ArrayList<>();

        return vulnerable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Fields */
    private String program = null;
    private String fileName;
    private String regExp = null;
}