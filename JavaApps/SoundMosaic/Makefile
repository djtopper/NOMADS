include ../makefile.conf

JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	SoundMosaic.java

default: classes myliblink

topliblink:
	rm -f ./nomads
	/bin/ln -s /var/www/NOMADS/javalib/nomads .

myliblink:
	rm -f ./nomads
	/bin/ln -s /var/www/$(USER)/NOMADS/javalib/nomads .

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
	rm -f ./nomads