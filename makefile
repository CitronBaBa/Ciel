default: CielWindow
jfx = --module-path FX/lib --add-modules=javafx.controls --add-modules=javafx.fxml

CielWindow: 
	javac $@.java EtoileControl.java Coordination.java $(jfx) 
	java $(jfx) CielWindow
