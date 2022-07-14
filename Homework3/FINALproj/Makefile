all: compile

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Main.java

clean:
	rm -f *.class *~
	find . ! -name "And.java" ! -name "Arrays.java" ! -name "Simple.java" ! -name "If.java" ! -name "LLVMVisitor.java" ! -name "README.txt" ! -name "MoreThan4.java" ! -name "Classes.java" ! -name "TreeVisitor.java" ! -name "LinearSearch.java" ! -name "QuickSort.java" ! -name "Factorial.java" ! -name "LinkedList.java" ! -name "BinaryTree.java" ! -name "Main.java" ! -name "CheckerVisitor.java" ! -name "Examples" ! -name "ST_MethodType.java" ! -name "ST_ClassType.java" ! -name "minijava.jj" ! -name "Main.java" ! -name "MyVisitor.java" ! -name "Makefile" -type f -exec rm -f {} +
	find . ! -name "And.java" ! -name "Arrays.java" ! -name "Simple.java" ! -name "If.java" ! -name "LLVMVisitor.java" ! -name "README.txt" ! -name "MoreThan4.java" ! -name "Classes.java" ! -name "TreeVisitor.java" ! -name "LinearSearch.java" ! -name "QuickSort.java" ! -name "Factorial.java" ! -name "LinkedList.java" ! -name "BinaryTree.java" ! -name "Main.java" ! -name "CheckerVisitor.java" ! -name "Examples" ! -name "ST_MethodType.java" ! -name "ST_ClassType.java" ! -name "minijava.jj" ! -name "Main.java" ! -name "MyVisitor.java" ! -name "Makefile" ! -name "." ! -name ".." -type d -exec rm -r -f {} +

run:
	java Main ./Examples/And.java /Examples/Classes.java ./Examples/If.java ./Examples/Simple.java /Examples/Arrays.java ./Examples/MoreThan4.java