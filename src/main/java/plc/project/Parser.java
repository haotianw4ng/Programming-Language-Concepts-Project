package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    public int getIndex() {
        if (tokens.has(0)) return tokens.get(0).getIndex();
        else return tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length();
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Global parseGlobal() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code list} rule. This method should only be called if the
     * next token declares a list, aka {@code LIST}.
     */
    public Ast.Global parseList() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code mutable} rule. This method should only be called if the
     * next token declares a mutable global variable, aka {@code VAR}.
     */
    public Ast.Global parseMutable() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code immutable} rule. This method should only be called if the
     * next token declares an immutable global variable, aka {@code VAL}.
     */
    public Ast.Global parseImmutable() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Function parseFunction() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code block} rule. This method should only be called if the
     * preceding token indicates the opening a block.
     */
    public List<Ast.Statement> parseBlock() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Statement parseStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a switch statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a switch statement, aka
     * {@code SWITCH}.
     */
    public Ast.Statement.Switch parseSwitchStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a case or default statement block from the {@code switch} rule. 
     * This method should only be called if the next tokens start the case or 
     * default block of a switch statement, aka {@code CASE} or {@code DEFAULT}.
     */
    public Ast.Statement.Case parseCaseStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        return parseLogicalExpression();
       // throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expression parseLogicalExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseComparisonExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        Ast.Expression left = parseMultiplicativeExpression();
        while (match("+") || match("-")) {
            String op = tokens.get(-1).getLiteral();
            Ast.Expression right = parseMultiplicativeExpression();
            left = new Ast.Expression.Binary(op, left, right);
        }
        return left;
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
       Ast.Expression left = parsePrimaryExpression();
       while (match("*") || match("\\") || match("^")) {
           String op = tokens.get(-1).getLiteral();
           Ast.Expression right = parsePrimaryExpression();
           left = new Ast.Expression.Binary(op, left, right);
       }
       return left;
        // throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression() throws ParseException {
        if (peek("NIL")) {
            match("NIL");
            return new Ast.Expression.Literal(null);
        } else if (peek("TRUE")) {
            match("TRUE");
            Boolean result = new Boolean("True");
            return new Ast.Expression.Literal(result);
        } else if (peek("FALSE")) {
            match("FALSE");
            Boolean result = new Boolean("FALSE");
            return new Ast.Expression.Literal(result);
        } else if (peek(Token.Type.INTEGER)) {
            BigInteger result = new BigInteger(this.tokens.get(0).getLiteral());
            match(Token.Type.INTEGER);
            return new Ast.Expression.Literal(result);
        } else if (peek(Token.Type.DECIMAL)) {
            BigDecimal result = new BigDecimal(this.tokens.get(0).getLiteral());
            match(Token.Type.DECIMAL);
            return new Ast.Expression.Literal(result);
        } else if (peek(Token.Type.CHARACTER)) {
            String newToken = this.tokens.get(0).getLiteral().replace("'", "");
            //String newToken = this.tokens.get(0).getLiteral().substring(1,this.tokens.get(0).getLiteral().length()-1);
            //newToken = newToken.replace("\\n", "\n");
            Character result = null;
            switch (newToken) {
                case "\\n": result = '\n';
                    break;
                case "\\t": result = '\t';
                    break;
                case "\\r": result = '\r';
                    break;
                case "\\b": result = '\b';
                    break;
                case "\\'": result = '\'';
                    break;
                case "\\\"": result= '\"';
            }
            result = new Character(result);
            match(Token.Type.CHARACTER);
            return new Ast.Expression.Literal(result);
        } else if (peek(Token.Type.STRING)) {
            String newToken = this.tokens.get(0).getLiteral().replace("\\\"", "\"");
            newToken = newToken.replace("\\r", "\r");
            newToken = newToken.replace("\\t", "\t");
            newToken = newToken.replace("\\n", "\r");
            newToken = newToken.replace("\\b", "\b");
            newToken = newToken.replace("\\\"", "\"");
            newToken = newToken.replace("\\\'", "\'");
            newToken = newToken.replace("\\\\", "\\");
            match(Token.Type.STRING);
            return new Ast.Expression.Literal(newToken);
        } else if (peek("(")) {
            match("(");
            Ast.Expression expr = parseExpression();
            if (match(")")) {
                return new Ast.Expression.Group(expr);
            } else {
                throw new ParseException("Error: no closing parentheses", getIndex());
            }
        } else if (peek(Token.Type.IDENTIFIER)) {
            String token = this.tokens.get(0).getLiteral();
            List<Ast.Expression> exprList = new ArrayList<>();
            match(Token.Type.IDENTIFIER);
            if (!peek("(")) {
                if (!peek("[")) {
                    return new Ast.Expression.Access(null,token);
                }
                else {
                    match("[");
                    Ast.Expression expr = parseExpression();
                    if (!peek("]")) {
                        throw new ParseException("Missing closing bracket", getIndex());
                    } else {
                        match("]");
                        // what to do here
                        return new Ast.Expression.Access(Optional.of(expr),token);
                    }
                }
            }
            else if (peek("(",")")) {
                match("(", ")");
            } else {
                match("(");
                Ast.Expression expr = parseExpression();
                exprList.add(expr);
                while (match(",")) {
                    expr = parseExpression();
                    exprList.add(expr);
                }
                if (!peek(")")) throw new ParseException("Missing closing parentheses", getIndex());
                else match(")");
            }
            return new Ast.Expression.Function(token, exprList);
        } else throw new UnsupportedOperationException(); //TODO

    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++){
            if(!tokens.has(i)){
                return false;
            } else if(patterns[i] instanceof Token.Type){
                if(patterns[i] != tokens.get(i).getType()){
                    return false;
                }
            } else if(patterns[i] instanceof String){
                if(!patterns[i].equals(tokens.get(i).getLiteral())){
                    return false;
                }
            } else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if (peek){
            for(int i = 0; i < patterns.length;i++){
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
