package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Function function;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {

        try {
            Environment.Function func = scope.lookupFunction("main", 0);
            requireAssignable(Environment.Type.INTEGER, func.getReturnType());
        } catch (RuntimeException except) {
            throw new RuntimeException("Main function incorrect");
        }

        for (Ast.Global global: ast.getGlobals()) {
            visit(global);
        }
        for (Ast.Function function: ast.getFunctions()) {
            visit(function);
        }

        return null;
        //throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Global ast) {
        if (ast.getValue().isPresent()) {
            visit(ast.getValue().get());
            try {
                requireAssignable(Environment.getType(ast.getTypeName()),ast.getValue().get().getType());
            } catch (RuntimeException e) {
                throw new RuntimeException("requireAssignable failed");
            }
/**
            // TODO Check if value is of subtype of global's type
            if (ast.getVariable().getType() == Environment.Type.ANY) {
            } else if (Environment.create(ast.getValue().get()).getType() == ast.getVariable().getType()){
            } else if (ast.getVariable().getType() == Environment.Type.COMPARABLE) {
                Environment.PlcObject temp = Environment.create(ast.getValue().get());
                if (!((temp.getType() == Environment.Type.INTEGER) || (temp.getType() == Environment.Type.DECIMAL) || (temp.getType() == Environment.Type.CHARACTER) ||(temp.getType() == Environment.Type.STRING))) {
                    throw new RuntimeException("variable mismatch");
                }
            } else {
                //if (Environment.create(ast.getValue().get()).getType() != ast.getVariable().getType()) {
                    throw new RuntimeException("variable mismatch");
                //}
            }**/
        }

       // scope.defineVariable(ast.getName(), ast.getMutable(), Environment.NIL);
        Environment.Variable var = scope.defineVariable(ast.getName(), ast.getName(),Environment.getType(ast.getTypeName()), ast.getMutable(), Environment.NIL);
       // ast.setVariable(new Environment.Variable(ast.getName(), ast.getMutable(), Environment.NIL));
        ast.setVariable(var);
        return null;

       // throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Function ast) {

        List<Environment.Type> parameterTypes = new ArrayList<>();
        for (int i = 0; i < ast.getParameterTypeNames().size(); ++i) {
            parameterTypes.add(Environment.getType(ast.getParameterTypeNames().get(i)));
        }
        Environment.Type returnType = ast.getReturnTypeName().isPresent() ? Environment.getType(ast.getReturnTypeName().get()) : Environment.Type.NIL;
        Environment.Function func = scope.defineFunction(ast.getName(), ast.getName(), parameterTypes, returnType, args-> Environment.NIL);
        ast.setFunction(func);

        // need to save parent scope in variable to restore after? or no
        Scope newScope = new Scope(scope);
        try {
            //Scope newScope = new Scope(scope);
            for (Ast.Statement stmt : ast.getStatements()) {
                visit(stmt);
            }
            //ast.getStatements().forEach(this::visit);
            //scope = newScope.getParent();
        } finally {
            scope = newScope.getParent();
            return null;
        }

        //return null;

        //throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        if(ast.getExpression().getClass() != Ast.Expression.Function.class){
            throw new RuntimeException("The expression is not an Ast.Expression.Function");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        if(!ast.getTypeName().isPresent() && !ast.getValue().isPresent()) {
            throw new RuntimeException("Value not present");
        }

        try {
            if(ast.getTypeName().isPresent()) {
                if (ast.getValue().isPresent()) {
                    visit(ast.getValue().get());
                    Environment.Type type = Environment.getType(ast.getTypeName().get());
                    requireAssignable(type, ast.getValue().get().getType());
                }
                ast.setVariable(scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName().get()), true, Environment.NIL));
            }
            else{
                visit(ast.getValue().get());
                ast.setVariable(scope.defineVariable(ast.getName(), ast.getName(), ast.getValue().get().getType(), true, Environment.NIL));
            }
        } catch (RuntimeException except) {
            throw new RuntimeException(except);
        }

        return null;
    }


    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        if(ast.getReceiver().getClass() != Ast.Expression.Access.class){
            throw new RuntimeException("The receiver is not an access expression ");
        }

        try {
            visit(ast.getValue());
            visit(ast.getReceiver());
            requireAssignable(ast.getReceiver().getType(),ast.getValue().getType());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        visit(ast.getCondition());

        if (ast.getCondition().getType() != Environment.Type.BOOLEAN) {
            throw new RuntimeException("The condition is not of type Boolean");
        }

        if (ast.getThenStatements().isEmpty()) {
            throw new RuntimeException("The thenStatements list is empty");
        }

        try{
            scope = new Scope(scope);
            for (Ast.Statement stmt : ast.getThenStatements()){
                visit(stmt);
            }
        }
        catch (RuntimeException except) {
            throw new RuntimeException(except);
        }

        try{
            scope = new Scope(scope);
            for (Ast.Statement stmt : ast.getElseStatements()){
                visit(stmt);
            }
        }
        catch (RuntimeException except) {
            throw new RuntimeException(except);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        visit(ast.getCondition());
        boolean if_default = false;
        try {
            scope = new Scope(scope);
            for (Ast.Statement.Case cas : ast.getCases()) {
                if (cas.getValue().isPresent()) {
                    if_default = true;
                }
            }

            if (if_default)
            {
                for (Ast.Statement.Case cas : ast.getCases()) {
                    if (cas.getValue().isPresent()) {
                        throw new RuntimeException("");
                    }
                }
            }else
            {
                for (Ast.Statement.Case cas : ast.getCases()) {
                    if (!cas.getValue().isPresent()) {
                        scope = new Scope(scope);
                        visit(cas);
                    }
                }
            }
        } finally {
            scope = scope.getParent();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        for (Ast.Statement stmt : ast.getStatements()) {
            scope = new Scope(scope);
            visit(stmt);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        visit(ast.getCondition());

        if (ast.getCondition().getType() != Environment.Type.BOOLEAN) {
            throw new RuntimeException("The condition is not of type Boolean");
        }

        try{
            scope = new Scope(scope);
            for (Ast.Statement stmt : ast.getStatements()) {
                visit(stmt);
            }
        }
        finally{
            scope = scope.getParent();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        visit(ast.getValue());
        try {
            requireAssignable(function.getFunction().getReturnType(), ast.getValue().getType());

        }catch (RuntimeException except) {
            throw new RuntimeException("Cannot assign value to return type, mismatch");
        }
        return null;
        //throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {

        if (ast.getLiteral() == Environment.NIL)
            ast.setType(Environment.Type.NIL);
        else if (ast.getLiteral() instanceof Boolean)
            ast.setType(Environment.Type.BOOLEAN);
        else if (ast.getLiteral() instanceof Character)
            ast.setType(Environment.Type.CHARACTER);
        else if (ast.getLiteral() instanceof String) {
            ast.setType(Environment.Type.STRING);
        } else if (ast.getLiteral() instanceof BigInteger) {
            if (((BigInteger)ast.getLiteral()).bitCount() > 32) {
                throw new RuntimeException("too many bits!!!!!!");
            }
            ast.setType(Environment.Type.INTEGER);
        } else if (ast.getLiteral() instanceof BigDecimal) {
            if (((BigDecimal)ast.getLiteral()).doubleValue() == Double.NEGATIVE_INFINITY ||  ((BigDecimal)ast.getLiteral()).doubleValue() == Double.POSITIVE_INFINITY) {
                throw new RuntimeException("decimal too big :(");
            }
            ast.setType(Environment.Type.DECIMAL);
        }
        return null;

        //throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        visit(ast.getExpression());
        if(ast.getExpression() instanceof Ast.Expression.Binary) {
            ast.setType(ast.getExpression().getType());
            return null;
        }
        throw new RuntimeException("The contained expression is not a binary expression");
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        String op = ast.getOperator();
        visit(ast.getLeft());
        visit(ast.getRight());
        Environment.Type left = ast.getLeft().getType();
        Environment.Type right = ast.getRight().getType();

        if (op.equals("&&") || op.equals("||")) {
            requireAssignable(Environment.Type.BOOLEAN,left);
            requireAssignable(Environment.Type.BOOLEAN,right);
            ast.setType(Environment.Type.BOOLEAN);
        }

        else if (op.equals("<") || op.equals(">") ||op.equals("==") || op.equals("!=")) {
            requireAssignable(left, right);
            requireAssignable(Environment.Type.COMPARABLE, left);
            requireAssignable(Environment.Type.COMPARABLE, right);
            ast.setType(Environment.Type.BOOLEAN);
        }

        else if (op.equals("+")) {
            if (left == Environment.Type.STRING || right == Environment.Type.STRING) {
                ast.setType(Environment.Type.STRING);;
            }
            else if (left == Environment.Type.INTEGER ){
                if (right == Environment.Type.INTEGER){
                    ast.setType(Environment.Type.INTEGER);
                }
                else{
                    throw new RuntimeException("RHS is not the same type as LHS");
                }
            }
            else if (left == Environment.Type.DECIMAL ){
                if (right == Environment.Type.DECIMAL){
                    ast.setType(Environment.Type.DECIMAL);
                }
                else{
                    throw new RuntimeException("RHS is not the same type as LHS");
                }
            }
            else{
                throw new RuntimeException("Not supported '+' operation");
            }
        }

        else if (op.equals("*") || op.equals("-") || op.equals("/")) {
            if ((left == Environment.Type.INTEGER || left == Environment.Type.DECIMAL) && right == left) {
                ast.setType(left);
            }
            else{
                throw new RuntimeException("The LHS must be an Integer/Decimal, " +
                        "and the RHS needs to be the same as the LHS.");
            }
        }

        else if (op.equals("^")) {
            if (left == Environment.Type.INTEGER || left == Environment.Type.DECIMAL) {
                if (right == Environment.Type.INTEGER){
                    ast.setType(left);
                }
                else{
                    throw new RuntimeException("The RHS must be an Integer");
                }
            }
            else{
                throw new RuntimeException("The LHS must be either an Integer or a Decimal");
            }
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        if(ast.getOffset().isPresent()) {
            if (ast.getOffset().get().getType() != Environment.Type.INTEGER)
            {
                throw new RuntimeException("The offset type is not an Integer");
            }
            visit(ast.getOffset().get());
            ast.setVariable(ast.getOffset().get().getType().getGlobal(ast.getName()));
        }
        else{
            ast.setVariable(scope.lookupVariable(ast.getName()));
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        List<Ast.Expression> args = ast.getArguments();

        System.out.println(args);
        System.out.println(args.size());
        ast.setFunction(scope.lookupFunction(ast.getName(),args.size()));
        List<Environment.Type> argTypes = ast.getFunction().getParameterTypes();

        for(int i = 0; i < args.size(); i++){
            visit(ast.getArguments().get(i));
            Environment.Type parameter_type = Environment.getType(argTypes.get(i).getName());
            Environment.Type argument_type = args.get(i).getType();

            try{
                requireAssignable(parameter_type,argument_type);
            }catch (RuntimeException except) {
                throw new RuntimeException(except);
            }
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        Environment.Type list_type = ast.getType();

        for (Ast.Expression elem : ast.getValues()) {
            //TODO: For a value to be assignable,
            // it's type must be a subtype of the list's type as defined in Ast.Global.
            try{
                requireAssignable(list_type,elem.getType());
            }
            catch (RuntimeException except) {
                throw new RuntimeException(except);
            }
        }

        return null;
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        if (target == Environment.Type.ANY) {
        } else if (target == Environment.Type.COMPARABLE) {
            if (!(type == Environment.Type.INTEGER || type == Environment.Type.DECIMAL || type == Environment.Type.CHARACTER || type == Environment.Type.STRING)) {
                throw new RuntimeException("Cannot assign target to type provided");
            }
        } else if (target == type) {
        } else {
            throw new RuntimeException("Cannot assign");
        }
    }

}
