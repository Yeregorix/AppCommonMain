/*
 * Copyright (c) 2017-2019 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.common;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

public class Main {
	public static String ERROR_MESSAGE_TITLE, ERROR_MESSAGE, FORMATTED_ERROR_MESSAGE;

	public static void main(String[] args) throws Throwable {
		Info app = getApplicationInfo();
		requireVersion(app.javaVersion);
		launchApplication(app.main, args);
	}

	public static Info getApplicationInfo() throws IOException, ClassNotFoundException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/application.main")));
			Class<?> main = Main.class.getClassLoader().loadClass(in.readLine());
			String javaVersion = in.readLine();
			return new Info(main, javaVersion == null ? "1.8.0_40" : javaVersion);
		} finally {
			if (in != null)
				in.close();
		}
	}

	public static String getCurrentVersion() {
		return System.getProperty("java.version");
	}

	// https://github.com/SpongePowered/SpongeCommon/blob/stable-7/src/java6/java/org/spongepowered/launch/JavaVersionCheckUtils.java
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
				int part = Integer.parseInt(versionParts[i]);
				// The value of the part of the version is related to it's proximity to the beginning
				// Multiply by 3 to "pad" each of the parts a bit more so a higher value
				// of a less significant version part couldn't as easily outweight the
				// more significant version parts.
				value += part * Math.pow(10, versionParts.length - (i - 1) * 3);
			} catch (NumberFormatException ignored) {
			}
		}
		return value;
	}

	public static void loadLanguage() {
		if ("fr".equals(System.getProperty("user.language"))) {
			ERROR_MESSAGE_TITLE = "JRE invalide";

			ERROR_MESSAGE = "La version de votre environnement d'éxecution (%s) n'est pas à jour.\n"
					+ "Pour fonctionner, l'application nécessite la version %s (ou plus) de Java.\n"
					+ "Vous pouvez la télécharger depuis le site d'Oracle:\n"
					+ "http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";

			FORMATTED_ERROR_MESSAGE = "<br>La version de votre environnement d'éxecution (%s) n'est pas à jour.</br>"
					+ "<br>Pour fonctionner, l'application nécessite la version %s (ou plus) de Java.</br>"
					+ "<br>Vous pouvez la télécharger depuis <a href=\"http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html\">le site d'Oracle</a>.</br>";
		} else {
			ERROR_MESSAGE_TITLE = "Invalid JRE";

			ERROR_MESSAGE = "The version of your runtime environment (%s) is out of date.\n"
					+ "To run, the application requires the version %s (or more) of Java.\n"
					+ "You can download it from the Oracle site:\n"
					+ "http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";

			FORMATTED_ERROR_MESSAGE = "<br>The version of your runtime environment (%s) is out of date.</br>"
					+ "<br>To run, the application requires the version %s (or more) of Java.</br>"
					+ "<br>You can download it from <a href=\"http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html\">the Oracle site</a>.</br>";
		}
	}

	public static void requireVersion(String requiredVersion) {
		String version = getCurrentVersion();
		if (getVersionValue(version) < getVersionValue(requiredVersion)) {
			loadLanguage();
			if (!GraphicsEnvironment.isHeadless())
				JOptionPane.showMessageDialog(null, createClickableMessage(String.format(FORMATTED_ERROR_MESSAGE, version, requiredVersion)), ERROR_MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(String.format(ERROR_MESSAGE, version, requiredVersion));
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

	public static JEditorPane createClickableMessage(String htmlBody) {
		JEditorPane pane = new JEditorPane();

		pane.setContentType("text/html");
		pane.setText("<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");

		pane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent ev) {
				if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(ev.getURL().toURI());
					} catch (Exception ignored) {
					}
				}
			}
		});

		pane.setEditable(false);
		pane.setBorder(null);
		return pane;
	}

	private static String getStyle() {
		JLabel label = new JLabel();
		Font font = label.getFont();
		Color color = label.getBackground();

		return "font-family:" + font.getFamily() + ";" +
				"font-weight:" + (font.isBold() ? "bold" : "normal") + ";" +
				"font-size:" + font.getSize() + "pt;" +
				"background-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");";
	}

	public static class Info {
		public final Class<?> main;
		public final String javaVersion;

		public Info(Class<?> main, String javaVersion) {
			this.main = main;
			this.javaVersion = javaVersion;
		}
	}
}
