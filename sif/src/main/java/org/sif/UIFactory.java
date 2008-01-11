/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at french IRSTV institute and is able
 * to manipulate and create vectorial and raster spatial information. OrbisGIS
 * is distributed under GPL 3 licence. It is produced  by the geomatic team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OrbisGIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-developers/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-users/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */
package org.sif;

import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.gdms.data.DataSourceFactory;

public class UIFactory {

	private static HashMap<String, String> inputs = new HashMap<String, String>();
	static File baseDir = new File(System.getProperty("user.home")
			+ File.separator + ".sif");
	private static URL defaultIconURL;
	private static String okMessage;
	static final DataSourceFactory dsf = new DataSourceFactory();

	public static SIFDialog getSimpleDialog(UIPanel panel) {
		return getSimpleDialog(panel, null);
	}

	public static SIFDialog getSimpleDialog(UIPanel panel, Window owner) {
		SIFDialog dlg = new SIFDialog(owner);
		SimplePanel simplePanel = new SimplePanel(dlg, panel);
		dlg.setComponent(simplePanel, inputs);
		return dlg;
	}

	public static DynamicUIPanel getDynamicUIPanel(String title, URL icon,
			String[] names) {
		return getDynamicUIPanel(null, title, icon, names, new int[0],
				new String[0], new String[0]);
	}

	public static DynamicUIPanel getDynamicUIPanel(String title, URL icon,
			String[] names, int[] types, String[] expressions,
			String[] errorMsgs) {
		return getDynamicUIPanel(null, title, icon, names, types, expressions,
				errorMsgs);
	}

	public static DynamicUIPanel getDynamicUIPanel(String id, String title,
			URL icon, String[] names) {
		return new DynamicUIPanel(id, title, icon, names, new int[0],
				new String[0], new String[0]);
	}

	public static DynamicUIPanel getDynamicUIPanel(String id, String title,
			URL icon, String[] names, int[] types, String[] expressions,
			String[] errorMsgs) {
		return new DynamicUIPanel(id, title, icon, names, types, expressions,
				errorMsgs);
	}

	public static SIFWizard getWizard(UIPanel[] panels) {
		return getWizard(panels, null);
	}

	private static SIFWizard getWizard(UIPanel[] panels, Frame owner) {
		SIFWizard dlg = new SIFWizard(owner);
		SimplePanel[] simplePanels = new SimplePanel[panels.length];
		for (int i = 0; i < simplePanels.length; i++) {
			simplePanels[i] = new SimplePanel(dlg, panels[i]);
		}
		dlg.setComponent(simplePanels, inputs);
		return dlg;
	}

	public static boolean showDialog(UIPanel[] panels) {
		AbstractOutsideFrame dlg;
		if (panels.length == 0) {
			throw new IllegalArgumentException(
					"At least a panel has to be specified");
		} else if (panels.length == 1) {
			dlg = getSimpleDialog(panels[0]);
		} else {
			dlg = getWizard(panels);
		}
		dlg.setModal(true);
		dlg.pack();
		dlg.setLocationRelativeTo(null);
		dlg.setVisible(true);

		return dlg.isAccepted();
	}

	public static boolean showDialog(UIPanel panel) {
		return showDialog(new UIPanel[] { panel });
	}

	public static void setInputFor(String id, String inputName) {
		inputs.put(id, inputName);
	}

	public static void setPersistencyDirectory(File baseDir) {
		if (!baseDir.exists()) {
			throw new IllegalArgumentException(baseDir + " doesn't exist");
		}
		UIFactory.baseDir = baseDir;
	}

	public static void setTempDirectory(File tempDir) {
		if (!tempDir.exists()) {
			throw new IllegalArgumentException(tempDir + " doesn't exist");
		}
		dsf.setTempDir(tempDir.getAbsolutePath());
	}

	public static URL getDefaultIcon() {
		return defaultIconURL;
	}

	public static void setDefaultIcon(URL iconURL) {
		UIFactory.defaultIconURL = iconURL;
	}

	public static String getDefaultOkMessage() {
		return okMessage;
	}

	public static void setDefaultOkMessage(String msg) {
		okMessage = msg;
	}
}
