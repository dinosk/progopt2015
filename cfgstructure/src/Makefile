all: compile

compile:
	javac -cp ../../simplec/dist/Compiler.jar:. petter/cfg/OptimizerAnalysis.java

ant:
	cd ../../simplec; ant

execute:
	java -cp ../../simplec/dist/Compiler.jar:. petter/cfg/OptimizerAnalysis $(testfile)

clean:
	rm -rf *.class *~