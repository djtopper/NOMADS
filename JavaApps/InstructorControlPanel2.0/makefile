include ../makefile.conf

.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
	GroupDiscussPrompt.java \
	InstructorGroupDiscuss.java \
	CloudPrompt.java \
	CloudDisplay.java \
	PollPrompt.java \
	PollDisplay.java \
	UnityGroovePrompt.java\
	UnityGroovePanel.java\
	colorBox.java\
	SandPointerDisplay.java\
	SoundMosaicDisplay.java\
	InstructorControlPanel.java 

default: classes myliblink comlink jar

topliblink:
	rm -f ./nomads
	/bin/ln -s /var/www/NOMADS/javalib/nomads .

jar:
	jar cf InstructorControlPanel.jar *.class

myliblink:
	rm -f ./nomads
	/bin/ln -s /var/www/$(USER)/NOMADS/javalib/nomads .

comlink:
	rm -f ./com
	/bin/ln -s /var/www/$(USER)/NOMADS/javalib/com .

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
		rm -f ./nomads