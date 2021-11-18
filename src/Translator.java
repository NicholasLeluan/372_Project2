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
    static List<String> methods = Arrays.asList("cmd","or","not","output","outputs","and"); // "built-in" methods of our language

    public static void main(String[] args) throws IOException, FormattingError {
        String className = args[0].substring(0,args[0].length()-4);
        f = new FileWriter(String.format("%s.java",className)); // this writes to a Java file that matches the passed in .txt file
    	f.write(String.format("public class %s {\n",className));
    	f.write("\tpublic static void main(String[] args){\n");

        // Pattern for variable assignment
        Pattern variableAssignmentPattern = Pattern.compile("var (.+)");

        Pattern existingVariableAssignmentPattern = Pattern.compile("^\\w+(?=\\s+=) = (.*)");

        Pattern methodPattern = Pattern.compile("(.*)\\((.*)\\)");

        Pattern inLineCommentPost = Pattern.compile("(.*) [*] (.*)");


        File file = new File(args[0]);
        scanner = new Scanner(file);
        int lineNo = 1;
        int openIfs = 0; //used as flag to determine correct if formatting
        int openFroms = 0;
        boolean commentBlock = false;
        while (scanner.hasNext()){
        	try {
            String line = scanner.nextLine().trim();
            Matcher newVariableAssignment = variableAssignmentPattern.matcher(line);
            Matcher oldVariableAssignment = existingVariableAssignmentPattern.matcher(line);
            Matcher methodMatcher = methodPattern.matcher(line);
            Matcher postInLineComment = inLineCommentPost.matcher(line);
            // if the read in line is a variable assignment call the variableAssignment method
            if(commentBlock){
                f.write("* ");
            }
            if (line.startsWith("*")){
                f.write("//");
                f.write(line +"\n");
                continue;
            }
            if(postInLineComment.matches()){
                line = postInLineComment.group(1).trim();
            }
            if (newVariableAssignment.matches() || oldVariableAssignment.matches()){
                f.write(addVariable(line,lineNo)+";\n");
            }else if(line.trim().startsWith("if") || line.trim().startsWith("or if")){
                boolean startsWithOrIf = line.trim().startsWith("or if");
                if (!startsWithOrIf){
                    openIfs+=1;
                }
                f.write(ifStatement(line,startsWithOrIf,lineNo)+"\n");
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
                f.write(fromLoopStatement(line,lineNo));
                openFroms += 1;
            }else if(line.equals("stop loop now")){

                f.write("break;");
            }
            else if(methodMatcher.matches()){
                f.write(getMethod(line,lineNo) + ";\n");
            }else if(line.equals("start comment")){
                f.write("/*\n");
                commentBlock = true;
            }else if(line.startsWith("end comment")){
                f.write("*/\n");
                commentBlock = false;
            }
            //END
            lineNo++;
        	} catch (Exception e) {
        		System.out.println(e.toString());
        		return;
        		
        	}
        }
        if (openIfs > 0 || openFroms > 0){
            throw new FormattingError(lineNo);
        }else{
            f.write("\n}\n");
            f.write("}");
            f.close();
        }
        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        String successMessage = String.format(
                "| SUCCESS! File has been successfully translated! \n| Run ```javac %s.java``` to compile the file \n" +
                        "| Then use ```java %s``` along with appropriate \n| command line arguments to run program. ",
                className,className);
        System.out.println(successMessage);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    /**
     * This is called when a line is suspected (sus) of attempting to assing a variable
     * to some value
     * @param line
     * @throws FormattingError 
     * @throws IOException 
     */
    private static String addVariable(String line,int lineNo) throws FormattingError, DivByZero, VariableAlreadyDefined, UndefinedVariable, MethodNotFound {
        String retVal = "";

        Pattern newVarPattern = Pattern.compile("var (.*) = (.*)");
        Matcher newVarMatcher = newVarPattern.matcher(line);

        Pattern varAssignmentPattern = Pattern.compile("(.*) = (.*)");
        Matcher varAssignmentMatcher = varAssignmentPattern.matcher(line);

        if(newVarMatcher.matches()){
            String variable = newVarMatcher.group(1);// the variable name
            if(variables.contains(variable)){
                throw new VariableAlreadyDefined(lineNo);
            }
            String rightExpression = expression(newVarMatcher.group(2),lineNo);
            String exprClass = getClass(newVarMatcher.group(2));
            retVal = String.format("%s %s = %s",exprClass,variable,rightExpression);
            variables.add(variable);
            variableTypes.put(variable,exprClass);
        }else if(varAssignmentMatcher.matches()){
            String variable = varAssignmentMatcher.group(1);
            if(!variables.contains(variable)){
                throw new UndefinedVariable(lineNo);
            }
            String rightExpression = expression(varAssignmentMatcher.group(2),lineNo);
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
    private static String ifStatement(String line, boolean orIf,int lineNo) throws FormattingError, ConditionalNoMatch, DivByZero, MethodNotFound {
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
            retVal += conditionalStatement(formatCheck.group(2),lineNo);
        }else{
            throw new FormattingError(lineNo);
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
    private static String conditionalStatement(String line,int lineNo) throws ConditionalNoMatch, FormattingError, DivByZero, MethodNotFound {
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
        // Uses a boolean condition method
        Pattern methodExpression = Pattern.compile("(.*)\\((.*)\\)");
        Matcher methodExpressionMatcher = methodExpression.matcher(line.trim());

        if(matcherGTOE.matches()){
            return String.format("%s >= %s",
                    expression(matcherGTOE.group(1),lineNo),expression(matcherGTOE.group(2),lineNo));
        }else if(matcherGT.matches()){
            return String.format("%s > %s",
                    expression(matcherGT.group(1),lineNo),expression(matcherGT.group(2),lineNo));
        }else if(matcherLTOE.matches()){
            return String.format("%s <= %s",
                    expression(matcherLTOE.group(1),lineNo),expression(matcherLTOE.group(2),lineNo));
        }else if(matcherLT.matches()){
            return String.format("%s < %s",
                    expression(matcherLT.group(1),lineNo),expression(matcherLT.group(2),lineNo));
        }else if(matcherEql.matches()){
            return String.format("%s == %s",
                    expression(matcherEql.group(1),lineNo),expression(matcherEql.group(2),lineNo));
        }else if(matcherNotEql.matches()){
            return String.format("%s != %s",
                    expression(matcherNotEql.group(1),lineNo),expression(matcherNotEql.group(2),lineNo));
        }else if(methodExpressionMatcher.matches()){
            return  getMethod(line,lineNo);
        }
        throw new ConditionalNoMatch(lineNo);
    }

    /**
     * Reduces a given string to a simple expression; can perform the singleton
     * @param expr
     * @throws FormattingError 
     */
    private static String expression(String expr,int lineNo) throws
            FormattingError, DivByZero, MethodNotFound {
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

        Pattern stringLiteralPattern = Pattern.compile("\"(.*)\"");
        Matcher stringMatcher = stringLiteralPattern.matcher(expr);

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
            String denom = divPatternMatcher.group(2).trim();
            if(denom.equals("0")){
                throw new DivByZero(lineNo);
            }
            return String.format("%s / %s",divPatternMatcher.group(1),divPatternMatcher.group(2));
        }else if(modPatternMatcher.matches()){
            return String.format("%s %% %s",modPatternMatcher.group(1),modPatternMatcher.group(2));
        }else if(methodMatcher.matches()){
            if(methods.contains(methodMatcher.group(1))){
                return getMethod(expr,lineNo);
            }
            throw new MethodNotFound(lineNo,methodMatcher.group(1));
        }
        else if(singletonPatternMatcher.matches()){
            return String.format("%s",singletonPatternMatcher.group(1));
        }else if(stringMatcher.matches()){
            System.out.println("FOUND A STRING::" + expr);
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

        Pattern stringExpression = Pattern.compile("(\"(.*)\")");
        Matcher stringMatcher = stringExpression.matcher(expression.trim());


        //TODO: can probably adapt this pretty easily for strings

        if(mathMatcher.matches()) {
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
            if (variableTypes.containsKey(singletonNumMatcher.group(1))){
                return variableTypes.get(singletonNumMatcher.group(1));
            }
            if (doublePattern.matcher(singletonNumMatcher.group(1)).matches()){
                return "double";
            }
            return "int";
        }else if(methodExpressionMatcher.matches()){
            // Only handling ints at the moment; might need to change this
            // maybe do a .contains("cmd") to determine a quick result
            if(expression.contains("cmd")){
                return "int";
            }else if(expression.contains("not(") || expression.contains("or(") ||expression.contains("and(")){
                return "boolean";
            }
        }else if(stringMatcher.matches()){
            return "String";
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
    private static String getMethod(String expression,int lineNo) throws FormattingError{
        Pattern printPattern = Pattern.compile("output\\((\"(.*)\"|\\w+)\\)");
        Matcher printPatternMatcher = printPattern.matcher(expression);

        Pattern print2Pattern = Pattern.compile("outputs\\((\"(.*)\"|\\w+)\\)");
        Matcher print2PatternMatcher = print2Pattern.matcher(expression);
        
        Pattern cmdPattern = Pattern.compile("cmd\\((\\d)\\)");
        Matcher cmdPatternMatcher = cmdPattern.matcher(expression);

        Pattern booleanOrPattern = Pattern.compile("or\\((\\w+),(\\w+)\\)");
        Matcher booleanOr = booleanOrPattern.matcher(expression);

        Pattern booleanAndPattern = Pattern.compile("and\\((\\w+),(\\w+)\\)");
        Matcher booleanAnd = booleanAndPattern.matcher(expression);

        Pattern booleanNotPattern = Pattern.compile("not\\((\\w+)\\)");
        Matcher booleanNot = booleanNotPattern.matcher(expression);


        if (printPatternMatcher.matches()) {
        	return "System.out.print(" + printPatternMatcher.group(1) + ")";
        } else if (print2PatternMatcher.matches()) {
        	return "System.out.println(" + print2PatternMatcher.group(1) + ")";
        } else if (cmdPatternMatcher.matches()) {
        	return "Integer.parseInt(args[" + cmdPatternMatcher.group(1) + "])";
        }else if(booleanOr.matches()){
            return String.format("%s || %s",booleanOr.group(1),booleanOr.group(2));
        }else if(booleanAnd.matches()){
            return String.format("%s && %s",booleanAnd.group(1),booleanAnd.group(2));
        }else if(booleanNot.matches()){
            return String.format("!%s",booleanNot.group(1));
        }
        else {
        	throw new FormattingError(lineNo);
        }
    }

    /**
     * Method that builds and returns the beginning statement of a for loop in Java
     * @param expression
     * @return
     * @throws UndefinedVariable
     * @throws FormattingError
     */
    private static String fromLoopStatement(String expression,int lineNo) throws UndefinedVariable, FormattingError, DivByZero, VariableAlreadyDefined, MethodNotFound {
        expression = expression.trim();

        Pattern fromPattern = Pattern.compile("from (.*) to (.*) (increment|decrement) by (\\d+)");
        Matcher fromPatternMatcher = fromPattern.matcher(expression);

        Pattern variablePredefinedExtractor = Pattern.compile("^\\w+(?=\\s+=) = (.*)");

        Pattern variableNewExtractor = Pattern.compile("(int|double) (.*) = (.*)");

        Pattern allDigits = Pattern.compile("[0-9]+");
        String variable = "NULL";
        if(fromPatternMatcher.matches()){
            String varAssignment = addVariable(fromPatternMatcher.group(1),lineNo);
            String toVal = fromPatternMatcher.group(2);
            // checks to see if the toVal is an int; if not an int: check if variable has been defined before
            if(!allDigits.matcher(toVal).matches()){
                if(!variables.contains(toVal)){
                	throw new UndefinedVariable(lineNo);
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
