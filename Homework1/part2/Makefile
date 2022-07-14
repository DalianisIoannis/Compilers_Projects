compile:
	jflex scanner.flex
	java -jar ./jar/java-cup-11b.jar -interface -parser Parser parser.cup
	javac -cp ./jar/java-cup-11b-runtime.jar *.java

execute:
	@java -cp ./jar/java-cup-11b-runtime.jar:. Driver

clean:
	rm -f *.class *~ Main.java Parser.java Scanner.java sym.java

all: clean ; make compile ; make execute
