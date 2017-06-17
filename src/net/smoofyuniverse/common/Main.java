/*******************************************************************************
 * Copyright (C) 2017 Hugo Dupanloup (Yeregorix)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package net.smoofyuniverse.common;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Main {
	public static final String REQUIRED_VERSION = "1.8.0_101"; // 1.8.0_40 JavaFX Dialogs, 1.8.0_101 Let's Encrypt compatibility

	public static final String ERROR_MESSAGE = "La version de votre environnement d'éxecution (%s) n'est pas à jour.\n"
			+ "Pour fonctionner, l'application nécessite la version %s (ou plus) de Java.\n"
			+ "Vous pouvez la télécharger depuis le site d'Oracle:\n"
			+ "http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";
	
	public static final String FORMATTED_ERROR_MESSAGE = "<br>La version de votre environnement d'éxecution (%s) n'est pas à jour.</br>"
			+ "<br>Pour fonctionner, l'application nécessite la version %s (ou plus) de Java.</br>"
			+ "<br>Vous pouvez la télécharger depuis <a href=\"http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html\">le site d'Oracle</a>.</br>";

	public static void main(String[] args) throws Throwable {
		ensureRequiredVersion();
		launchApplication(getApplicationClass(), args);
	}

	public static void ensureRequiredVersion() {
		String version = getCurrentVersion();
		if (getVersionValue(version) < getVersionValue(REQUIRED_VERSION)) {
			if (!GraphicsEnvironment.isHeadless())
				JOptionPane.showMessageDialog(null, new MessageWithLink(String.format(FORMATTED_ERROR_MESSAGE, version, REQUIRED_VERSION)), "JRE invalide", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(String.format(ERROR_MESSAGE, version, REQUIRED_VERSION));
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
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(Main.class.getResourceAsStream("/application.main")))) {
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

	public static class MessageWithLink extends JEditorPane {
		private static final long serialVersionUID = 1L;

		public MessageWithLink(String htmlBody) {
			super("text/html", "<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");
			addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent ev) {
					if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						try {
							Desktop.getDesktop().browse(ev.getURL().toURI());
						} catch (Exception e) {}
					}
				}
			});
			setEditable(false);
			setBorder(null);
		}

		private static String getStyle() {
			JLabel label = new JLabel();
			Font font = label.getFont();
			Color color = label.getBackground();

			StringBuilder style = new StringBuilder();
			style.append("font-family:" + font.getFamily() + ";");
			style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
			style.append("font-size:" + font.getSize() + "pt;");
			style.append("background-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");");
			return style.toString();
		}
	}
}
