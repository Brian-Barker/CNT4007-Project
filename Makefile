JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        Client.java \
        Server.java

SAMPLE_CLASSES = \
        Sample_Client.java \
        Sample_Server.java

default: classes

classes: $(CLASSES:.java=.class)

sample: $(SAMPLE_CLASSES:.java=.class)

clean:
	$(RM) *.class
