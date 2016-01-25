/**
 * OrbisToolBox is an OrbisGIS plugin dedicated to create and manage processing.
 * <p/>
 * OrbisToolBox is distributed under GPL 3 license. It is produced by CNRS <http://www.cnrs.fr/> as part of the
 * MApUCE project, funded by the French Agence Nationale de la Recherche (ANR) under contract ANR-13-VBDU-0004.
 * <p/>
 * OrbisToolBox is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p/>
 * OrbisToolBox is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with OrbisToolBox. If not, see
 * <http://www.gnu.org/licenses/>.
 * <p/>
 * For more information, please consult: <http://www.orbisgis.org/> or contact directly: info_at_orbisgis.org
 */

package org.orbisgis.orbistoolbox.view.ui.dataui;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FilenameUtils;
import org.orbisgis.orbistoolbox.controller.processexecution.utils.FormatFactory;
import org.orbisgis.orbistoolbox.model.*;
import org.orbisgis.orbistoolbox.view.ToolBox;
import org.orbisgis.orbistoolbox.view.utils.ToolBoxIcon;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.common.ContainerItem;
import org.orbisgis.sif.components.OpenFilePanel;
import org.orbisgis.sif.components.SaveFilePanel;
import org.orbisgis.sif.multiInputPanel.CheckBoxChoice;
import org.orbisgis.sif.multiInputPanel.MultiInputPanel;
import org.orbisgis.sif.multiInputPanel.TextBoxType;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.EventHandler;
import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

/**
 * DataUI for DataStore
 *
 * @author Sylvain PALOMINOS
 **/

public class DataStoreUI implements DataUI{

    /** constant size of the text fields **/
    private static final int TEXTFIELD_WIDTH = 25;

    private ToolBox toolBox;

    public void setToolBox(ToolBox toolBox){
        this.toolBox = toolBox;
    }

    @Override
    public Map<URI, Object> getDefaultValue(DescriptionType inputOrOutput) {
        return new HashMap<>();
    }

    @Override
    public ImageIcon getIconFromData(DescriptionType inputOrOutput) {
        return ToolBoxIcon.getIcon("datastore");
    }

    @Override
    public JComponent createUI(DescriptionType inputOrOutput, Map<URI, Object> dataMap) {
        JPanel panel = new JPanel(new MigLayout("fill"));
        DataStore dataStore = null;
        Map<String, String> extensionMap = null;
        boolean isOptional = false;
        //If the descriptionType is an input, add a comboBox to select the input type and according to the type,
        // add a second JComponent to write the input value
        if(inputOrOutput instanceof Input){
            Input input = (Input)inputOrOutput;
            dataStore = (DataStore)input.getDataDescription();
            extensionMap = ToolBox.getImportableFormat(true);
            if(input.getMinOccurs() == 0){
                isOptional = true;
            }
        }
        if(inputOrOutput instanceof Output){
            Output output = (Output)inputOrOutput;
            dataStore = (DataStore)output.getDataDescription();
            extensionMap = ToolBox.getExportableFormat(true);
        }
        if(dataStore == null || extensionMap == null){
            return panel;
        }
        panel.add(new JLabel("Select"));

        ButtonGroup group;
        if(isOptional) {
            //Override the setSelected method to allow to unselect buttons
            group = new ButtonGroup(){
                @Override
                public void setSelected(ButtonModel m, boolean b) {
                    if(b && m != null && m != getSelection()){
                        super.setSelected(m, b);
                    }
                    else if (!b && m == getSelection()){
                        clearSelection();
                    }
                }
            };
        }
        else{
            group = new ButtonGroup();
        }

        /**Instantiate the geocatalog radioButton and its optionPanel**/
        JRadioButton geocatalog = new JRadioButton("Geocatalog");
        JPanel optionPanelGeocatalog = new JPanel(new BorderLayout());
        JComboBox<String> comboBox;
        if(dataStore.isSpatial()) {
            comboBox = new JComboBox<>(ToolBox.getGeocatalogTableList(true).toArray(new String[]{}));
        }
        else {
            comboBox = new JComboBox<>(ToolBox.getGeocatalogTableList(false).toArray(new String[]{}));
        }
        comboBox.addActionListener(EventHandler.create(ActionListener.class, this, "onGeocatalogTableSelected", "source"));
        comboBox.addMouseListener(EventHandler.create(MouseListener.class, this, "onComboBoxEntered", "source", "mouseEntered"));
        comboBox.addMouseListener(EventHandler.create(MouseListener.class, this, "onComboBoxExited", "source", "mouseExited"));
        comboBox.putClientProperty("uri", inputOrOutput.getIdentifier());
        comboBox.putClientProperty("dataMap", dataMap);
        comboBox.putClientProperty("dataStore", dataStore);
        comboBox.setBackground(Color.WHITE);
        comboBox.setToolTipText(inputOrOutput.getResume());
        JPanel tableSelection = new JPanel(new MigLayout("fill"));
        tableSelection.add(comboBox, "growx, span");
        if(inputOrOutput instanceof Output){
            String newTable = "New_Table";
            comboBox.insertItemAt(newTable, 0);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(newTable);
            Document doc = ((JTextComponent)comboBox.getEditor().getEditorComponent()).getDocument();
            doc.putProperty("comboBox", comboBox);
            doc.addDocumentListener(EventHandler.create(DocumentListener.class, this, "onNewTable", "document"));
        }
        optionPanelGeocatalog.add(new JLabel("Geocatalog :"), BorderLayout.LINE_START);
        optionPanelGeocatalog.add(tableSelection, BorderLayout.CENTER);
        geocatalog.putClientProperty("optionPanel", optionPanelGeocatalog);
        geocatalog.addActionListener(EventHandler.create(ActionListener.class, this, "onRadioSelected", "source"));

        /**Instantiate the file radioButton and its optionPanel**/
        JRadioButton file = new JRadioButton("File");
        JPanel optionPanelFile = new JPanel(new BorderLayout());
        optionPanelFile.add(new JLabel("file :"), BorderLayout.LINE_START);
        JTextField textField = new JTextField();
        textField.getDocument().putProperty("dataMap", dataMap);
        textField.getDocument().putProperty("inputOrOutput", inputOrOutput);
        textField.getDocument().putProperty("dataStore", dataStore);
        textField.getDocument().addDocumentListener(EventHandler.create(DocumentListener.class,
                this,
                "saveDocumentTextFile",
                "document"));
        textField.setToolTipText(inputOrOutput.getResume());

        optionPanelFile.add(textField, BorderLayout.CENTER);
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(EventHandler.create(ActionListener.class, this, "onBrowse", ""));
        browseButton.putClientProperty("uri", inputOrOutput.getIdentifier());
        browseButton.putClientProperty("dataMap", dataMap);
        browseButton.putClientProperty("JTextField", textField);
        browseButton.putClientProperty("dataStore", dataStore);
        OpenFilePanel filePanel;
        //If it is an input, the file panel is an Open one
        if(inputOrOutput instanceof Input){
            filePanel = new OpenFilePanel("DataStoreUI.File."+inputOrOutput.getIdentifier(), "Select File");
            filePanel.setAcceptAllFileFilterUsed(false);
            for(Format format : dataStore.getFormats()){
                String ext = FormatFactory.getFormatExtension(format);
                String description = "";
                for(Map.Entry<String, String> entry : ToolBox.getImportableFormat(false).entrySet()){
                    if(entry.getKey().equalsIgnoreCase(ext)){
                        description = entry.getValue();
                    }
                }
                filePanel.addFilter(ext, description);
            }
        }
        //If it is an output, the file panel is an Save one
        else {
            filePanel = new SaveFilePanel("DataStoreUI.File."+inputOrOutput.getIdentifier(), "Save File");
            for(Format format : dataStore.getFormats()){
                String ext = FormatFactory.getFormatExtension(format);
                String description = "";
                for(Map.Entry<String, String> entry : ToolBox.getExportableFormat(false).entrySet()){
                    if(entry.getKey().equalsIgnoreCase(ext)){
                        description = entry.getValue();
                    }
                }
                filePanel.addFilter(ext, description);
            }
        }
        filePanel.loadState();
        textField.setText(filePanel.getCurrentDirectory().getAbsolutePath());
        browseButton.putClientProperty("filePanel", filePanel);

        optionPanelFile.add(browseButton, BorderLayout.LINE_END);
        file.putClientProperty("optionPanel", optionPanelFile);
        file.addActionListener(EventHandler.create(ActionListener.class, this, "onRadioSelected", "source"));

        /**Instantiate the dataBase radioButton and its optionPanel**/
        JRadioButton database = new JRadioButton("Database");
        JPanel optionPanelDataBase = new JPanel(new BorderLayout());
        JLabel label = new JLabel("database :");
        optionPanelDataBase.add(label, BorderLayout.LINE_START);
        JTextField parametersTextField = new JTextField();
        parametersTextField.getDocument().putProperty("dataMap", dataMap);
        parametersTextField.getDocument().putProperty("uri", inputOrOutput.getIdentifier());
        parametersTextField.getDocument().putProperty("dataStore", dataStore);
        parametersTextField.getDocument().addDocumentListener(EventHandler.create(DocumentListener.class,
                this,
                "saveDocumentTextDataBase",
                "document"));
        optionPanelDataBase.add(parametersTextField, BorderLayout.CENTER);
        JButton parametersButton = new JButton("Parameters");
        parametersButton.putClientProperty("textField", parametersTextField);
        parametersButton.addActionListener(EventHandler.create(ActionListener.class, this, "onParameters", "source"));
        optionPanelDataBase.add(parametersButton, BorderLayout.LINE_END);
        database.putClientProperty("optionPanel", optionPanelDataBase);
        database.addActionListener(EventHandler.create(ActionListener.class, this, "onRadioSelected", "source"));

        JPanel radioPanel = new JPanel(new MigLayout("fill"));
        JComponent dataField  = new JPanel(new MigLayout("fill"));
        if(dataStore.isFile()){
            group.add(file);
            radioPanel.add(file, "growx");
            file.putClientProperty("dataField", dataField);
            file.putClientProperty("dataMap", dataMap);
            file.putClientProperty("uri", inputOrOutput.getIdentifier());

            file.setSelected(true);
            dataField.removeAll();
            dataField.add(optionPanelFile, "growx, span");
        }
        /*if(dataStore.isDataBase()){
            group.add(database);
            radioPanel.add(database, "growx");
            database.putClientProperty("dataField", dataField);
            database.putClientProperty("dataMap", dataMap);
            database.putClientProperty("uri", inputOrOutput.getIdentifier());

            database.setSelected(true);
            dataField.removeAll();
            dataField.add(optionPanelDataBase, "growx, span");
        }*/
        if(dataStore.isGeocatalog()){
            group.add(geocatalog);
            radioPanel.add(geocatalog, "growx");
            geocatalog.putClientProperty("dataField", dataField);
            geocatalog.putClientProperty("dataMap", dataMap);
            geocatalog.putClientProperty("uri", inputOrOutput.getIdentifier());

            geocatalog.setSelected(true);
            dataField.removeAll();
            dataField.add(optionPanelGeocatalog, "growx, span");
            if(comboBox.getItemCount() > 0){
                comboBox.setSelectedIndex(0);
            }
        }
        panel.add(radioPanel, "growx, wrap");
        panel.add(dataField, "growx, span");

        return panel;
    }

    /**
     * When the mouse enter in the JComboBox and their is no sources listed in the JComboBox,
     * it shows a tooltip text to the user.
     * @param source Source JComboBox
     */
    public void onComboBoxEntered(Object source){
        //Retrieve the client properties
        JComboBox<String> comboBox = (JComboBox)source;
        if(comboBox.getItemCount() == 0) {
            comboBox.putClientProperty("initialDelay", ToolTipManager.sharedInstance().getInitialDelay());
            comboBox.putClientProperty("toolTipText", comboBox.getToolTipText());
            ToolTipManager.sharedInstance().setInitialDelay(0);
            ToolTipManager.sharedInstance().setDismissDelay(2500);
            comboBox.setToolTipText("First add a table to the Geocatalog");
            ToolTipManager.sharedInstance().mouseMoved(
                    new MouseEvent(comboBox,MouseEvent.MOUSE_MOVED,System.currentTimeMillis(),0,0,0,0,false));
        }
        DataStore dataStore = (DataStore)comboBox.getClientProperty("dataStore");
        if(dataStore.isSpatial()) {
            comboBox.removeAllItems();
            for(String s : ToolBox.getGeocatalogTableList(true).toArray(new String[]{})){
                comboBox.addItem(s);
            }
        }
        else {
            comboBox.removeAllItems();
            for(String s : ToolBox.getGeocatalogTableList(false).toArray(new String[]{})){
                comboBox.addItem(s);
            }
        }
    }

    /**
     * When the mouse leaves the JComboBox, reset the tooltip text delay.
     * @param source JComboBox source.
     */
    public void onComboBoxExited(Object source){
        //Retrieve the client properties
        JComboBox<ContainerItem<String>> comboBox = (JComboBox)source;
        Object tooltipText = comboBox.getClientProperty("toolTipText");
        if(tooltipText != null) {
            comboBox.setToolTipText((String)tooltipText);
        }
        Object delay = comboBox.getClientProperty("initialDelay");
        if(delay != null){
            ToolTipManager.sharedInstance().setInitialDelay((int)delay);
        }
    }

    /**
     * When a table is selected in the geocatalog field, empty the textField for a new table,
     * save the selectedtable,
     * tell the child DataField that there is a modification.
     * @param source Source geocatalog JComboBox
     */
    public void onGeocatalogTableSelected(Object source){
        JComboBox<String> comboBox = (JComboBox) source;
        //If the ComboBox is empty, don't do anything.
        //The process won't launch util the user sets the DataStore
        if(comboBox.getItemCount()>0 && comboBox.getItemAt(0).isEmpty()){
            return;
        }
        if(comboBox.getClientProperty("textField") != null){
            JTextField textField = (JTextField)comboBox.getClientProperty("textField");
            if(!textField.getText().isEmpty() && comboBox.getSelectedIndex() != comboBox.getItemCount()-1) {
                textField.setText("");
            }
            if(!textField.getText().isEmpty() && comboBox.getSelectedIndex() == comboBox.getItemCount()-1){
                return;
            }
        }
        //Retrieve the client properties
        Map<URI, Object> dataMap = (Map<URI, Object>) comboBox.getClientProperty("dataMap");
        URI uri = (URI) comboBox.getClientProperty("uri");
        DataStore dataStore = (DataStore) comboBox.getClientProperty("dataStore");
        //Tells all the dataField linked that the data source is loaded
        for (DataField dataField : dataStore.getListDataField()) {
            dataField.setSourceModified(true);
        }
        dataMap.remove(uri);
        dataMap.put(uri, URI.create("geocatalog:"+comboBox.getSelectedItem()+"#"+comboBox.getSelectedItem()));
    }

    /**
     * When a new table name is set, empty the comboBox and save the table name.
     * @param document
     */
    public void onNewTable(Document document){
        try {
            JComboBox<String> comboBox = (JComboBox<String>)document.getProperty("comboBox");
            Map<URI, Object> dataMap = (Map<URI, Object>)comboBox.getClientProperty("dataMap");
            URI uri = (URI)comboBox.getClientProperty("uri");
            String text = document.getText(0, document.getLength());
            if(!text.isEmpty()){
                dataMap.put(uri, URI.create("geocatalog:"+text.toUpperCase()+"#"+text.toUpperCase()));
            }
        } catch (BadLocationException e) {
            LoggerFactory.getLogger(DataStoreUI.class).error(e.getMessage());
        }
    }

    public void onParameters(Object source){
        if(source instanceof JButton){
            JButton parametersButton = (JButton)source;
            JTextField textField = (JTextField)parametersButton.getClientProperty("textField");
            MultiInputPanel multiInputPanel = new MultiInputPanel("JDBC parameters");
            TextBoxType textBoxDriver = new TextBoxType(TEXTFIELD_WIDTH);
            TextBoxType textBoxJDBCUrl = new TextBoxType(TEXTFIELD_WIDTH);
            CheckBoxChoice checkBoxPasswd = new CheckBoxChoice(false);
            TextBoxType textBoxSchema = new TextBoxType(TEXTFIELD_WIDTH);
            TextBoxType textBoxTable = new TextBoxType(TEXTFIELD_WIDTH);
            multiInputPanel.addInput("driver", "Driver :", "driver string", textBoxDriver);
            multiInputPanel.addInput("jdbcUrl", "JDBC Url :", "jdbc url", textBoxJDBCUrl);
            multiInputPanel.addInput("passwd", "Requires password :", checkBoxPasswd);
            multiInputPanel.addInput("schema", "Schema :", "schema", textBoxSchema);
            multiInputPanel.addInput("table", "Table :", "table", textBoxTable);

            if(UIFactory.showDialog(multiInputPanel, true, true)){
                URI uri = URI.create(textBoxJDBCUrl.getValue()+"?auth="+checkBoxPasswd.getValue()+
                        ";driver="+textBoxDriver.getValue()+";schema="+textBoxSchema.getValue()+
                        "#"+textBoxTable.getValue());
                textField.setText(uri.toString());
            }
        }
    }

    public void onRadioSelected(Object source){
        if(source instanceof JRadioButton){
            JRadioButton radioButton = (JRadioButton)source;
            JPanel dataField = (JPanel) radioButton.getClientProperty("dataField");
            dataField.removeAll();
            if(radioButton.isSelected()) {
                JPanel optionPanel = (JPanel) radioButton.getClientProperty("optionPanel");
                dataField.add(optionPanel, "growx, span");
                dataField.repaint();
            }
            else{
                HashMap<URI, Object> dataMap = (HashMap<URI, Object>)radioButton.getClientProperty("dataMap");
                URI uri = (URI)radioButton.getClientProperty("uri");
                dataMap.put(uri, null);
            }
            dataField.revalidate();
        }
    }

    /**
     * Opens an LoadPanel to permit to the user to select the file to load.
     * @param event
     */
    public void onBrowse(ActionEvent event){
        //Open the file browse window
        JButton source = (JButton)event.getSource();
        OpenFilePanel openFilePanel = (OpenFilePanel)source.getClientProperty("filePanel");
        if (UIFactory.showDialog(openFilePanel, true, true)) {
            DataStore dataStore = (DataStore) source.getClientProperty("dataStore");
            URI selectedFileURI = openFilePanel.getSelectedFile().toURI();
            //Load the selected file an retrieve the table name.
            String tableName = toolBox.loadURI(selectedFileURI);
            if(tableName != null) {
                //Saves the table name in the URI into the uri fragment
                selectedFileURI = URI.create(selectedFileURI.toString()+"#"+tableName);
                //Set the UI with the selected value
                JTextField textField = (JTextField) source.getClientProperty("JTextField");
                textField.setText(openFilePanel.getSelectedFile().getName());
                Map<URI, Object> dataMap = (Map<URI, Object>) source.getClientProperty("dataMap");
                //Store the selection
                URI uri = (URI) source.getClientProperty("uri");
                dataMap.remove(uri);
                dataMap.put(uri, selectedFileURI);
                //tells the dataField they should revalidate
                for (DataField dataField : dataStore.getListDataField()) {
                    dataField.setSourceModified(true);
                }
            }
            else{
                for (DataField dataField : dataStore.getListDataField()) {
                    dataField.setSourceModified(false);
                }
            }
        }
    }

    /**
     * Save the text contained by the Document in the dataMap set as property.
     * @param document
     */
    public void saveDocumentTextFile(Document document){
        try {
            DataStore dataStore = (DataStore)document.getProperty("dataStore");
            DescriptionType inputOrOutput = (DescriptionType)document.getProperty("inputOrOutput");
            File file = new File(document.getText(0, document.getLength()));
            if(inputOrOutput instanceof Input) {
                //Load the selected file an retrieve the table name.
                String tableName = toolBox.loadURI(file.toURI());
                if (tableName != null) {
                    //Saves the table name in the URI into the uri fragment
                    URI selectedFileURI = URI.create(file.toURI().toString() + "#" + tableName);
                    //Store the selection
                    Map<URI, Object> dataMap = (Map<URI, Object>) document.getProperty("dataMap");
                    URI uri = inputOrOutput.getIdentifier();
                    dataMap.remove(uri);
                    dataMap.put(uri, selectedFileURI);
                    //tells the dataField they should revalidate
                    for (DataField dataField : dataStore.getListDataField()) {
                        dataField.setSourceModified(true);
                    }
                }
                else{
                    for (DataField dataField : dataStore.getListDataField()) {
                        dataField.setSourceModified(false);
                    }
                }
            }
            if(inputOrOutput instanceof Output){
                String tableName = toolBox.getDataManager().findUniqueTableName(FilenameUtils.getBaseName(file.getName()));
                URI selectedFileURI = URI.create(file.toURI().toString() + "#" + tableName);
                //Store the selection
                Map<URI, Object> dataMap = (Map<URI, Object>) document.getProperty("dataMap");
                URI uri = inputOrOutput.getIdentifier();
                dataMap.remove(uri);
                dataMap.put(uri, selectedFileURI);
            }
        } catch (BadLocationException|SQLException e) {
            LoggerFactory.getLogger(DataStore.class).error(e.getMessage());
        }
    }

    /**
     * Save the text contained by the Document in the dataMap set as property.
     * @param document
     */
    public void saveDocumentTextDataBase(Document document){
        try {
            DataStore dataStore = (DataStore)document.getProperty("dataStore");
            URI dataBaseURI = URI.create(document.getText(0, document.getLength()));
            //Load the selected file an retrieve the table name.
            String tableName = toolBox.loadURI(dataBaseURI);
            if(tableName != null) {
                //Store the selection
                Map<URI, Object> dataMap = (Map<URI, Object>)document.getProperty("dataMap");
                URI uri = (URI)document.getProperty("uri");
                dataMap.remove(uri);
                dataMap.put(uri, tableName);
                //tells the dataField they should revalidate
                for (DataField dataField : dataStore.getListDataField()) {
                    dataField.setSourceModified(true);
                }
            }
            else{
                for (DataField dataField : dataStore.getListDataField()) {
                    dataField.setSourceModified(false);
                }
            }
        } catch (BadLocationException e) {
            LoggerFactory.getLogger(DataStore.class).error(e.getMessage());
        }
    }
}