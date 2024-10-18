A Java Compiler created for didactic purposes.
It is intended to work for a specific LL(1) grammar, it makes use of various classes such as "Lexer" and "Parser" to make a lexical-scan of the code before a syntax-analysis with the parser.
In the end it translates the file into Assembly-Level code for the Java Virtual Machine that can be translated into a .bytecode file thanks to the Jasmin assembler. 
