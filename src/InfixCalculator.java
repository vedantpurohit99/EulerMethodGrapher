import java.util.*;

// 1234567890
// ln log lg
// sin cos tg tan arcsin arccos arctg arctan
// ^ sqrt + - * /
// e pi
// ( ) [ ] { }
public class InfixCalculator{
    private ArrayList<String> postfix;
    String expression;
    
    public InfixCalculator(String s){
        expression = s;
        postfix = infixToPostfix(s);
    }

    boolean isOperator(String s){
        String[] operators = new String[]{"ln", "log", "lg",
                                          "sin", "cos", "tg", "tan", "arcsin", "arccos", "arctg", "arctan",
                                          "+", "-", "*", "/", "^", "sqrt"};
        for(String op : operators) if(s.equals(op)) return true;
        return false;
    }

    boolean isFunction(String s){
        String[] operators = new String[]{"ln", "log", "lg",
                                          "sin", "cos", "tg", "tan", "arcsin", "arccos", "arctg", "arctan",
                                          "sqrt"};
        for(String op : operators) if(s.equals(op)) return true;
        return false;
    }

    boolean isOperand(String s){
        if(s.equals("x") || s.equals("y") || s.equals("-x") || s.equals("-y")) return true;
        try{ Double.parseDouble(s); return true; }
        catch(Exception e){}
        return false;
    }

    boolean isNumber(String s){
        if(s.equals("e") || s.equals("pi")) return true;
        try{ Double.parseDouble(s); return true; }
        catch(Exception e){}
        return false;
    }

    boolean isVariable(String s){
        return s.equals("x") || s.equals("y") || s.equals("-x") || s.equals("-y");
    }

    boolean allVariables(String s){
        if(s.length() == 1) return false;
        for(int i=0; i<s.length(); i++) if(!(s.charAt(i) == 'x' || s.charAt(i) == 'y')) return false;
        return true;
    }

    int operatorPrecedence(String s){
        if(s.equals("(")) return 1;
        else if("+-".contains(s) || s.equals("neg")) return 2;
        else if("*/".contains(s)) return 3;
        else if(s.equals("^")) return 4;
        else return 5;
    }

    void resolveNegatives(ArrayList<String> tokenised){
        for(int i=0; i<tokenised.size(); i++){
            if(tokenised.get(i).equals("-x")){
                tokenised.remove(i);
                tokenised.add(i, "x");
                tokenised.add(i, "*");
                tokenised.add(i, "-1");
            } else if(tokenised.get(i).equals("-y")){
                tokenised.remove(i);
                tokenised.add(i, "y");
                tokenised.add(i, "*");
                tokenised.add(i, "-1");
            } 
        }
    }
    
    /**
     * <variable><varaible>: xy
     * <number><number>: epi
     * <number><variable>: 5x
     * <variable><number>: x5
     * <end-bracket><open-bracket>: )(
     * <number><function>: 5sin(x)
     * <variable><function>: xsin(x)
     * <end-bracket><function>: )sin(x)
     * <number><open-bracket>: 5(
     * <variable><open-bracket>: x(
     * <close-bracket><number>: )5
     * <close-bracket><variable>: )x
     */
    void padMultiplication(ArrayList<String> tokenised){
        boolean changesMade = false;
        System.out.println(tokenised);
        do{
            changesMade = false;
            for(int i=0; i<tokenised.size(); i++){
                try{
                    String
                        current = tokenised.get(i),
                        next = tokenised.get(i+1);
                    if(isVariable(current) && isVariable(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(isNumber(current) && isNumber(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(isNumber(current) && isVariable(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(isVariable(current) && isNumber(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(current.equals(")") && next.equals("(")){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(isNumber(current) && isFunction(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(isVariable(current) && isFunction(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(current.equals(")") && isFunction(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(isNumber(current) && next.equals("(")){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(isVariable(current) && next.equals("(")){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(current.equals(")") && isNumber(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(current.equals(")") && isVariable(next)){ tokenised.add(i+1, "*"); changesMade = true; }
                    else if(allVariables(current)){                        
                        tokenised.remove(i); changesMade = true;
                        for(int j=0; j<current.length()-1; j++){
                            tokenised.add(current.charAt(j)+""); tokenised.add("*");
                        } tokenised.add(current.charAt(current.length()-1)+"");
                    } else if(allVariables(next)){
                        tokenised.remove(i+1); changesMade = true;
                        for(int j=next.length()-1; j>0; j--){
                            tokenised.add(i+1, next.charAt(j)+""); tokenised.add(i+1, "*");
                        } tokenised.add(i+1, next.charAt(0)+"");
                    }
                } catch(Exception e){}
            } System.out.println(tokenised);
        } while(changesMade);
    }
    
    ArrayList<String> infixToPostfix(String infix){        
        String[] tokenised = infix.split("(?<=[^\\.a-zA-Z\\d])|(?=[^\\.a-zA-Z\\d])");        
        ArrayList<String> list = new ArrayList<String>();
        for(String s : tokenised) list.add(s);

        //padMultiplications(list);

        boolean changesMade = false;
        System.out.println(list);
        do{
            changesMade = false;
            for(int i=0; i<list.size(); i++){
                try{
                    if((i == 0 && list.get(i).equals("-") && isOperand(list.get(i+1))) ||
                      (list.get(i).equals("-") && isOperand(list.get(i+1)) && !isOperand(list.get(i-1)))){
                    /*if(list.get(i).equals("+") || list.get(i).equals("-")){
                        if(i == list.size()-1) continue;
                        if(!(list.get(i+1).equals("+") || list.get(i+1).equals("-"))) continue;*/

                        System.out.println(i+"a"+list);
                        if(i>0 && list.get(i-1).equals(")")) continue;
                        
                        changesMade = true;
                        /*int j=i, negatives=0;
                        for(; j<list.size() && (list.get(j).equals("+") || list.get(j).equals("-")); j++)
                        if(list.get(j).equals("-")) negatives++;*/
                        
                        list.remove(i);

                        if(list.get(i).equals("-x")) list.add(i, "x");
                        else if(list.get(i).equals("-y")) list.add(i, "y");
                        else if(list.get(i).equals("x") || list.get(i).equals("y")) list.add(i, "-"+list.get(i));
                        else list.add(i, -Double.parseDouble(list.get(i))+"");
                        
                        list.remove(i+1);
                    } else if((i == 0 && list.get(i).equals("+") && isOperand(list.get(i+1))) ||
                              (list.get(i).equals("+") && isOperand(list.get(i+1)) && !isOperand(list.get(i-1)))){
                        if(list.get(i-1).equals(")")) continue;
                        changesMade = true;                        
                        list.remove(i);
                    } else if((list.get(i).equals("-") || list.get(i).equals("+")) &&
                       (list.get(i+1).equals("-") || list.get(i+1).equals("+"))){
                        int j=i, negatives=0;
                        for(; j<list.size() && (list.get(j).equals("+") || list.get(j).equals("-")); j++)
                            if(list.get(j).equals("-")) negatives++;
                        j--; for(; j>=i; j--) list.remove(j);
                        list.add(i, negatives%2 == 0 ? "+" : "-");
                    } else{
                        ArrayList<String> temp = new ArrayList<String>();
                        String s = list.get(i); String current = "";
                        
                        if(s.length() == 1 || isOperand(s) || isFunction(s) || isNumber(s)) continue;

                        list.remove(i); changesMade = true;
                        for(int j=0; j<s.length(); j++){
                            if(s.charAt(j) == 'x' || s.charAt(j) == 'y') temp.add(s.charAt(j)+"");
                            else{
                                while(j<s.length() && s.charAt(j) != 'x' && s.charAt(j) != 'y'){
                                    current += s.charAt(j)+""; j++;
                                    if(current.equals("pi") || current.equals("e")) break;
                                    if(current.indexOf("e") != -1){
                                        temp.add(current.substring(0, current.indexOf("e")));
                                        temp.add(current.substring(current.indexOf("e")));
                                        current = ""; break;
                                    } if(current.indexOf("pi") != -1){
                                        temp.add(current.substring(0, current.indexOf("pi")));
                                        temp.add(current.substring(current.indexOf("pi")));
                                        current = ""; break;
                                    }
                                } j--;
                                if(!current.equals("")) temp.add(current);
                                current = "";
                            }
                        }

                        for(int j=temp.size()-1; j>=0; j--) list.add(i, temp.get(j));
                    }
                } catch(Exception e){e.printStackTrace();}
            }
            System.out.println("`"+list);
        } while(changesMade);
        
        padMultiplication(list);
        System.out.println(list);
        resolveNegatives(list);
        System.out.println(list);
        
        tokenised = new String[list.size()];
        for(int i=0; i<tokenised.length; i++) tokenised[i] = list.get(i);
        
        ArrayList<String> postfix = new ArrayList<String>();

        Stack<String> opStack = new Stack<String>();
        int basePriority = 0;
        for(String token : tokenised){
            //System.out.println("\n"+token);
            //System.out.println(postfix);
            //System.out.println(opStack);

            if(token.equals("e")) token = Math.E+"";
            else if(token.equals("pi")) token = Math.PI+"";

            if(isOperand(token)){
                //System.out.println("Operand");
                postfix.add(token);
            }
            else if(token.equals("(")){
                //System.out.println("Open bracket");
                opStack.push(token);
            }
            else if(token.equals(")")){
                //System.out.println("Close bracket");
                while(!opStack.isEmpty() && !opStack.peek().equals("(")) postfix.add(opStack.pop());
                if(!opStack.isEmpty()) opStack.pop();
            } else if(isOperator(token)){
                //System.out.println("operator");
                while(!opStack.isEmpty() && operatorPrecedence(opStack.peek()) >= operatorPrecedence(token))
                    postfix.add(opStack.pop());
                opStack.push(token);
            }
        }
        while(!opStack.isEmpty()) postfix.add(opStack.pop());
        return postfix;
    }

    double evaluatePostfix(ArrayList<String> postfix, double x, double y){
        //System.out.println("postfix = " + postfix);
        Stack<Double> operandStack = new Stack<Double>();

        for(String token : postfix){
            if(token.equals("x")) token = x+"";
            else if(token.equals("y")) token = y+"";
            else if(token.equals("-x")) token = (-x)+"";
            else if(token.equals("-y")) token = (-y)+"";

            //System.out.println("\n"+token);
            //System.out.println(operandStack);
            if(isOperand(token))operandStack.push(Double.parseDouble(token));
            else if(isOperator(token)){
                if("+-*/^".contains(token)){
                    double a = 0, b = 0;
                    if(!operandStack.isEmpty()) a = operandStack.pop();
                    if(!operandStack.isEmpty()) b = operandStack.pop();
                    operandStack.push(evaluateAtomic(token, a, b));
                    //operandStack.push(evaluateAtomic(token, operandStack.pop(), operandStack.pop()));
                }
                else operandStack.push(evaluateAtomic(token, operandStack.pop()));
            }
        } return operandStack.pop();
    }

    double evaluateAtomic(String operator, Double... operands){
        if(operator.equals("+")) return operands[1] + operands[0];
        else if(operator.equals("-")) return operands[1] - operands[0];
        else if(operator.equals("neg")) return 0 - operands[0];
        else if(operator.equals("*")) return operands[1] * operands[0];
        else if(operator.equals("/")) return operands[1] / operands[0];
        else if(operator.equals("^")) return Math.pow(operands[1], operands[0]);
        else if(operator.equals("sqrt")) return Math.sqrt(operands[0]);
        else if(operator.equals("ln")) return Math.log(operands[0]);
        else if(operator.equals("log") || operator.equals("lg")) return Math.log10(operands[0]);
        else if(operator.equals("sin")) return Math.sin(operands[0]);
        else if(operator.equals("csc")) return 1/Math.sin(operands[0]);
        else if(operator.equals("cos")) return Math.cos(operands[0]);
        else if(operator.equals("sec")) return 1/Math.cos(operands[0]);
        else if(operator.equals("tg") || operator.equals("tan")) return Math.tan(operands[0]);
        else if(operator.equals("cot")) return 1/Math.tan(operands[0]);
        else if(operator.equals("arcsin")) return Math.asin(operands[0]);
        else if(operator.equals("arccos")) return Math.acos(operands[0]);
        else if(operator.equals("arctg") || operator.equals("arctan")) return Math.atan(operands[0]);
        return 0;
    }

    public double calculate(double x, double y){
        /*String replaced = "";
        for(int i=0; i<expression.length(); i++){
          if(expression.charAt(i) == 'x') replaced += x+"";
          else if(expression.charAt(i) == 'y') replaced += y+"";
          else replaced += expression.charAt(i)+"";
        }
        return evaluatePostfix(infixToPostfix(replaced), x, y);*/
        return evaluatePostfix(postfix, x, y);
    }
    
    public static void main(String[] args){
        //System.out.println(true);
        String s = "(2*sin(x/2))/(sqrt(1.5-(sin(x/2))^2))";
        s = "-x/(1+x^2)^(-1/2)";
        s = "1/3x";
        //s = "sin(pi)";
        //System.out.println(evaluatePostfix(infixToPostfix(s), Math.PI, 0));
        //System.out.println(Math.sin(Math.PI));
        //InfixCalculator ic = new InfixCalculator(s);
        s = "-+--1";
        s = "-1-x-y";
        s = "x(-y)";
        s = "-y(2)77xsin(x)";
        s = "1-xtan(x)sin(x)-x";
        s = "----x^2";
        s = "--(--2)---+1*+--x^2-----+(((+++--(--y)+1+++-2))-3)";
        s = "ee(3.14)3.14pi2.71828etg(x)piesin(xpi)";
        //s = "x(-y)";
        //s = "-x/(1+xyxxxsin(x)^2)^(-1/2)";
        InfixCalculator c = new InfixCalculator(s);
        System.out.println(c.calculate(0, 0));
        //System.out.println(c.calculate(Math.PI, 1));
        //System.out.println(c.postfix);
        /*s = "y2.7xsin"; String current = "";
        for(int j=0; j<s.length(); j++){
            if(s.charAt(j) == 'x' || s.charAt(j) == 'y') System.out.print(s.charAt(j)+", ");
            else{
                while(j<s.length() && s.charAt(j) != 'x' && s.charAt(j) != 'y'){ current += s.charAt(j)+""; j++; } j--;
                System.out.print(current+", ");
                current = "";
            }
        }*/
    }
}
