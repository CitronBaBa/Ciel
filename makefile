default: UniverseWindow
jfx = --module-path FX2/lib --add-modules=javafx.controls --add-modules=javafx.fxml --add-modules=richtextfx

UniverseWindow:
	javac $@.java EtoileControl.java Coordination.java TextRealm.java CielControl.java $(jfx)
	java $(jfx) UniverseWindow
