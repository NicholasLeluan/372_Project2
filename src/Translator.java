import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//need help with how to write a cla to a java file - T
//how to match with negative numbers? 
//can int mod real be allowed?


public class Translator {
	static FileWriter f = null;

    private static Scanner scanner;
    HashMap<String, Integer> integerVariables = new HashMap<String, Integer>();
    HashMap<String,Data> variableMap = new HashMap<>();
    static ArrayList<String> keywords = new ArrayList<String>();
    static ArrayList<String> variables = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
    	//@Nick - dont cringe its good enough for now lol
    	keywords.add("var");
    	keywords.add("add");
    	keywords.add("mult");
    	keywords.add("div");
    	keywords.add("mod");
    	keywords.add("true");
    	keywords.add("false");
    	keywords.add("or");
    	keywords.add("not");
    	keywords.add("less than");
    	keywords.add("greater than");
    	keywords.add("less than or equal to");
    	keywords.add("greater than or equal to");
    	keywords.add("equal to");
    	keywords.add("not equal to");
    	keywords.add("if");
    	keywords.add("then");
    	keywords.add("or if");
    	keywords.add("or");
    	//keywords.add("while this");
    	//keywords.add("increment by");
    	//keywords.add("do that");
    	keywords.add("end");
    	keywords.add("from");
    	keywords.add("to");
    	keywords.add("output");
    	keywords.add("outputs");
    	keywords.add("text");
    	keywords.add("cla");
    	
    	
    	//creates java file
        // Test3 will be the name of file being run which SHOULD be the 0th command line argument
    	f = new FileWriter("Test3.java");
    	f.write("public class Test3 {\n");
    	f.write("\tpublic static void main(String[] args){\n");
    	// gets added to from what we read

        // Pattern for variable assignment
        Pattern variableAssignmentPattern = Pattern.compile("var (.+)");

        //
        Pattern existingVariableAssignmentPattern = Pattern.compile("(.+) = (.*)");

        // Pattern for if statements; will start with if, end with 'end'
        Pattern ifStatementPattern = Pattern.compile("^(if)");


        // we will read the file
        // for each line -> read the "header" words that indicate what the statement is
        // based on the context call certain methods for parsing
        // if line[0] == var

        File file = new File(args[0]);
        System.out.println(String.format("OPENED FILE:: %s",args[0]));
        scanner = new Scanner(file);
        int lineNo = 1;
        int openIfs = 0; //used as flag to determine correct if formatting
        while (scanner.hasNext()){
            String line = scanner.nextLine();
            Matcher newVariableAssignment = variableAssignmentPattern.matcher(line);
            Matcher oldVariableAssignment = existingVariableAssignmentPattern.matcher(line);
            //Matcher ifStatement = ifStatementPattern.matcher(line);
            // if the read in line is a variable assignment call the variableAssignment method
            if (newVariableAssignment.matches() || oldVariableAssignment.matches()){
                //System.out.println("ADD VARIABLE::"+addVariable(line));
                f.write(addVariable(line));
            }else if(line.trim().startsWith("if")){
                //System.out.println("IF OUT IN MAIN:: " + ifStatement(line));
                f.write(ifStatement(line)+"\n");
                openIfs += 1;
            }else if(line.trim().equals("end if")){
                f.write("}\n");
                openIfs -=1;
            }
            lineNo++;
        }
        if (openIfs > 0){
            /// TODO:: IF FORMATTING ERROR
            // will probably need the same for the loops
        }else{
            f.write("\n}\n");
            f.write("}");
            f.close();
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
     * @throws IOException 
     */
    private static String addVariable(String line) throws IOException{
        String retVal = "";

        Pattern newVarPattern = Pattern.compile("var (.*) = (.*)");
        Matcher newVarMatcher = newVarPattern.matcher(line);

        Pattern varAssignmentPattern = Pattern.compile("(.*) = (.*)");
        Matcher varAssignmentMatcher = varAssignmentPattern.matcher(line);

//        Pattern integerPattern = Pattern.compile("var (.+) = (\\d+)");
//        Matcher integerMatcher = integerPattern.matcher(line);
//
//        Pattern realsPattern = Pattern.compile("var (.+) = (\\d+\\.\\d+)");
//        Matcher realsMatcher = realsPattern.matcher(line);
//
//        Pattern isReal = Pattern.compile("(\\d+\\.\\d+)");
//
//        //not sure on these
//        Pattern addPattern = Pattern.compile("var (.+) = ((\\d+)|(\\d+\\.\\d+)) add ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
//        Matcher addPatternMatcher = addPattern.matcher(line);
//
//        Pattern addPattern1 = Pattern.compile("(.+) = ((\\d+)|(\\d+\\.\\d+)) add ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
//        Matcher addPattern1Matcher = addPattern1.matcher(line);
//
//        Pattern subPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) sub ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
//        Matcher subPatternMatcher = subPattern.matcher(line);
//
//        Pattern multPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) mult ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
//        Matcher multPatternMatcher = multPattern.matcher(line);
//
//        Pattern divPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) div ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
//        Matcher divPatternMatcher = divPattern.matcher(line);
//
//        Pattern modPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) mod ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
//        Matcher modPatternMatcher = modPattern.matcher(line);
//
//        Pattern commandLinePattern = Pattern.compile("var (.+) = cmd\\((\\d+)\\)");
//        Matcher commandLineMatcher = commandLinePattern.matcher(line);
        if(newVarMatcher.matches()){
            String variable = newVarMatcher.group(1);// the variable name
            if(variables.contains(variable)){
                System.out.println("ERROR: Variable has already been assigned");
                return null;
            }
            String rightExpression = expression(newVarMatcher.group(2));
            String exprClass = getClass(newVarMatcher.group(2));
            retVal = String.format("%s %s = %s;\n",exprClass,variable,rightExpression);
            variables.add(variable);
        }else if(varAssignmentMatcher.matches()){
            System.out.print("already defined: "+line);
            String variable = varAssignmentMatcher.group(1);
            if(!variables.contains(variable)){
                System.out.println("Variable does not exist! Throw error");
                return null;
            }
            String rightExpression = expression(varAssignmentMatcher.group(2));
            retVal = String.format("%s = %s;",variable,rightExpression);
        }
//        if(integerMatcher.matches()){ //matches ints
//        	if (keywords.contains(integerMatcher.group(1))) {
//        		//TODO Throw an error message!!!!!!!!!!!!!!!!!!!!
//        	}
//        	variables.add(integerMatcher.group(1));
//        	f.write("\t\tint " +  integerMatcher.group(1) + " = " + integerMatcher.group(2)+ ";\n");
//        	//f.write("\t\tSystem.out.println(" + integerMatcher.group(1) + ");\n");
//
//            System.out.println(String.format("Matched %s with INTEGER",integerMatcher.group(2)));
//
//        }else if(realsMatcher.matches()){ //matches reals
//        	if (keywords.contains(realsMatcher.group(1))) {
//        		//TODO Throw an error message!!!!!!!!!!!!!!!!!!!! variable is a keyword
//        	}
//        	variables.add(integerMatcher.group(1));
//        	f.write("\t\tdouble " +  integerMatcher.group(1) + " = " + integerMatcher.group(2)+ ";\n");
//        	//f.write("\t\tSystem.out.println(" + integerMatcher.group(1) + ");\n");
//
//            System.out.println(String.format("Matched %s with FLOAT",realsMatcher.group(2)));
//        // creating a variable with an add expression
//        }else if(addPatternMatcher.matches()){ //matches add
//        	//what is this adding to though??????
//        	f.write(addPatternMatcher.group(0) + " + " + addPatternMatcher.group(2)+ ";\n");
//
//            System.out.println(String.format("Matched %s with ADD",addPatternMatcher.group(1)));
//        // taking an already defined variable; assigning it to the add expression
//        }else if (addPattern1Matcher.matches()){
//            if(variables.contains(addPattern1Matcher.group(0))){
//                Matcher val1 = isReal.matcher(addPattern1Matcher.group(1));
//                Matcher val2 = isReal.matcher(addPattern1Matcher.group(2));
//                if (val1.matches() || val2.matches()){
//                    f.write("double "+ addPattern1Matcher.group(0) + " = " + addPattern1Matcher.group(1) + " + " + addPattern1Matcher.group(2)+";\n");
//                }else{
//                    f.write("int "+ addPattern1Matcher.group(0) + " = " + addPattern1Matcher.group(1) + " + " + addPattern1Matcher.group(2)+";\n");
//                }
//            }else{
//                // TODO: THROW VARIABLE NOT FOUND ERROR!!!!!!!!!!!!!!!
//                System.out.println("THIS IS AN ERROR; variable has not been created");
//            }
//        }
//        else if(commandLineMatcher.matches()){ //matches cla
//        	if (keywords.contains(commandLineMatcher.group(1))) {
//        		//TODO Throw an error message!!!!!!!!!!!!!!!!!!!!
//        	}
//
//        	//TODO write to java file!!!!!!!!!
//
//            System.out.println(String.format("Matched %s with COMMAND LINE",commandLineMatcher.group(2)));
//
//        }else{
//        	//TODO Error messgage!!!!!!!!!!!!!!!!!!!!
//            System.out.println("Did not match, likely throw an error here");
//        }
        return retVal;

    }

    /**
     * Method that writes a new if statement; this does not check if the contents are
     * complete; this does check to see if the if statement is formatted correctly;
     * if it is not, errors will be thrown
     * @param line
     * @return
     */
    private static String ifStatement(String line){
        String retVal = "if ("; // start
        Pattern extractConditional = Pattern.compile("if (.*) then");
        Matcher formatCheck = extractConditional.matcher(line.trim());
        if(formatCheck.matches()){
            // gets the condition statement that makes the if statement;
            // only handles one conditional at the moment
            retVal += conditionalStatement(formatCheck.group(1));
        }else{
            // TODO: throw an improper formatting error
            System.out.println("ERROR:"+line.trim());
            return null;
        }
        retVal += "){";
        return retVal;
    }

    /**
     * Method that is called when either a conditional statement is expected or
     * needed to be extracted;
     * @param line String value that should contain a valid matching conditional statement
     * @return String value of the built Java translation of the conditional
     */
    private static String conditionalStatement(String line){
        // GREATER THAN OR EQUAL TO
        Pattern condGTOE = Pattern.compile("(.*) greater than or equal to (.*)");
        Matcher matcherGTOE = condGTOE.matcher(line.trim());
        // GREATER THAN
        Pattern condGT = Pattern.compile("(.*) greater than (.*)");
        Matcher matcherGT = condGT.matcher(line.trim());
        // LESS THAN OR EQUAL TO
        Pattern condLTOE = Pattern.compile("(.*) less than or equal to (.*)");
        Matcher matcherLTOE = condLTOE.matcher(line.trim());
        // LESS THAN
        Pattern condLT = Pattern.compile("(.*) less than (.*)");
        Matcher matcherLT = condLT.matcher(line.trim());
        // EQUAL TO
        Pattern condEql = Pattern.compile("(.*) equals (.*)");
        Matcher matcherEql = condEql.matcher(line.trim());
        //NOT EQUAL TO
        Pattern condNotEql = Pattern.compile("(.*) not equal to (.*)");
        Matcher matcherNotEql = condNotEql.matcher(line.trim());

        if(matcherGTOE.matches()){
            return String.format("%s >= %s",
                    expression(matcherGTOE.group(1)),expression(matcherGTOE.group(2)));
        }else if(matcherGT.matches()){
            return String.format("%s > %s",
                    expression(matcherGT.group(1)),expression(matcherGT.group(2)));
        }else if(matcherLTOE.matches()){
            return String.format("%s <= %s",
                    expression(matcherLTOE.group(1)),expression(matcherLTOE.group(2)));
        }else if(matcherLT.matches()){
            return String.format("%s < %s",
                    expression(matcherLT.group(1)),expression(matcherLT.group(2)));
        }else if(matcherEql.matches()){
            return String.format("%s == %s",
                    expression(matcherEql.group(1)),expression(matcherEql.group(2)));
        }else if(matcherNotEql.matches()){
            return String.format("%s != %s",
                    expression(matcherNotEql.group(1)),expression(matcherNotEql.group(2)));
        }
        //TODO::
        System.out.println("DID NOT MATCH IN CONDITIONAL; THROW ERROR");
        return null;
    }

    /**
     * Reduces a given string to a simple expression; can perform the singleton
     * @param expr
     */
    private static String expression(String expr){
        Pattern addPattern = Pattern.compile("(.*) add (.*)");
        Matcher addPatternMatcher = addPattern.matcher(expr);

        Pattern subPattern = Pattern.compile("(.*) sub (.*)");
        Matcher subPatternMatcher = subPattern.matcher(expr);

        Pattern multPattern = Pattern.compile("(.*) mult (.*)");
        Matcher multPatternMatcher = multPattern.matcher(expr);

        Pattern divPattern = Pattern.compile("(.*) div (.*)");
        Matcher divPatternMatcher = divPattern.matcher(expr);

        Pattern modPattern = Pattern.compile("(.*) mod (.*)");
        Matcher modPatternMatcher = modPattern.matcher(expr);

        Pattern methodPattern = Pattern.compile("(.*)\\((.*)\\)");
        Matcher methodMatcher = methodPattern.matcher(expr);

        Pattern singletonPattern = Pattern.compile("(.*)");
        Matcher singletonPatternMatcher = singletonPattern.matcher(expr);

        // maybe add logic within the if statements checking for int/float expressions
        if(addPatternMatcher.matches()){
            return String.format("%s + %s",addPatternMatcher.group(1),addPatternMatcher.group(2));
        }else if(subPatternMatcher.matches()){
            return String.format("%s - %s",subPatternMatcher.group(1),subPatternMatcher.group(2));
        }else if(multPatternMatcher.matches()){
            return String.format("%s * %s",multPatternMatcher.group(1),multPatternMatcher.group(2));
        }else if(divPatternMatcher.matches()){
            return String.format("%s / %s",divPatternMatcher.group(1),divPatternMatcher.group(2));
        }else if(modPatternMatcher.matches()){
            return String.format("%s %% %s",modPatternMatcher.group(1),modPatternMatcher.group(2));
        }else if(singletonPatternMatcher.matches()){
            return String.format("%s",singletonPatternMatcher.group(1));
        }
        return null;
    }

    private static String getClass(String expression){
        Pattern doublePattern = Pattern.compile("(\\d.\\d)");

        Pattern mathExpression = Pattern.compile("(\\d|\\d.\\d) (add|sub|mult|div|mod) (\\d|\\d.\\d)");
        Matcher mathMatcher = mathExpression.matcher(expression.trim());

        Pattern booleanExpression = Pattern.compile("(true|false)");
        Matcher booleanMatcher = booleanExpression.matcher(expression.trim());

        //TODO: can probably adapt this pretty easily for strings

        if(mathMatcher.matches()) {
            boolean a = doublePattern.matcher(mathMatcher.group(1)).matches();
            boolean b = doublePattern.matcher(mathMatcher.group(3)).matches();
            if (a || b) {
                return "double";
            }
            return "int";
        }else if(booleanMatcher.matches()){
            return "boolean";
        }
        return null;
    }

}
