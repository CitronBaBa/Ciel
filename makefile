default: UniverseWindow
jfx = --module-path FX/lib --add-modules=javafx.controls --add-modules=javafx.fxml

UniverseWindow:
	javac $@.java EtoileControl.java Coordination.java TextRealm.java CielControl.java Etoile.java $(jfx)
	java $(jfx) UniverseWindow 
