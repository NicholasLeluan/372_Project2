import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
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
    static List<String> keywords = Arrays.asList("var","add","mult","div","mod","true","false","or","not",
                                                "less than","less than or equal to","greater than","greater than or equal to",
                                                "equal to","not equal to","if","then","or","or if","end if","end from",
                                                "to","output","outputs","text","cla");
    static ArrayList<String> variables = new ArrayList<String>();
    static HashMap<String,String> variableTypes = new HashMap<>();
    static List<String> methods = Arrays.asList("cmd"); // "built-in" methods of our language

    public static void main(String[] args) throws IOException, FormattingError {
        String className = args[0].substring(0,args[0].length()-4);
        f = new FileWriter(String.format("%s.java",className)); // this writes to a Java file that matches the passed in .txt file
    	f.write(String.format("public class %s {\n",className));
    	f.write("\tpublic static void main(String[] args){\n");

        // Pattern for variable assignment
        Pattern variableAssignmentPattern = Pattern.compile("var (.+)");

        Pattern existingVariableAssignmentPattern = Pattern.compile("^\\w+(?=\\s+=) = (.*)");

        Pattern methodPattern = Pattern.compile("(.*)\\((.*)\\)");

        File file = new File(args[0]);
        System.out.println(String.format("OPENED FILE:: %s",args[0]));
        scanner = new Scanner(file);
        int lineNo = 1;
        int openIfs = 0; //used as flag to determine correct if formatting
        int openFroms = 0;
        while (scanner.hasNext()){
        	try {
            String line = scanner.nextLine().trim();
            Matcher newVariableAssignment = variableAssignmentPattern.matcher(line);
            Matcher oldVariableAssignment = existingVariableAssignmentPattern.matcher(line);
            Matcher methodMatcher = methodPattern.matcher(line);
            // if the read in line is a variable assignment call the variableAssignment method
            if (newVariableAssignment.matches() || oldVariableAssignment.matches()){
                f.write(addVariable(line)+";\n");
            }else if(line.trim().startsWith("if") || line.trim().startsWith("or if")){
                boolean startsWithOrIf = line.trim().startsWith("or if");
                if (!startsWithOrIf){
                    openIfs+=1;
                }
                f.write(ifStatement(line,startsWithOrIf)+"\n");
            }else if(line.trim().equals("or")){
                f.write("\n}else{\n");
            }
            else if(line.trim().equals("end if") || line.trim().equals("end from")){
                f.write("}\n");
                if (line.trim().equals("end if")){
                    openIfs -= 1;
                }else{
                    openFroms -= 1;
                }
            }else if(line.trim().startsWith("from")){
                f.write(fromLoopStatement(line));
                openFroms += 1;
            }else if(line.equals("stop loop now")){
                f.write("break;");
            }
            else if(methodMatcher.matches()){
                f.write(getMethod(line) + ";\n");
            }

            //END
            lineNo++;
        	} catch (Exception e) {
        		System.out.println(e.toString());
        		return;
        		
        	}
        }
        if (openIfs > 0){
            throw new FormattingError();
        }else{
            f.write("\n}\n");
            f.write("}");
            f.close();
        }
        System.out.println(variableTypes.toString());
    }

    /**
     * This is called when a line is suspected (sus) of attempting to assing a variable
     * to some value
     * @param line
     * @throws FormattingError 
     * @throws IOException 
     */
    private static String addVariable(String line) throws FormattingError {
        String retVal = "";

        Pattern newVarPattern = Pattern.compile("var (.*) = (.*)");
        Matcher newVarMatcher = newVarPattern.matcher(line);

        Pattern varAssignmentPattern = Pattern.compile("(.*) = (.*)");
        Matcher varAssignmentMatcher = varAssignmentPattern.matcher(line);

        if(newVarMatcher.matches()){
            String variable = newVarMatcher.group(1);// the variable name
            if(variables.contains(variable)){
                System.out.println("ERROR: Variable has already been assigned");
                return null;
            }
            String rightExpression = expression(newVarMatcher.group(2));
            System.out.println("GOING INT CLASS MATCH:: "+ newVarMatcher.group(2));
            String exprClass = getClass(newVarMatcher.group(2));
            retVal = String.format("%s %s = %s",exprClass,variable,rightExpression);
            variables.add(variable);
            variableTypes.put(variable,exprClass);
        }else if(varAssignmentMatcher.matches()){
            String variable = varAssignmentMatcher.group(1);
            if(!variables.contains(variable)){
                System.out.println("Variable does not exist! Throw error");
                return null;
            }
            String rightExpression = expression(varAssignmentMatcher.group(2));
            retVal = String.format("%s = %s",variable,rightExpression);
        }
        return retVal;

    }

    /**
     * Method that writes a new if statement; this does not check if the contents are
     * complete; this does check to see if the if statement is formatted correctly;
     * if it is not, errors will be thrown
     * @param line
     * @return
     * @throws FormattingError 
     * @throws ConditionalNoMatch 
     */
    private static String ifStatement(String line, boolean orIf) throws FormattingError, ConditionalNoMatch{
        String retVal = "";
        //String retVal = "if ("; // start
        if (orIf){
            retVal += "\n}else if (";
        }else{
            retVal += "if(";
        }
        Pattern extractConditional = Pattern.compile("(if|or if) (.*) then");
        Matcher formatCheck = extractConditional.matcher(line.trim());
        if(formatCheck.matches()){
            retVal += conditionalStatement(formatCheck.group(2));
        }else{
            // TODO: throw an improper formatting error
            System.out.println("ERROR:"+line.trim());
            throw new FormattingError();
            //return null;
        }
        retVal += "){";
        return retVal;
    }

    /**
     * Method that is called when either a conditional statement is expected or
     * needed to be extracted;
     * @param line String value that should contain a valid matching conditional statement
     * @return String value of the built Java translation of the conditional
     * @throws ConditionalNoMatch 
     * @throws FormattingError 
     */
    private static String conditionalStatement(String line) throws ConditionalNoMatch, FormattingError{
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
        throw new ConditionalNoMatch();
        //System.out.println("DID NOT MATCH IN CONDITIONAL; THROW ERROR");
        //return null;
    }

    /**
     * Reduces a given string to a simple expression; can perform the singleton
     * @param expr
     * @throws FormattingError 
     */
    private static String expression(String expr) throws FormattingError{
        // add boolean expressions ((true|false) and (true|false)); ((true|false) or (true|false));
        //(not (true|false));

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
        }else if(methodMatcher.matches()){
            if(methods.contains(methodMatcher.group(1))){
                return getMethod(expr);
            }
            //TODO:: throw an unknown method error
            return null;
        }
        else if(singletonPatternMatcher.matches()){
            return String.format("%s",singletonPatternMatcher.group(1));
        }
        return null;
    }

    /**
     * Method that takes in a string, evaluates the string through pattern matching and
     * returns what the Java class the expression represents.
     * @param expression
     * @return
     */
    private static String getClass(String expression){
        System.out.println(String.format("CLASS expr:: %s",expression));

        Pattern doublePattern = Pattern.compile("-?(\\d+\\.\\d*)");
        // for expressions that have 2 numbers(either float or int)
        Pattern mathExpression = Pattern.compile("-?(\\w*|\\d+\\.?\\d*) (add|sub|mult|div|mod) -?(\\d+\\.?\\d*|\\S+$)");
        Matcher mathMatcher = mathExpression.matcher(expression.trim());
        // for boolean expressions
        Pattern booleanExpression = Pattern.compile("(true|false)");
        Matcher booleanMatcher = booleanExpression.matcher(expression.trim());
        // for single digits
        Pattern singletonNumExpression = Pattern.compile("(\\d+\\.?\\d*)");
        Matcher singletonNumMatcher = singletonNumExpression.matcher(expression.trim());

        Pattern methodExpression = Pattern.compile("(.*)\\((.*)\\)");
        Matcher methodExpressionMatcher = methodExpression.matcher(expression.trim());


        //TODO: can probably adapt this pretty easily for strings

        if(mathMatcher.matches()) {
            // here we are.
            // we need to check to see if x or y in x mod y is a variable
            System.out.println(String.format("A: %s\nB:%s",mathMatcher.group(1).trim(),mathMatcher.group(3).trim()));
            System.out.println(variableTypes.containsKey(mathMatcher.group(1)));
            String var1 = mathMatcher.group(1);
            String var2 = mathMatcher.group(3);
            if(variableTypes.containsKey(var1) && variableTypes.containsKey(var2)){
                boolean classVal1 = variableTypes.get(var1).equals("double");
                boolean classVal2 = variableTypes.get(var2).equals("double");
                if(classVal1 || classVal2){
                    return "double";
                }
                return "int";
            }else if(variableTypes.containsKey(var1) || variableTypes.containsKey(var2)){
                String alreadyDefinedVar = "";
                if(variableTypes.containsKey(var1)){
                    alreadyDefinedVar = variableTypes.get(var1);
                    boolean class1 = alreadyDefinedVar.equals("double");
                    boolean class2 = doublePattern.matcher(mathMatcher.group(3)).matches();
                    if (class1 || class2){
                        return "double";
                    }else{
                        return "int";
                    }
                }else{
                    alreadyDefinedVar = variableTypes.get(var2);
                    boolean class1 = alreadyDefinedVar.equals("double");
                    boolean class2 = doublePattern.matcher(mathMatcher.group(1)).matches();
                    if (class1 || class2){
                        return "double";
                    }else{
                        return "int";
                    }
                }
            }
            boolean a = doublePattern.matcher(mathMatcher.group(1)).matches();
            boolean b = doublePattern.matcher(mathMatcher.group(3)).matches();
            if (a || b) {
                return "double";
            }
            return "int";
        }else if(booleanMatcher.matches()){
            return "boolean";
        }else if(singletonNumMatcher.matches()){
            if (doublePattern.matcher(singletonNumMatcher.group(1)).matches()){
                return "double";
            }
            return "int";
        }else if(methodExpressionMatcher.matches()){
            // Only handling ints at the moment; might need to change this
            // maybe do a .contains("cmd") to determine a quick result
            return "int";
        }
        System.out.println("FAIL:"+expression);
        return null;
    }


    /**
     * Method that is meta in that it gets methods.
     * @param expression
     * @return
     * @throws FormattingError 
     */
    private static String getMethod(String expression) throws FormattingError{
        Pattern printPattern = Pattern.compile("output\\((\"(.*)\"|\\w+)\\)");
        Matcher printPatternMatcher = printPattern.matcher(expression);

        Pattern print2Pattern = Pattern.compile("outputs\\((\"(.*)\"|\\w+)\\)");
        Matcher print2PatternMatcher = print2Pattern.matcher(expression);
        
        Pattern cmdPattern = Pattern.compile("cmd\\((\\d)\\)");
        Matcher cmdPatternMatcher = cmdPattern.matcher(expression);
        System.out.println("CHECKING IFS");
        if (printPatternMatcher.matches()) {
        	return "System.out.print(" + printPatternMatcher.group(1) + ")";
        } else if (print2PatternMatcher.matches()) {
        	//matches to "output(" + printPatternMatcher.group(2) + ")\n";
        	return "System.out.println(" + print2PatternMatcher.group(1) + ")";
        } else if (cmdPatternMatcher.matches()) {
        	return "Integer.parseInt(args[" + cmdPatternMatcher.group(1) + "])";
        } else {
            System.out.println("ERROR WITH::"+expression);
        	throw new FormattingError();
        }
    }

    private static String fromLoopStatement(String expression) throws UndefinedVariable, FormattingError{
        expression = expression.trim();

        Pattern fromPattern = Pattern.compile("from (.*) to (.*) (increment|decrement) by (\\d+)");
        Matcher fromPatternMatcher = fromPattern.matcher(expression);

        Pattern variablePredefinedExtractor = Pattern.compile("^\\w+(?=\\s+=) = (.*)");

        Pattern variableNewExtractor = Pattern.compile("(int|double) (.*) = (.*)");

        Pattern allDigits = Pattern.compile("[0-9]+");
        String variable = "NULL";
        if(fromPatternMatcher.matches()){
            String varAssignment = addVariable(fromPatternMatcher.group(1));
            String toVal = fromPatternMatcher.group(2);
            // checks to see if the toVal is an int; if not an int: check if variable has been defined before
            if(!allDigits.matcher(toVal).matches()){
                if(!variables.contains(toVal)){
                    //TODO: throw undefined variable error
                	throw new UndefinedVariable();
                    //System.out.println("THROW ERROR HERE: to value in from loop is a " +
                     //       "variable that has not been defined "+toVal);
                    //return null;
                }
            }
            String plusOrMinus = fromPatternMatcher.group(3);
            String byNum = fromPatternMatcher.group(4);
            String sign = "SIGN";
            String comp = "COMP";
            if(plusOrMinus.equals("increment")){
                sign = "+";
                comp = "<";
            }else if(plusOrMinus.equals("decrement")){
                sign = "-";
                comp = ">";
            }
            //if the variable assignment returns something predefined: i.e. i = 1
            if(variablePredefinedExtractor.matcher(varAssignment).matches()){
                Matcher m = Pattern.compile("^\\w+(?=\\s+=)").matcher(varAssignment);
                m.find();
                variable = m.group(0);
            }else if(variableNewExtractor.matcher(varAssignment).matches()){
                Matcher m = variableNewExtractor.matcher(varAssignment);
                m.find();
                variable = m.group(2);
            }
            return String.format("for(%s; %s %s %s; %s %s= %s){\n",
                    varAssignment,variable,comp,toVal,variable,sign,byNum);

        }
        return null;
    }
    

}
