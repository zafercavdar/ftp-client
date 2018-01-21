all: CSftp.jar
CSftp.jar: CSftp.java
	javac CSftp.java
	jar cvfe CSftp.jar CSftp *.class

run: CSftp.jar
	java -jar CSftp.jar speedtest.tele2.net 21

clean:
	rm -f *.class
	rm -f CSftp.jar
