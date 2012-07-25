/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-1012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.core.ui.plugins.views.geocatalog;

import java.util.Vector;

import org.gdms.source.SourceManager;
import org.gvsig.remoteClient.wms.WMSClient;
import org.gvsig.remoteClient.wms.WMSLayer;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import org.orbisgis.core.sif.UIFactory;
import org.orbisgis.core.sif.UIPanel;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.message.ErrorMessages;
import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchFrame;
import org.orbisgis.core.ui.plugins.views.geocatalog.newSourceWizards.wms.LayerConfigurationPanel;
import org.orbisgis.core.ui.plugins.views.geocatalog.newSourceWizards.wms.SRSPanel;
import org.orbisgis.core.ui.plugins.views.geocatalog.newSourceWizards.wms.WMSConnectionPanel;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;
import org.orbisgis.utils.I18N;

import org.gdms.data.stream.StreamSource;

public class WMSGeocatalogPlugIn extends AbstractPlugIn {

	public boolean execute(PlugInContext context) throws Exception {
		LayerConfigurationPanel layerConfiguration = new LayerConfigurationPanel();
                WMSConnectionPanel wmsConnection = new WMSConnectionPanel(layerConfiguration);
		SRSPanel srsPanel = new SRSPanel(wmsConnection);
		if (UIFactory.showDialog(new UIPanel[] { wmsConnection,
				layerConfiguration, srsPanel })) {
			WMSClient client = wmsConnection.getWMSClient();
			String validImageFormat = getFirstImageFormat(client.getFormats());
			if (validImageFormat == null) {
				ErrorMessages.error(I18N.getString("orbisgis.errorMessages.wms.CannotFindImageFormat"));
			} else {
				Object[] layers = layerConfiguration.getSelectedLayers();
				for (Object layer : layers) {
					String layerName = ((WMSLayer) layer).getName();
					//Old way to create stream source
                                        //WMSSource source = new WMSSource(client.getHost(),layerName, srsPanel.getSRS(), validImageFormat);
					//New way, we set SRS and image format later
                                        StreamSource source = new StreamSource(client.getHost(), client.getPort(), layerName, "wms", validImageFormat, srsPanel.getSRS());
                                        client.close();
                                        SourceManager sourceManager = Services.getService(DataManager.class).getSourceManager();
					String uniqueName = sourceManager.getUniqueName(layerName);
					sourceManager.register(uniqueName, source);
				}
			}

		}
		return true;
	}

	private String getFirstImageFormat(Vector<?> formats) {
		String[] preferredFormats = new String[] { "image/png", "image/jpeg",
				"image/gif", "image/tiff" };
		for (int i = 0; i < preferredFormats.length; i++) {
			if (formats.contains(preferredFormats[i])) {
				return preferredFormats[i];
			}
		}

		for (Object object : formats) {
			String format = object.toString();
			if (format.startsWith("image/")) {
				return format;
			}
		}

		return null;
	}

	@Override
	public void initialize(PlugInContext context) throws Exception {
		WorkbenchContext wbContext = context.getWorkbenchContext();
		WorkbenchFrame frame = wbContext.getWorkbench().getFrame()
				.getGeocatalog();
		context.getFeatureInstaller().addPopupMenuItem(
				frame,
				this,
				new String[] { Names.POPUP_GEOCATALOG_ADD,
						Names.POPUP_GEOCATALOG_Stream },
				Names.POPUP_GEOCATALOG_ADD, false, null,
				wbContext);
                //ajouter un popupMenu de WMS
                context.getFeatureInstaller().addPopupMenuItem(
				frame,
				this,
				new String[] { Names.POPUP_GEOCATALOG_Stream,
						Names.POPUP_GEOCATALOG_WMS },
				Names.POPUP_GEOCATALOG_Stream, false, OrbisGISIcon.GEOCATALOG_WMS,
				wbContext);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
