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
    // list of keywords that CANNOT be used as variables or anything else
    static List<String> keywords = Arrays.asList("var","add","mult","div","mod","true","false","or","not",
                                                "less than","less than or equal to","greater than","greater than or equal to",
                                                "equal to","not equal to","if","then","or","or if","end if","end from",
                                                "to","output","outputs","text","cla","array");
    // list of the variables that have been used
    static ArrayList<String> variables = new ArrayList<String>();
    // map of variables as the key and the values as the class the variable represents
    static HashMap<String,String> variableTypes = new HashMap<>();
    // methods that are used; also thought of as a keyword
    static List<String> methods = Arrays.asList("cmd","or","not","output","outputs","and","array"); // "built-in" methods of our language

    public static void main(String[] args) throws IOException, FormattingError {
        String className = args[0].substring(0,args[0].length()-4);
        f = new FileWriter(String.format("%s.java",className)); // this writes to a Java file that matches the passed in .txt file
    	f.write(String.format("public class %s {\n",className));
    	f.write("\tpublic static void main(String[] args){\n");

        // Pattern for variable assignment
        Pattern variableAssignmentPattern = Pattern.compile("var (.+)");
        // Pattern that matches with an already existing variable name
        Pattern existingVariableAssignmentPattern = Pattern.compile("^\\w+(?=\\s+=) = (.*)");
        // Pattern to match with methods; i.e. method()
        Pattern methodPattern = Pattern.compile("(.*)\\((.*)\\)");
        // Pattern for the in-line comments
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
            // for an inline comment
            if (line.startsWith("*")){
                f.write("//");
                f.write(line +"\n");
                continue;
            }
            // matcher for inline comments
            if(postInLineComment.matches()){
                line = postInLineComment.group(1).trim();
            }
            // if the line is a variable assignment; both new and old
            if (newVariableAssignment.matches() || oldVariableAssignment.matches()){
                f.write(addVariable(line,lineNo)+";\n");
            // conditional statement for the if statemtents
            }else if(line.trim().startsWith("if") || line.trim().startsWith("or if")){
                boolean startsWithOrIf = line.trim().startsWith("or if");
                if (!startsWithOrIf){
                    openIfs+=1; // keeps track of the open if statements; this will be compared to
                    // another value at the end of compiling; if these values do not match an error is
                    // thrown
                }
                f.write(ifStatement(line,startsWithOrIf,lineNo)+"\n");
            }else if(line.trim().equals("or")){
                f.write("\n}else{\n"); // or => else in Java
            }
            // closes the if statement with a bracket
            else if(line.trim().equals("end if") || line.trim().equals("end from") || line.trim().equals("end for each")){
                f.write("}\n");
                if (line.trim().equals("end if")){
                    openIfs -= 1; // compared value with the 'openIfs' statement at the end of reading the file
                }else{
                    openFroms -= 1; //compared to the openFroms to be compared at end of reading file
                }
            // start of a from block
            }else if(line.trim().startsWith("from")){
                f.write(fromLoopStatement(line,lineNo));
                openFroms += 1; // compare this with 0 at the end of reading lines
            // break out of a loop
            }else if(line.equals("stop loop now")){
                f.write("break;");
            // start of a for each loop; used with arrays
            }else if(line.trim().startsWith("for each")){
                f.write(forEachLoopStatement(line,lineNo));
                openFroms += 1;
            }
            // matches with a method that will be called
            else if(methodMatcher.matches()){
                f.write(getMethod(line,lineNo) + ";\n");
            // begins a comment header; will trigger a commentBlock flag, which will add a "*" to
            // the beginning of every write if this flag is false
            }else if(line.equals("start comment")){
                f.write("/*\n");
                commentBlock = true;
            // end of a comment block; switch the flag to stop writing "*" at the beginning
            // of every line
            }else if(line.startsWith("end comment")){
                f.write("*/\n");
                commentBlock = false;
            }
            lineNo++; // increment the line count
        	} catch (Exception e) {
        		System.out.println(e.toString());
        		return;
        	}
        }
        // comparative statement that will throw an error if the values are not 0;
        // values that are not zero means that a if or a loop statement was not correctly formatted;
        // most likely because of a lack of end statement
        if (openIfs > 0 || openFroms > 0){
            throw new FormattingError(lineNo);
        // closing brackets for a successful compiled file
        }else{
            f.write("\n}\n");
            f.write("}");
            f.close();
        }
        // SUCCESS MESSAGE
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
    private static String addVariable(String line,int lineNo) throws FormattingError, DivByZero, VariableAlreadyDefined, UndefinedVariable, MethodNotFound, UseOfKeyword {
        String retVal = "";
        // pattern for if a new variable is being created
        Pattern newVarPattern = Pattern.compile("var (.*) = (.*)");
        Matcher newVarMatcher = newVarPattern.matcher(line);
        // pattern for an existing variable to be signed to new value
        Pattern varAssignmentPattern = Pattern.compile("(.*) = (.*)");
        Matcher varAssignmentMatcher = varAssignmentPattern.matcher(line);

        if(newVarMatcher.matches()){
            String variable = newVarMatcher.group(1);// the variable name
            if(variables.contains(variable)){
                throw new VariableAlreadyDefined(lineNo); // variable has already been defined
            }
            if(keywords.contains(variable)){
                throw new UseOfKeyword(variable, lineNo); // variable is a protected keyword
            }
            String rightExpression = expression(newVarMatcher.group(2),lineNo);
            String exprClass = getClass(newVarMatcher.group(2));
            retVal = String.format("%s %s = %s",exprClass,variable,rightExpression);
            variables.add(variable);
            variableTypes.put(variable,exprClass);
        }else if(varAssignmentMatcher.matches()){
            String variable = varAssignmentMatcher.group(1);
            if(!variables.contains(variable)){
                throw new UndefinedVariable(lineNo); // variable was NOT defined; thus cannot be redefined
            }
            String rightExpression = expression(varAssignmentMatcher.group(2),lineNo);
            retVal = String.format("%s = %s",variable,rightExpression); // return value
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
        String retVal = ""; // value to add to
        // is it an "or if" statement?
        if (orIf){
            retVal += "\n}else if (";
        }else{
            retVal += "if(";
        }
        // this pattern is used to grab the variable/expression used in the if statement
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
        // ADDITION
        Pattern addPattern = Pattern.compile("(.*) add (.*)");
        Matcher addPatternMatcher = addPattern.matcher(expr);
        // SUBTRACTION
        Pattern subPattern = Pattern.compile("(.*) sub (.*)");
        Matcher subPatternMatcher = subPattern.matcher(expr);
        // MULTIPLICATION
        Pattern multPattern = Pattern.compile("(.*) mult (.*)");
        Matcher multPatternMatcher = multPattern.matcher(expr);
        // DIVIDE
        Pattern divPattern = Pattern.compile("(.*) div (.*)");
        Matcher divPatternMatcher = divPattern.matcher(expr);
        // MODULOUS
        Pattern modPattern = Pattern.compile("(.*) mod (.*)");
        Matcher modPatternMatcher = modPattern.matcher(expr);
        // METHOD MATCHER
        Pattern methodPattern = Pattern.compile("(.*)\\((.*)\\)");
        Matcher methodMatcher = methodPattern.matcher(expr);
        // STRING LITERAL
        Pattern stringLiteralPattern = Pattern.compile("\"(.*)\"");
        Matcher stringMatcher = stringLiteralPattern.matcher(expr);
        // SINGLETON VALUE; grabs whatever is left
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
                throw new DivByZero(lineNo); // ig the denominator is zero; handle here so Java doesnt cry
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
            System.out.println("FOUND A STRING::" + expr); // I think this is deadcode
        }
        return null;
    }

    /**
     * Method that takes in a string, evaluates the string through pattern matching and
     * returns what the Java class the expression represents.
     * @param expression
     * @return
     */
    private static String getClass(String expression) throws UndefinedVariable {
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
        // for methods
        Pattern methodExpression = Pattern.compile("(.*)\\((.*)\\)");
        Matcher methodExpressionMatcher = methodExpression.matcher(expression.trim());
        // for a string expression
        Pattern stringExpression = Pattern.compile("(\"(.*)\")");
        Matcher stringMatcher = stringExpression.matcher(expression.trim());
        // for array()
        Pattern arrayExpression = Pattern.compile("array\\((.*)\\)");
        Matcher arrayMatcher = arrayExpression.matcher(expression.trim());
        // for a Singleton expression, this gets the first word only
        Pattern singletonExpression = Pattern.compile("(\\S+)");
        Matcher singletonMatcher = singletonExpression.matcher(expression.trim());

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
            }else if(arrayMatcher.matches()){
                return "int[]";
            }
        }else if(stringMatcher.matches()){
            return "String";
        }else if(singletonMatcher.matches()){
            if (variables.contains(expression.trim())){
                return variableTypes.get(expression.trim());
            }
            throw new UndefinedVariable(0);
        }
        System.out.println("FAIL:"+expression); // should not get here, this is debug
        return null;
    }


    /**
     * Method that is meta in that it gets methods.
     * @param expression
     * @return
     * @throws FormattingError 
     */
    private static String getMethod(String expression,int lineNo) throws FormattingError{
        // OUTPUT
        Pattern printPattern = Pattern.compile("output\\((\"(.*)\"|\\w+)\\)");
        Matcher printPatternMatcher = printPattern.matcher(expression);
        // OUTPUTS
        Pattern print2Pattern = Pattern.compile("outputs\\((\"(.*)\"|\\w+)\\)");
        Matcher print2PatternMatcher = print2Pattern.matcher(expression);
        // COMMAND LINE
        Pattern cmdPattern = Pattern.compile("cmd\\((\\d)\\)");
        Matcher cmdPatternMatcher = cmdPattern.matcher(expression);
        // OR CONDITIONAL
        Pattern booleanOrPattern = Pattern.compile("or\\((\\w+),(\\w+)\\)");
        Matcher booleanOr = booleanOrPattern.matcher(expression);
        // AND CONDITIONAL
        Pattern booleanAndPattern = Pattern.compile("and\\((\\w+),(\\w+)\\)");
        Matcher booleanAnd = booleanAndPattern.matcher(expression);
        // NOT CONDITIONAL
        Pattern booleanNotPattern = Pattern.compile("not\\((\\w+)\\)");
        Matcher booleanNot = booleanNotPattern.matcher(expression);
        // ARRAY method
        Pattern arrayPattern = Pattern.compile("array\\((.*)\\)");
        Matcher arrayMatcher = arrayPattern.matcher(expression);
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
        }else if(arrayMatcher.matches()){
            return arrayBuilder(arrayMatcher.group(1));
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
    private static String fromLoopStatement(String expression,int lineNo) throws UndefinedVariable, FormattingError, DivByZero, VariableAlreadyDefined, MethodNotFound, UseOfKeyword {
        expression = expression.trim();
        // pattern for a FROM loop
        Pattern fromPattern = Pattern.compile("from (.*) to (.*) (increment|decrement) by (\\d+)");
        Matcher fromPatternMatcher = fromPattern.matcher(expression);
        // Extracts a predefined variable from the loop expression
        Pattern variablePredefinedExtractor = Pattern.compile("^\\w+(?=\\s+=) = (.*)");
        // extracts a newly defined variable from the loop expression
        Pattern variableNewExtractor = Pattern.compile("(int|double) (.*) = (.*)");
        // used to see if a string contains only numbers
        Pattern allDigits = Pattern.compile("[0-9]+");
        String variable = "NULL"; // begin; this should be changed
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

    /**
     * Method that creates the Java string version of a new int array.
     * ARRAYS CAN ONLY BE INTEGER ARRAYS!
     * @param contents
     * @return
     */
    private static String arrayBuilder(String contents){
        try {
            String retVal = "{";
            String[] splitS = contents.split(",");
            for (String n : splitS) {
                int check = Integer.parseInt(n.trim()); // if the element is not a number; this will error
                retVal += n.trim() + ",";
            }
            retVal = retVal.substring(0, retVal.length() - 1);
            retVal += "}";
            return retVal;
        }catch (NumberFormatException e){
            return null;
        }
    }

    /**
     * Method that creates the for each loop; this loop can only be used on predefined
     * arrays and uses a variable that has not yet been defined in the program before
     *
     * @param line
     * @param lineNo
     * @return
     * @throws MethodNotFound
     * @throws FormattingError
     * @throws UndefinedVariable
     * @throws VariableAlreadyDefined
     * @throws DivByZero
     * @throws UseOfKeyword
     */
    private static String forEachLoopStatement(String line, int lineNo) throws MethodNotFound, FormattingError, UndefinedVariable, VariableAlreadyDefined, DivByZero, UseOfKeyword {
        // Matches for the for each loop; used to determine correct formatting
        Pattern statementPattern = Pattern.compile("for each (.*) in (.*) do");
        Matcher statementMatcher = statementPattern.matcher(line);

        if(!statementMatcher.matches()){
            throw new FormattingError(lineNo);
        }
        String variable = statementMatcher.group(1);
        String arrayVar = statementMatcher.group(2);
        if(!variables.contains(arrayVar) || variables.contains(variable)){
            throw new UndefinedVariable(lineNo);
        }
        String retVal = String.format("for(int %s : %s){",variable,arrayVar);
        return retVal;
    }
    

}
