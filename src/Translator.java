import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Translator {

    private static Scanner scanner;
    HashMap<String, Integer> integerVariables = new HashMap<String, Integer>();


    public static void main(String[] args) throws FileNotFoundException {

        Pattern variableAssignmentPattern = Pattern.compile("var (.+)");
        // we will read the file
        // for each line -> read the "header" words that indicate what the statement is
        // based on the context call certain methods for parsing
        // if line[0] == var

        File file = new File(args[0]);
        System.out.println(String.format("OPENED FILE:: %s\n",args[0]));
        scanner = new Scanner(file);
        while (scanner.hasNext()){
            String line = scanner.nextLine();
            Matcher variableAssignment = variableAssignmentPattern.matcher(line);
            // if the read in line is a variable assignment call the variableAssignment method
            if (variableAssignment.matches()){
                addVariable(line);
            }
        }
    }

    /**
     * This is called when a line is suspected (sus) of attempting to assing a variable
     * to some value
     * TODO:
     * - assign value for integers
     * - assign value for reals
     * - ** assingn value for Strings
     * - throw any errors:
     *  - there is no match leftside of equals
     *  - varible assingment is made when variable was already declared (i.e. the map already has
     *  the variable)
     * @param line
     */
    private static void addVariable(String line){
        Pattern integerPattern = Pattern.compile("var (.+) = (\\d+)");
        Matcher integerMatcher = integerPattern.matcher(line);

        Pattern realsPattern = Pattern.compile("var (.+) = (\\d+\\.\\d+)");
        Matcher realsMatcher = realsPattern.matcher(line);

        Pattern commandLinePattern = Pattern.compile("var (.+) = cmd\\((\\d+)\\)");
        Matcher commandLineMatcher = commandLinePattern.matcher(line);

        if(integerMatcher.matches()){
            System.out.println(String.format("Matched %s with INTEGER",integerMatcher.group(2)));
        }else if(realsMatcher.matches()){
            System.out.println(String.format("Matched %s with FLOAT",realsMatcher.group(2)));
        }else if(commandLineMatcher.matches()){
            System.out.println(String.format("Matched %s with COMMAND LINE",commandLineMatcher.group(2)));
        }else{
            System.out.println("Did not match, likely throw an error here");
        }


    }

}
