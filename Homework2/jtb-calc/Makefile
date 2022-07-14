all: compile

compile:
	java -jar ../jtb132di.jar calc.jj
	java -jar ../javacc5.jar calc-jtb.jj
	javac Main.java

clean:
	#rm -f *.class *~
	find . ! -name "visitor-control-flow-example" ! -name "calc.jj" ! -name "EvalVisitor.java" ! -name "EvalVisitor.java" ! -name "Main.java" ! -name "Makefile" ! -name "input" -type f -exec rm -f {} +
	find . ! -name "visitor-control-flow-example" ! -name "calc.jj" ! -name "EvalVisitor.java" ! -name "EvalVisitor.java" ! -name "Main.java" ! -name "Makefile" ! -name "input" ! -name "." ! -name ".." -type d -exec rm -r -f {} +
