.PHONY: run

all: compile

run: compile
	java -cp '.:postgresql-42.3.6.jar' Main

compile: clear
	javac -cp '.:postgresql-42.3.6.jar' Main.java

clear:
	rm -rf *.class