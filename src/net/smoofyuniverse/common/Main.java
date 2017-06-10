package net.smoofyuniverse.common;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;

public class Main {
	public static final String REQUIRED_VERSION = "1.8.0_40";
	
    public static final String ERROR_MESSAGE = "La version de votre environnement d'éxecution (%s) n'est pas à jour.\n"
            + "Pour fonctionner, l'application nécessite la version %s (ou plus) de Java.\n"
            + "Vous pouvez la télécharger depuis le site d'Oracle:\n"
            + "http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";

	public static void main(String[] args) throws Throwable {
		ensureRequiredVersion();
		launchApplication(getApplicationClass(), args);
	}
	
	public static void ensureRequiredVersion() {
		String version = getCurrentVersion();
		if (getVersionValue(version) < getVersionValue(REQUIRED_VERSION)) {
			String error = String.format(ERROR_MESSAGE, version, REQUIRED_VERSION);

			if (!GraphicsEnvironment.isHeadless())
				JOptionPane.showMessageDialog(null, error, "JRE invalide", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(error);
		}
	}
	
	public static String getCurrentVersion() {
        return System.getProperty("java.version");
    }

    public static double getVersionValue(String version) {
		// Get rid of any dashes, such as those in early access versions which have "-ea" on the end of the version
		int i = version.indexOf('-');
		if (i != -1)
			version = version.substring(0, i);

		// Replace underscores with periods for easier String splitting
		version = version.replace('_', '.');

		// Split the version up into parts
		String[] versionParts = version.split("\\.");

		double value = 0;
		for (i = 0; i < versionParts.length; i++) {
			try {
				int part = Integer.valueOf(versionParts[i]);
				// The value of the part of the version is related to it's proximity to the beginning
				// Multiply by 3 to "pad" each of the parts a bit more so a higher value
				// of a less significant version part couldn't as easily outweight the
				// more significant version parts.
				value += part * Math.pow(10, versionParts.length - (i - 1) * 3);
			} catch (NumberFormatException e) {
				continue;
			}
		}
		return value;
	}

	public static Class<?> getApplicationClass() throws IOException, ClassNotFoundException {
    	try (BufferedReader in = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/application.main")))) {
    		return Main.class.getClassLoader().loadClass(in.readLine());
    	}
	}
	
	public static void launchApplication(Class<?> main, String[] args) throws Throwable {
		System.out.println("Launching application " + main.getName() + " ..");
		try {
			main.getMethod("main", String[].class).invoke(null, new Object[] { args });
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
