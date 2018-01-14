all: CSftp.jar
CSftp.jar: CSftp.java
	javac -cp ".:./commons-net-3.6.jar" CSftp.java
	jar cvfe CSftp.jar CSftp *.class

run: CSftp.jar
	java -cp ".:./commons-net-3.6.jar" -jar CSftp.jar ftp.gnu.org 21

clean:
	rm -f *.class
	rm -f CSftp.jar
