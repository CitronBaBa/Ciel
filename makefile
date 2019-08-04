default: UniverseWindow
jfx = --module-path FX/lib --add-modules ALL-MODULE-PATH
CielJavaManager:
	javac $@.java $(jfx)
	java $(jfx) $@
CielRobot:
	javac $@.java $(jfx)
	java $(jfx) $@
UniverseWindow:
	javac $@.java EtoileControl.java Coordination.java TextRealm.java CielControl.java EtoileControl_EmptyShape.java CielJavaManager.java $(jfx)
	java $(jfx) UniverseWindow
