default: UniverseWindow
jfx = --module-path FX/lib --add-modules ALL-MODULE-PATH

UniverseWindow:
	javac $@.java EtoileControl.java Coordination.java TextRealm.java CielControl.java EtoileControl_EmptyShape.java CielJavaManager.java $(jfx)
	java $(jfx) UniverseWindow
