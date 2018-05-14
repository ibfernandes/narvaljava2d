package engine.engine;

import java.io.File;

public class Settings {
	public static final String gameFolder =  System.getProperty("user.home") + File.separator + "Documents"  + File.separator + "Traveller" + File.separator;
	public static final String mapsFolder = gameFolder + "Maps" + File.separator;
	public static final String savesFolder = gameFolder + "Saves" + File.separator;
	
}
