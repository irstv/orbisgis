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
/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
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
 *
 * or contact directly:
 * info _at_ orbisgis.org
 */
package org.orbisgis.core.ui.editors.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.gdms.data.DataSource;
import org.gdms.data.edition.EditionEvent;
import org.gdms.data.edition.EditionListener;
import org.gdms.data.edition.FieldEditionEvent;
import org.gdms.data.edition.MetadataEditionListener;
import org.gdms.data.edition.MultipleEditionEvent;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Constraint;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DriverException;
import org.orbisgis.core.Services;
import org.orbisgis.core.background.BackgroundJob;
import org.orbisgis.core.background.BackgroundManager;
import org.orbisgis.core.errorManager.ErrorManager;
import org.orbisgis.core.sif.SQLUIPanel;
import org.orbisgis.core.sif.UIFactory;
import org.orbisgis.core.ui.components.sif.AskValue;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchFrame;
import org.orbisgis.core.ui.plugins.views.tableEditor.TableEditorPlugIn;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;
import org.orbisgis.core.ui.preferences.lookandfeel.UIColorPreferences;
import org.orbisgis.core.ui.preferences.lookandfeel.images.IconLoader;
import org.orbisgis.progress.ProgressMonitor;
import org.orbisgis.progress.NullProgressMonitor;
import org.orbisgis.utils.I18N;

public class TableComponent extends JPanel implements WorkbenchFrame {

        private static final String OPTIMALWIDTH = "OPTIMALWIDTH"; //$NON-NLS-1$
        private static final String SETWIDTH = "SETWIDTH"; //$NON-NLS-1$
        private static final String SORTUP = "SORTUP"; //$NON-NLS-1$
        private static final String SORTDOWN = "SORTDOWN"; //$NON-NLS-1$
        private static final String NOSORT = "NOSORT"; //$NON-NLS-1$
        private static final Color NUMERIC_COLOR = new Color(205, 197, 191);
        private static final Color DEFAULT_COLOR = new Color(238, 229, 222);
        // Swing components
        private javax.swing.JScrollPane jScrollPane = null;
        private JTable table = null;
        private JLabel nbRowsSelectedLabel = null;
        // Model
        private int selectedColumn = -1;
        private DataSourceDataModel tableModel;
        private DataSource dataSource;
        private ArrayList<Integer> indexes = null;
        private Selection selection;
        private TableEditableElement element;
        private int selectedRowsCount;
        // listeners
        private ActionListener menuListener = new PopupActionListener();
        private ModificationListener listener = new ModificationListener();
        private SelectionListener selectionListener = new SyncSelectionListener();
        // flags
        private boolean managingSelection;
        private TableEditorPlugIn editor;
        private org.orbisgis.core.ui.pluginSystem.menu.MenuTree menuTree;
        private SearchToolBars searchToolBar;

        @Override
        public org.orbisgis.core.ui.pluginSystem.menu.MenuTree getMenuTreePopup() {
                return menuTree;
        }

        /**
         * This is the default constructor
         * 
         * @throws DriverException
         */
        public TableComponent(TableEditorPlugIn editor) {
                this.editor = editor;
                initialize();
        }

        /**
         * This method initializes this
         */
        private void initialize() {
                menuTree = new org.orbisgis.core.ui.pluginSystem.menu.MenuTree();
                this.setLayout(new BorderLayout());
                add(getJScrollPane(), BorderLayout.CENTER);
                add(getTableToolBar(), BorderLayout.NORTH);
                add(getSearchToolBar(), BorderLayout.SOUTH);
        }

        /**
         * This method initializes table
         * 
         * @return javax.swing.JTable
         */
        public javax.swing.JTable getTable() {
                if (table == null) {
                        table = new JTable();
                        table.setSelectionBackground(UIColorPreferences.TABLE_EDITOR_SELECTION_BACKGROUND);
                        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                        table.getSelectionModel().setSelectionMode(
                                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                        table.setDragEnabled(true);
                        table.getSelectionModel().addListSelectionListener(
                                new ListSelectionListener() {

                                        @Override
                                        public void valueChanged(ListSelectionEvent e) {
                                                if (!e.getValueIsAdjusting()) {
                                                        if (!managingSelection && (selection != null)) {
                                                                managingSelection = true;
                                                                int[] selectedRows = table.getSelectedRows();
                                                                if (indexes != null) {
                                                                        for (int i = 0; i < selectedRows.length; i++) {
                                                                                selectedRows[i] = indexes.get(selectedRows[i]);
                                                                        }
                                                                }
                                                                selectedRowsCount = selectedRows.length;
                                                                checkSelectionRefresh(selectedRows);

                                                                selection.setSelectedRows(selectedRows);

                                                                managingSelection = false;
                                                                updateRowsMessage();

                                                        }
                                                }
                                        }
                                });
                        table.getTableHeader().setReorderingAllowed(false);
                        table.getTableHeader().addMouseListener(
                                new HeaderPopupMouseAdapter());
                        table.addMouseListener(new CellPopupMouseAdapter());
                        table.setColumnSelectionAllowed(true);
                        table.getColumnModel().setSelectionModel(
                                new DefaultListSelectionModel());
                }

                return table;
        }

        /**
         * Refresh the attributes table linked to a layer
         * @param selectedRows
         */
        public void checkSelectionRefresh(int[] selectedRows) {
                if (this.element.getMapContext() != null) {
                        this.element.getMapContext().checkSelectionRefresh(selectedRows,
                                selection.getSelectedRows(), dataSource);
                }

        }

        public void checkSelectionRefresh() {
                checkSelectionRefresh(new int[0]);
        }

        private Component getTableToolBar() {
                JToolBar toolBar = new JToolBar();
                toolBar.setFloatable(false);
                toolBar.add(getNbRowsInformation(), BorderLayout.WEST);
                toolBar.setBorderPainted(false);
                toolBar.setOpaque(false);
                return toolBar;
        }

        private Component getSearchToolBar() {
                if (searchToolBar == null) {
                        searchToolBar = new SearchToolBars(this);
                }
                return searchToolBar;
        }

        private JLabel getNbRowsInformation() {
                JLabel nbRowsMessage = new JLabel();
                nbRowsMessage.setText(I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.rowNumber")); //$NON-NLS-1$
                nbRowsSelectedLabel = nbRowsMessage;
                nbRowsMessage.setVerticalAlignment(JLabel.CENTER);
                nbRowsMessage.setPreferredSize(new Dimension(230, 19));
                return nbRowsMessage;
        }

        /**
         * This method initializes jScrollPane
         * 
         * @return javax.swing.JScrollPane
         */
        private javax.swing.JScrollPane getJScrollPane() {
                if (jScrollPane == null) {
                        jScrollPane = new javax.swing.JScrollPane();
                        jScrollPane.setViewportView(getTable());
                }

                return jScrollPane;
        }

        /**
         * Shows a dialog with the error type
         * 
         * @param msg
         */
        private void inputError(String msg, Exception e) {
                Services.getService(ErrorManager.class).error(msg, e);
                getTable().requestFocus();
        }

        public boolean tableHasFocus() {
                return table.hasFocus() || table.isEditing();
        }

        public String[] getSelectedFieldNames() {
                int[] selected = table.getSelectedColumns();
                String[] ret = new String[selected.length];

                for (int i = 0; i < ret.length; i++) {
                        ret[i] = tableModel.getColumnName(selected[i]);
                }

                return ret;
        }

        public void setElement(TableEditableElement element) {
                this.element = element;
                if (this.element == null) {
                        if (this.dataSource != null && dataSource.isEditable()) {
                                this.dataSource.removeEditionListener(listener);
                                this.dataSource.removeMetadataEditionListener(listener);
                        }
                        this.dataSource = null;
                        if (this.selection != null) {
                                this.selection.removeSelectionListener(selectionListener);
                                this.selection = null;
                        }
                        table.setModel(new DefaultTableModel());
                } else {
                        this.dataSource = element.getDataSource();
                        if (dataSource.isEditable()) {
                                this.dataSource.addEditionListener(listener);
                                this.dataSource.addMetadataEditionListener(listener);
                        }
                        tableModel = new DataSourceDataModel();
                        table.setModel(tableModel);
                        table.setBackground(DEFAULT_COLOR);
                        autoResizeColWidth(Math.min(5, tableModel.getRowCount()),
                                new HashMap<String, Integer>(),
                                new HashMap<String, TableCellRenderer>());
                        this.selection = element.getSelection();
                        this.selection.setSelectionListener(selectionListener);
                        selectedRowsCount = selection.getSelectedRows().length;
                        updateTableSelection();
                        updateRowsMessage();
                        try {
                                Metadata metadata = dataSource.getMetadata();
                                String[] fields = metadata.getFieldNames();
                                for (String string : fields) {
                                        searchToolBar.getFieldsCmb().addItem(string);
                                }
                        } catch (DriverException ex) {
                                Logger.getLogger(TableComponent.class.getName()).log(Level.SEVERE, null, ex);
                        }


                }
        }

        public TableEditableElement getElement() {
                return element;
        }

        private void autoResizeColWidth(int rowsToCheck,
                HashMap<String, Integer> widths,
                HashMap<String, TableCellRenderer> renderers) {
                DefaultTableColumnModel colModel = new DefaultTableColumnModel();
                int maxWidth = 200;
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        TableColumn col = new TableColumn(i);
                        String columnName = tableModel.getColumnName(i);
                        int columnType = tableModel.getColumnType(i).getTypeCode();

                        col.setHeaderValue(columnName);
                        TableCellRenderer tableCellRenderer = renderers.get(columnName);

                        if (tableCellRenderer == null) {
                                tableCellRenderer = new ButtonHeaderRenderer();
                        }
                        col.setHeaderRenderer(tableCellRenderer);

                        Integer width = widths.get(columnName);
                        if (width == null) {
                                width = getColumnOptimalWidth(rowsToCheck, maxWidth, i,
                                        new NullProgressMonitor());
                        }
                        col.setPreferredWidth(width);
                        colModel.addColumn(col);
                        switch (columnType) {
                                case Type.DOUBLE:
                                case Type.INT:
                                case Type.LONG:
                                        NumberFormat formatter = NumberFormat.getCurrencyInstance();
                                        FormatRenderer formatRenderer = new FormatRenderer(formatter);
                                        formatRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
                                        formatRenderer.setBackground(NUMERIC_COLOR);
                                        col.setCellRenderer(formatRenderer);
                                        break;
                                default:
                                        break;
                        }

                }
                table.setColumnModel(colModel);
        }

        private int getColumnOptimalWidth(int rowsToCheck, int maxWidth,
                int column, ProgressMonitor pm) {
                TableColumn col = table.getColumnModel().getColumn(column);
                int margin = 5;
                int headerMargin = 10;
                int width = 0;

                // Get width of column header
                TableCellRenderer renderer = col.getHeaderRenderer();

                if (renderer == null) {
                        renderer = table.getTableHeader().getDefaultRenderer();
                }

                Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);

                width = comp.getPreferredSize().width;

                // Check header
                comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, column);
                width = Math.max(width, comp.getPreferredSize().width + 2
                        * headerMargin);
                // Get maximum width of column data
                for (int r = 0; r < rowsToCheck; r++) {
                        if (r / 100 == r / 100.0) {
                                if (pm.isCancelled()) {
                                        break;
                                } else {
                                        pm.progressTo(100 * r / rowsToCheck);
                                }
                        }
                        renderer = table.getCellRenderer(r, column);
                        comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, column), false, false, r, column);
                        width = Math.max(width, comp.getPreferredSize().width);
                }

                // limit
                width = Math.min(width, maxWidth);

                // Add margin
                width += 2 * margin;

                return width;
        }

        private void refreshTableStructure() {
                TableColumnModel columnModel = table.getColumnModel();
                HashMap<String, Integer> widths = new HashMap<String, Integer>();
                HashMap<String, TableCellRenderer> renderers = new HashMap<String, TableCellRenderer>();
                try {
                        Metadata metadata = dataSource.getMetadata();
                        int fieldCount = metadata.getFieldCount();
                        searchToolBar.getFieldsCmb().removeAllItems();
                        searchToolBar.getFieldsCmb().addItem(I18N.getString("orbisgis.org.orbigis.core.tableComponent.searchPanel.all"));
                        for (int i = 0; i < fieldCount; i++) {
                                String columnName = null;
                                try {
                                        columnName = metadata.getFieldName(i);
                                        searchToolBar.getFieldsCmb().addItem(columnName);
                                } catch (DriverException e) {
                                }
                                int columnIndex = -1;
                                if (columnName != null) {
                                        try {
                                                columnIndex = columnModel.getColumnIndex(columnName);
                                        } catch (IllegalArgumentException e) {
                                                columnIndex = -1;
                                        }
                                        if (columnIndex != -1) {
                                                TableColumn column = columnModel.getColumn(columnIndex);
                                                widths.put(columnName, column.getPreferredWidth());
                                                renderers.put(columnName, column.getHeaderRenderer());
                                        }
                                }
                        }
                } catch (DriverException e) {
                        Services.getService(ErrorManager.class).warning(
                                I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.refreshTableStructure"), //$NON-NLS-1$
                                e);
                }
                tableModel.fireTableStructureChanged();
                autoResizeColWidth(Math.min(5, tableModel.getRowCount()), widths,
                        renderers);
        }

        /**
         * Retrieve the index of the data in the data source shown at the index row
         * in the displayed table.
         * 
         * @param row
         * @return
         */
        public int getRowIndex(int row) {
                if (indexes != null) {
                        row = indexes.get(row);
                }
                return row;
        }

        public void updateTableSelection() {
                if (!managingSelection) {
                        managingSelection = true;
                        ListSelectionModel model = table.getSelectionModel();
                        model.setValueIsAdjusting(true);
                        model.clearSelection();
                        int[] select = selection.getSelectedRows();
                        for (int i : select) {
                                if (indexes != null) {
                                        Integer sortedIndex = indexes.indexOf(i);
                                        model.addSelectionInterval(sortedIndex, sortedIndex);
                                } else {
                                        model.addSelectionInterval(i, i);
                                }
                        }
                        selectedRowsCount = select.length;
                        model.setValueIsAdjusting(false);
                        managingSelection = false;
                        updateRowsMessage();
                }
        }

        private void fireTableDataChanged() {
                Rectangle r = table.getVisibleRect();
                // to avoid losing the selection
                managingSelection = true;

                tableModel.fireTableDataChanged();

                managingSelection = false;
                updateTableSelection();

                table.scrollRectToVisible(r);
        }

        public int getSelectedRowsCount() {
                return selectedRowsCount;
        }

        public void setSelectedRowsCount(int selectedRowsCount) {
                this.selectedRowsCount = selectedRowsCount;
                updateRowsMessage();
        }

        public DataSourceDataModel getTableModel() {
                return tableModel;
        }

        public DataSource getDataSource() {
                return dataSource;
        }

        public Selection getSelection() {
                return selection;
        }

        public void updateRowsMessage() {
                if (selectedRowsCount > 0) {
                        nbRowsSelectedLabel.setText(I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.rowSelected") //$NON-NLS-1$
                                + selectedRowsCount + " / " + table.getRowCount()); //$NON-NLS-1$
                } else {
                        nbRowsSelectedLabel.setText(I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.rowNumber") //$NON-NLS-1$
                                + tableModel.getRowCount());
                }
        }

        public void moveSelectionUp() {
                int[] selectedRows = selection.getSelectedRows();
                HashSet<Integer> selectedRowSet = new HashSet<Integer>();
                indexes = new ArrayList<Integer>();
                for (int i : selectedRows) {
                        indexes.add(i);
                        selectedRowSet.add(i);
                }
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (!selectedRowSet.contains(i)) {
                                indexes.add(i);
                        }
                }
                fireTableDataChanged();
        }

        /**
         * Display the row header
         * @param show
         */
        public void setShowRowHeader(boolean show) {
                if (show&& table.getRowCount()>0) {
                        jScrollPane.setRowHeaderView(new TableRowHeader(this));
                }
        }

        /**
         * Reverse the selection
         */
        public void revertSelection() {
                if (selectedRowsCount > 0) {
                        BackgroundManager bm = Services.getService(BackgroundManager.class);
                        bm.backgroundOperation(new ReverseJob());
                }
        }

        private class SyncSelectionListener implements SelectionListener {

                @Override
                public void selectionChanged() {
                        updateTableSelection();
                }
        }

        private final class PopupActionListener implements ActionListener {

                @Override
                public void actionPerformed(ActionEvent e) {
                        if (OPTIMALWIDTH.equals(e.getActionCommand())) {
                                BackgroundManager bm = Services.getService(BackgroundManager.class);
                                bm.backgroundOperation(new BackgroundJob() {

                                        @Override
                                        public void run(ProgressMonitor pm) {
                                                final int width = getColumnOptimalWidth(table.getRowCount(), Integer.MAX_VALUE,
                                                        selectedColumn, pm);
                                                final TableColumn col = table.getColumnModel().getColumn(selectedColumn);
                                                SwingUtilities.invokeLater(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                                col.setPreferredWidth(width);
                                                        }
                                                });
                                        }

                                        @Override
                                        public String getTaskName() {
                                                return I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.columnOptimalWidth"); //$NON-NLS-1$
                                        }
                                });
                        } else if (SETWIDTH.equals(e.getActionCommand())) {
                                TableColumn selectedTableColumn = table.getTableHeader().getColumnModel().getColumn(selectedColumn);
                                AskValue av = new AskValue(
                                        I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.newColumnWidth"), //$NON-NLS-1$
                                        null, null, Integer.toString(selectedTableColumn.getPreferredWidth()));
                                av.setType(SQLUIPanel.INT);
                                if (UIFactory.showDialog(av)) {
                                        selectedTableColumn.setPreferredWidth(Integer.parseInt(av.getValue()));
                                }
                        } else if (SORTUP.equals(e.getActionCommand())) {
                                BackgroundManager bm = Services.getService(BackgroundManager.class);
                                bm.backgroundOperation(new SortJob(true));
                        } else if (SORTDOWN.equals(e.getActionCommand())) {
                                BackgroundManager bm = Services.getService(BackgroundManager.class);
                                bm.backgroundOperation(new SortJob(false));
                        } else if (NOSORT.equals(e.getActionCommand())) {
                                indexes = null;
                                fireTableDataChanged();
                        }
                        table.getTableHeader().repaint();
                }
        }

        private abstract class PopupMouseAdapter extends MouseAdapter {

                WorkbenchContext wbContext = Services.getService(WorkbenchContext.class);

                @Override
                public void mousePressed(MouseEvent e) {
                        updateContext(e);
                        popup(e);

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                        popup(e);
                }

                /**
                 * This method is used to update the popup context. Used by plugins to
                 * determine when it's showing.
                 * 
                 * @param e
                 */
                private void updateContext(MouseEvent e) {
                        boolean oneColumnHeaderIsSelected = table.getTableHeader().contains(e.getPoint());
                        selectedColumn = table.columnAtPoint(e.getPoint());
                        int clickedRow = table.rowAtPoint(e.getPoint());
                        final int[] row = new int[]{clickedRow};
                        if (oneColumnHeaderIsSelected) {
                                if ("ColumnAction".equals(getExtensionPointId())) { //$NON-NLS-1$
                                        wbContext.setHeaderSelected(selectedColumn);
                                } else {
                                        wbContext.setRowSelected(e);
                                        if (!table.isRowSelected(clickedRow)) {
                                                checkSelectionRefresh(row);
                                                selection.setSelectedRows(row);
                                                updateTableSelection();
                                        }
                                }
                        } else {
                                wbContext.setRowSelected(e);
                                if (!table.isRowSelected(clickedRow)) {
                                        checkSelectionRefresh(row);
                                        selection.setSelectedRows(row);
                                        updateTableSelection();
                                }
                        }
                }

                private void popup(final MouseEvent e) {

                        final Component component = getComponent();
                        component.repaint();
                        if (e.isPopupTrigger()) {
                                JComponent[] menus = null;
                                final JPopupMenu pop = getPopupMenu();
                                menus = wbContext.getWorkbench().getFrame().getMenuTableTreePopup().getJMenus();
                                for (JComponent menu : menus) {
                                        pop.add(menu);
                                }
                                pop.show(component, e.getX(), e.getY());
                        }

                }

                protected void addMenu(JPopupMenu pop, String text, Icon icon,
                        String actionCommand) {
                        JMenuItem menu = new JMenuItem(text);
                        menu.setIcon(icon);
                        menu.setActionCommand(actionCommand);
                        menu.addActionListener(menuListener);
                        pop.add(menu);
                }

                protected abstract Component getComponent();

                protected abstract String getExtensionPointId();

                protected abstract JPopupMenu getPopupMenu();
        }

        private class HeaderPopupMouseAdapter extends PopupMouseAdapter {

                @Override
                protected Component getComponent() {
                        return table.getTableHeader();
                }

                @Override
                protected String getExtensionPointId() {
                        return "ColumnAction"; //$NON-NLS-1$
                }

                @Override
                protected JPopupMenu getPopupMenu() {
                        JPopupMenu pop = new JPopupMenu();
                        addMenu(
                                pop,
                                I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.optimalWidth"), //$NON-NLS-1$
                                IconLoader.getIcon("text_letterspacing.png"), OPTIMALWIDTH); //$NON-NLS-1$
                        addMenu(
                                pop,
                                I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.setWidth"), //$NON-NLS-1$
                                null, SETWIDTH);
                        pop.addSeparator();
                        if (tableModel.getColumnType(selectedColumn).getTypeCode() != Type.GEOMETRY) {
                                addMenu(
                                        pop,
                                        I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.sortAscending"), //$NON-NLS-1$
                                        IconLoader.getIcon("thumb_up.png"), SORTUP); //$NON-NLS-1$
                                addMenu(
                                        pop,
                                        I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.sortDescending"), //$NON-NLS-1$
                                        IconLoader.getIcon("thumb_down.png"), SORTDOWN); //$NON-NLS-1$
                                addMenu(
                                        pop,
                                        I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.noSort"), //$NON-NLS-1$
                                        OrbisGISIcon.TABLE_REFRESH, NOSORT);
                        }
                        return pop;
                }
        }

        private class CellPopupMouseAdapter extends PopupMouseAdapter {

                @Override
                protected Component getComponent() {
                        return table;
                }

                @Override
                protected String getExtensionPointId() {
                        return "CellAction"; //$NON-NLS-1$
                }

                @Override
                protected JPopupMenu getPopupMenu() {
                        return new JPopupMenu();
                }
        }

        private class ModificationListener implements EditionListener,
                MetadataEditionListener {

                @Override
                public void multipleModification(MultipleEditionEvent e) {
                        tableModel.fireTableDataChanged();
                }

                @Override
                public void singleModification(EditionEvent e) {
                        if (e.getType() != EditionEvent.RESYNC) {
                                int row = (int) e.getRowIndex();
                                if (indexes != null) {
                                        row = indexes.indexOf(new Integer(row));
                                }
                                int column = e.getFieldIndex();
                                if ((e.getType() == EditionEvent.DELETE)
                                        || (e.getType() == EditionEvent.INSERT)) {
                                        refreshTableStructure();

                                } else {
                                        tableModel.fireTableCellUpdated(row, column);
                                }
                                if (row != -1) {
                                        table.scrollRectToVisible(table.getCellRect(row, column,
                                                true));
                                }
                        } else {
                                refreshTableStructure();
                        }
                }

                @Override
                public void fieldAdded(FieldEditionEvent event) {
                        fieldRemoved(null);

                }

                @Override
                public void fieldModified(FieldEditionEvent event) {
                        fieldRemoved(null);
                }

                @Override
                public void fieldRemoved(FieldEditionEvent event) {
                        refreshTableStructure();
                }
        }

        public class DataSourceDataModel extends AbstractTableModel {

                private Metadata metadata;

                private Metadata getMetadata() throws DriverException {
                        if (metadata == null) {
                                metadata = dataSource.getMetadata();

                        }
                        return metadata;
                }

                /**
                 * Returns the name of the field.
                 * 
                 * @param col
                 *            index of field
                 * 
                 * @return Name of field
                 */
                @Override
                public String getColumnName(int col) {
                        try {
                                return getMetadata().getFieldName(col);
                        } catch (DriverException e) {
                                return null;
                        }
                }

                /**
                 * Returns the field index
                 * @param fieldName
                 * @return field index
                 */
                public int getFieldIndex(String fieldName) {
                        try {
                                return getMetadata().getFieldIndex(fieldName);
                        } catch (DriverException e) {
                                return -1;
                        }
                }

                /**
                 * Returns the type of field
                 * 
                 * @param col
                 *            index of field
                 * @return Type of field
                 */
                public Type getColumnType(int col) {
                        try {
                                return getMetadata().getFieldType(col);
                        } catch (DriverException e) {
                                return null;
                        }
                }

                /**
                 * Returns the number of fields.
                 * 
                 * @return number of fields
                 */
                @Override
                public int getColumnCount() {
                        try {
                                return getMetadata().getFieldCount();
                        } catch (DriverException e) {
                                return 0;
                        }
                }

                /**
                 * Returns number of rows.
                 * 
                 * @return number of rows.
                 */
                @Override
                public int getRowCount() {
                        try {
                                return (int) dataSource.getRowCount();
                        } catch (DriverException e) {
                                return 0;
                        }
                }

                /**
                 * Returns the values of a specific row
                 * @param i
                 * @return
                 */
                public Value[] getRow(long i) {
                        try {
                                return dataSource.getRow(i);
                        } catch (DriverException e) {
                                return null;
                        }
                }

                /**
                 * @see javax.swing.table.TableModel#getValueAt(int, int)
                 */
                @Override
                public Object getValueAt(int row, int col) {
                        try {
                                return dataSource.getFieldValue(getRowIndex(row), col).toString();
                        } catch (DriverException e) {
                                return ""; //$NON-NLS-1$
                        }
                }

                /**
                 * @see javax.swing.table.TableModel#isCellEditable(int, int)
                 */
                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                        if (element.isEditable()) {
                                try {
                                        Type fieldType = getMetadata().getFieldType(columnIndex);
                                        Constraint c = fieldType.getConstraint(Constraint.READONLY);
                                        return (fieldType.getTypeCode() != Type.RASTER)
                                                && (c == null);
                                } catch (DriverException e) {
                                        return false;
                                }
                        } else {
                                return false;
                        }
                }

                /**
                 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int,
                 *      int)
                 */
                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                        try {
                                Type type = getMetadata().getFieldType(columnIndex);
                                String strValue = aValue.toString().trim();
                                Value v = ValueFactory.createValueByType(strValue, type.getTypeCode());
                                dataSource.setFieldValue(getRowIndex(rowIndex), columnIndex, v);
                        } catch (DriverException e1) {
                                throw new RuntimeException(e1);
                        } catch (NumberFormatException e) {
                                inputError(I18N.getString("orbisgis.org.orbisgis.ui.table.tableComponent.cannotParseNumber") + e.getMessage(), e); //$NON-NLS-1$
                        } catch (ParseException e) {
                                inputError(e.getMessage(), e);
                        }
                }
        }

        /**
         * A background job to revert a selection
         */
        private final class ReverseJob implements BackgroundJob {

                @Override
                public void run(ProgressMonitor pm) {
                        int[] select = selection.getSelectedRows();
                        Arrays.sort(select);
                        int count = tableModel.getRowCount();
                        ArrayList<Integer> newSel = new ArrayList<Integer>();
                        for (int i = 0; i < count; i++) {
                                if (i / 100 == i / 100.0) {
                                        if (pm.isCancelled()) {
                                                break;
                                        } else {
                                                pm.progressTo(100 * i / count);
                                        }
                                }
                                int result = Arrays.binarySearch(select, i);
                                if (result < 0) {
                                        newSel.add(i);

                                }
                        }
                        int size = newSel.size();
                        int[] sel = new int[size];
                        for (int i = 0; i < sel.length; i++) {
                                sel[i] = newSel.get(i);
                        }
                        selectedRowsCount = size;
                        checkSelectionRefresh(sel);
                        selection.setSelectedRows(sel);
                        updateRowsMessage();

                }

                @Override
                public String getTaskName() {
                        return "Reverse";
                }
        }

        private final class SortJob implements BackgroundJob {

                private boolean ascending;

                public SortJob(boolean ascending) {
                        this.ascending = ascending;
                }

                @Override
                public void run(ProgressMonitor pm) {
                        try {
                                int rowCount = (int) dataSource.getRowCount();
                                Value[][] cache = new Value[rowCount][1];
                                for (int i = 0; i < rowCount; i++) {
                                        cache[i][0] = dataSource.getFieldValue(i, selectedColumn);
                                }
                                ArrayList<Boolean> order = new ArrayList<Boolean>();
                                order.add(ascending);
                                TreeSet<Integer> sortset = new TreeSet<Integer>(
                                        new SortComparator(cache, order));
                                for (int i = 0; i < rowCount; i++) {
                                        if (i / 100 == i / 100.0) {
                                                if (pm.isCancelled()) {
                                                        break;
                                                } else {
                                                        pm.progressTo(100 * i / rowCount);
                                                }
                                        }
                                        sortset.add(new Integer(i));
                                }
                                ArrayList<Integer> indexes = new ArrayList<Integer>();
                                Iterator<Integer> it = sortset.iterator();
                                while (it.hasNext()) {
                                        Integer integer = (Integer) it.next();
                                        indexes.add(integer);
                                }
                                TableComponent.this.indexes = indexes;
                                SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                                fireTableDataChanged();
                                        }
                                });
                        } catch (DriverException e) {
                                Services.getService(ErrorManager.class).error(I18N.getString("orbisgis.org.orbisgis.ui.table.tableComponent.cannotSort"), e); //$NON-NLS-1$
                        }
                }

                @Override
                public String getTaskName() {
                        return I18N.getString("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.sorting"); //$NON-NLS-1$
                }
        }

        public class ButtonHeaderRenderer extends JButton implements
                TableCellRenderer {

                public ButtonHeaderRenderer() {
                        setMargin(new Insets(0, 0, 0, 0));
                }

                @Override
                public Component getTableCellRendererComponent(JTable table,
                        Object value, boolean isSelected, boolean hasFocus, int row,
                        int column) {
                        setText((value == null) ? "" : value.toString()); //$NON-NLS-1$
                        boolean isPressed = (column == selectedColumn);
                        if (isPressed) {
                                setPressedColumn(column);
                        }
                        getModel().setPressed(isPressed);
                        getModel().setArmed(isPressed);
                        return this;
                }

                public void setPressedColumn(int col) {
                        selectedColumn = col;
                }
        }

        /**
         * Return the index of the selected column
         * @return
         */
        public int getSelectedColumn() {
                return selectedColumn;
        }

        /**
         * Return the row height
         * @return
         */
        public int getRowHeight() {
                return table.getRowHeight();
        }

        /**
         * Return the number of rows
         * @return
         */
        public long getRowCount() {
                return table.getRowCount();
        }

        /**
         * Return the table grid color
         * @return
         */
        public Color getGridColor() {
                return table.getGridColor();
        }
}
