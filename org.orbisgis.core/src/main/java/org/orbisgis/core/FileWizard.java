package org.orbisgis.core;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;

import org.sif.SQLUIPanel;
import org.sif.UIPanel;

public class FileWizard {
	public static final String FILE_CHOOSER_SIF_ID = "org.orbisgis.FileChooser";

	private FilePanel filePanel;

	public UIPanel[] getWizardPanels() {
		filePanel = new FilePanel();
		return new UIPanel[] { filePanel };
	}

	protected class FilePanel implements SQLUIPanel {

		private JFileChooser fileChooser;

		public Component getComponent() {
			fileChooser = new JFileChooser();
			fileChooser.setControlButtonsAreShown(false);
			fileChooser.setMultiSelectionEnabled(true);
			return fileChooser;
		}

		public URL getIconURL() {
			return null;
		}

		public String getTitle() {
			return "New file";
		}

		public void initialize() {

		}

		public String validate() {
			if (fileChooser.getSelectedFile() == null) {
				return "A file have to be selected";
			}

			return null;
		}

		public String[] getErrorMessages() {
			return new String[0];
		}

		public String[] getFieldNames() {
			return new String[] { "file" };
		}

		public int[] getFieldTypes() {
			return new int[] { SQLUIPanel.STRING };
		}

		public String getId() {
			return FILE_CHOOSER_SIF_ID;
		}

		public String[] getValidationExpressions() {
			return new String[0];
		}

		public String[] getValues() {
			String ret = "";
			File[] selectedFiles = fileChooser.getSelectedFiles();
			String separator = "";
			for (File file : selectedFiles) {
				ret = ret + separator + file.getAbsolutePath();
				separator = "||";
			}

			return new String[] { ret };
		}

		public void setValue(String fieldName, String fieldValue) {
			String[] files = fieldValue.split("\\Q||\\E");
			File[] selectedFiles = new File[files.length];
			for (int i = 0; i < selectedFiles.length; i++) {
				selectedFiles[i] = new File(files[i]);
			}
			fileChooser.setSelectedFiles(selectedFiles);
		}

	}

	protected File[] getSelectedFiles() {
		return filePanel.fileChooser.getSelectedFiles();
	}

}
