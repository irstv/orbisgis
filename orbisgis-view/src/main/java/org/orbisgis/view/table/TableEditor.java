/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. 
 * 
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * 
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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
 * PROP_LABEL
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.view.table;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.sql.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.orbisgis.core.Services;
import org.orbisgis.corejdbc.DataManager;
import org.orbisgis.corejdbc.common.IntegerUnion;
import org.orbisgis.coremap.layerModel.ILayer;
import org.orbisgis.coremap.layerModel.MapContext;
import org.orbisgis.mapeditorapi.MapElement;
import org.orbisgis.progress.NullProgressMonitor;
import org.orbisgis.progress.ProgressMonitor;
import org.orbisgis.view.background.BackgroundJob;
import org.orbisgis.view.background.BackgroundManager;
import org.orbisgis.view.components.actions.ActionCommands;
import org.orbisgis.view.components.filter.DefaultActiveFilter;
import org.orbisgis.view.components.filter.FilterFactoryManager;
import org.orbisgis.view.table.jobs.CreateSourceFromSelection;
import org.orbisgis.viewapi.docking.DockingLocation;
import org.orbisgis.viewapi.docking.DockingPanelParameters;
import org.orbisgis.viewapi.edition.EditableElement;
import org.orbisgis.viewapi.edition.EditableElementException;
import org.orbisgis.viewapi.edition.EditableSource;
import org.orbisgis.viewapi.edition.EditorDockable;
import org.orbisgis.view.icons.OrbisGISIcon;
import org.orbisgis.view.table.ext.SourceTable;
import org.orbisgis.view.table.filters.FieldsContainsFilterFactory;
import org.orbisgis.view.table.filters.TableSelectionFilter;
import org.orbisgis.view.table.filters.WhereSQLFilterFactory;
import org.orbisgis.view.table.jobs.ComputeFieldStatistics;
import org.orbisgis.view.table.jobs.OptimalWidthJob;
import org.orbisgis.view.table.jobs.SearchJob;
import org.orbisgis.view.table.jobs.ZoomToSelectionJob;
import org.orbisgis.viewapi.edition.EditorManager;
import org.orbisgis.viewapi.table.TableEditableElement;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Edit a data source through a grid GUI.
 * @author Nicolas Fortin
 */
public class TableEditor extends JPanel implements EditorDockable,SourceTable {
        protected final static I18n I18N = I18nFactory.getI18n(TableEditor.class);
        private static final Logger LOGGER = Logger.getLogger("gui."+TableEditor.class);
        
        private static final long serialVersionUID = 1L;
        private TableEditableElement tableEditableElement;
        private DockingPanelParameters dockingPanelParameters;
        private JTable table;
        private JScrollPane tableScrollPane;
        private DataSourceRowSorter tableSorter;
        private DataSourceTableModel tableModel;
        private AtomicBoolean initialised = new AtomicBoolean(false);
        // Property selection change Event trigered by TableEditableElement
        // is ignored if onUpdateEditableSelection is true
        private AtomicBoolean onUpdateEditableSelection = new AtomicBoolean(false);
        private AtomicBoolean filterRunning = new AtomicBoolean(false);
        private FilterFactoryManager<TableSelectionFilter,DefaultActiveFilter> filterManager = 
                new FilterFactoryManager<TableSelectionFilter,DefaultActiveFilter>();
        private TableRowHeader tableRowHeader;
        private Point popupCellAdress = new Point();    // Col(x) and row(y) that trigger a popup
        private Point cellHighlight = new Point(-1,-1); // cell under cursor on right click
        private PropertyChangeListener editableSelectionListener =
                EventHandler.create(PropertyChangeListener.class,this,
                "onEditableSelectionChange","newValue");
        private ActionCommands popupActions = new ActionCommands();
        private DataSource dataSource;
        private DataManager dataManager;

        /**
         * Constructor
         * @param element Source to read and edit
         */
        public TableEditor(TableEditableElement element, DataManager dataManager) {
                super(new BorderLayout());
                this.dataManager = dataManager;
                this.dataSource = dataManager.getDataSource();
                //Add a listener to the source manager to close the table when
                //the source is removed
                this.tableEditableElement = element;
                dockingPanelParameters = new DockingPanelParameters();
                dockingPanelParameters.setTitleIcon(OrbisGISIcon.getIcon("openattributes"));
                dockingPanelParameters.setDefaultDockingLocation(
                        new DockingLocation(DockingLocation.Location.STACKED_ON, "map_editor"));
                tableScrollPane = new JScrollPane(makeTable());
                add(tableScrollPane,BorderLayout.CENTER);
                updateTitle();
        }

        private List<Action> getDockActions() {
                List<Action> actions = new LinkedList<>();
                /*
                TODO Edition
                if(tableEditableElement.getDataSource().isEditable()) {
                        try {
                                actions.add(new ActionAddColumn(tableEditableElement));
                                actions.add(new ActionAddRow(tableEditableElement));
                                actions.add(new ActionRemoveRow(tableEditableElement));
                                actions.add(new ActionCancel(tableEditableElement));
                                actions.add(new ActionUndo(tableEditableElement));
                                actions.add(new ActionRedo(tableEditableElement));
                                actions.add(new ActionSave(tableEditableElement));
                                actions.add(new ActionEdition(tableEditableElement));
                        } catch (UnsupportedOperationException ex) {
                                LOGGER.error(ex.getLocalizedMessage(),ex);
                        }
                }
                */
                return actions;
        }

        /**
         * The editable selection has been updated,
         * propagate in the table if necessary
         * @param newValue 
         */
        public void onEditableSelectionChange(Set<Integer> newValue) {
                if (!onUpdateEditableSelection.getAndSet(true)) {
                        try {
                                setRowSelection(newValue);
                        } finally {
                                onUpdateEditableSelection.set(false);
                        }
                }
        }

        /**
         * The popup is destroyed, the cell border need to be removed
         */
        public void onPopupBecomeInvisible() {
                cellHighlight.setLocation(-1, -1);
        }
        /**
         * The popup is shown, the cell border need to be set
         */
        public void onPopupBecomeVisible() {
                cellHighlight.setLocation(popupCellAdress);
        }
        private JComponent makeFilterManager() {
                JPanel filterComp = filterManager.makeFilterPanel(false);
                filterManager.setUserCanRemoveFilter(false);
                FieldsContainsFilterFactory factory = new FieldsContainsFilterFactory(table);
                filterManager.registerFilterFactory(factory);
                // SQL Filter is only available if there is a primary key
                try(Connection connection = dataSource.getConnection()) {
                    int idPk = JDBCUtilities.getIntegerPrimaryKey(connection.getMetaData(), tableEditableElement.getTableReference());
                    if(idPk > 0) {
                        filterManager.registerFilterFactory(new WhereSQLFilterFactory());
                    }
                } catch (SQLException ex) {
                    LOGGER.error(ex.getLocalizedMessage(), ex);
                }
                filterManager.addFilter(factory.getDefaultFilterValue());
                filterManager.getEventFilterChange().addListener(this, EventHandler.create(FilterFactoryManager.FilterChangeListener.class, this, "onApplySelectionFilter"));
                return filterComp;
        }
                
        /**
         * Apply the active search filters
         */
        public void onApplySelectionFilter() {
                List<TableSelectionFilter> filters = filterManager.getFilters();
                if(!filterRunning.getAndSet(true)) {
                        BackgroundManager bm = Services.getService(BackgroundManager.class);
                        bm.nonBlockingBackgroundOperation(new SearchJob(filters.get(0), table, tableEditableElement,filterRunning));
                } else {
                        LOGGER.info(I18N.tr("Searching request is already launched. Please wait a moment, or cancel it."));
                }
        }

        /**
         * Reload filter GUI components
         */
        private void reloadFilters() {
                LOGGER.debug("Reload Filter");
                DefaultActiveFilter currentFilter = filterManager.getFilterValues().iterator().next();
                filterManager.clearFilters();
                filterManager.addFilter(currentFilter);
        }
        private JComponent makeTable() {
                table = new JTable();
                table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                table.setAutoCreateColumnsFromModel(false);
                table.addMouseListener(EventHandler.create(MouseListener.class,
                        this,
                        "onMouseActionOnTableCells",
                        ""));
                
                table.getTableHeader().addMouseListener(EventHandler.create(MouseListener.class,
                        this,
                        "onMouseActionOnTableHeader",
                        ""));
                        
                        
                table.getSelectionModel().setSelectionMode(
                        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                table.getTableHeader().setReorderingAllowed(false);
                table.setColumnSelectionAllowed(true);
                //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
                table.setFillsViewportHeight(true);       
                table.setUpdateSelectionOnSort(true);
                table.setDragEnabled(true);
                table.setBackground(this.getBackground());
                return table;
        }
        
        public void onMouseActionOnTableHeader(MouseEvent e) {
                //Does this action correspond to a popup request
                if (e.isPopupTrigger()) { 
                        int col = table.columnAtPoint(e.getPoint());
                        popupCellAdress.setLocation(col,-1);
                        JPopupMenu menu = makeTableHeaderPopup(col);
                        menu.show(e.getComponent(), e.getX(), e.getY());
                }
        }

        public void onMouseActionOnTableCells(MouseEvent e) {
                //Does this action correspond to a popup request
                if (e.isPopupTrigger()) { 
                        int row = table.rowAtPoint(e.getPoint());
                        int col = table.columnAtPoint(e.getPoint());
                        popupCellAdress.setLocation(col,row);
                        JPopupMenu menu = makeTableCellPopup();
                        menu.addPopupMenuListener(EventHandler.create(PopupMenuListener.class, this, "onPopupBecomeInvisible",null,"popupMenuWillBecomeInvisible"));
                        menu.addPopupMenuListener(EventHandler.create(PopupMenuListener.class, this, "onPopupBecomeVisible",null,"popupMenuWillBecomeVisible"));
                        menu.show(e.getComponent(), e.getX(), e.getY());
                }
        }
        
        private JPopupMenu makeTableCellPopup() {
                JPopupMenu pop = new JPopupMenu();
                boolean hasSelectedRows = table.getSelectedRowCount()>0;
                if(hasSelectedRows) {
                        JMenuItem addRowFilter = new JMenuItem(I18N.tr("Filter selected rows"));
                        addRowFilter.setToolTipText(I18N.tr("Show only the selected rows"));
                        addRowFilter.addActionListener(
                                EventHandler.create(ActionListener.class,
                                this,"onMenuFilterRows"));
                        pop.add(addRowFilter);
                }
                if(tableSorter.isFiltered()) {
                        JMenuItem removeRowFilter = new JMenuItem(
                                I18N.tr("Clear row filter"));
                        removeRowFilter.setToolTipText(I18N.tr("Show all rows"));
                        removeRowFilter.addActionListener(
                                EventHandler.create(ActionListener.class,
                                this,"onMenuClearFilter"));
                        pop.add(removeRowFilter);
                }
                if(hasSelectedRows || tableSorter.isFiltered()) {
                        pop.addSeparator();
                }
                if(hasSelectedRows) {
                        JMenuItem deselectAll = new JMenuItem(
                                I18N.tr("Clear selection"),OrbisGISIcon.getIcon("edit-clear"));
                        deselectAll.setToolTipText(I18N.tr("Deselect all lines"));
                        deselectAll.addActionListener(
                                EventHandler.create(ActionListener.class,
                                this,"onMenuClearSelection"));
                        pop.add(deselectAll);
                        JMenuItem inverseSelection = new JMenuItem(
                                I18N.tr("Reverse selection"),
                                OrbisGISIcon.getIcon("arrow_refresh"));
                        inverseSelection.setToolTipText(I18N.tr("Reverse the current selection"));
                        inverseSelection.addActionListener(
                                EventHandler.create(ActionListener.class,
                                this,"onMenuReverseSelection"));
                        pop.add(inverseSelection);
                        
                        JMenuItem createDataSourceSelection = new JMenuItem(
                                I18N.tr("Create datasource from selection"),
                                OrbisGISIcon.getIcon("table_go"));
                        createDataSourceSelection.setToolTipText(
                                I18N.tr("Create a datasource from the current selection"));
                        createDataSourceSelection.addActionListener(
                                EventHandler.create(ActionListener.class,
                                this, "onCreateDataSourceFromSelection"));
                        pop.add(createDataSourceSelection);
                        
                        
                        if(isDataOnShownMapContext()) {
                                JMenuItem zoomToSelection = new JMenuItem(
                                        I18N.tr("Zoom to selection"),
                                OrbisGISIcon.getIcon("zoom_selected"));
                                zoomToSelection.setToolTipText(I18N.tr("In the map editor, zoom to the selected rows"));
                                zoomToSelection.addActionListener(
                                        EventHandler.create(ActionListener.class,
                                        this,"onMenuZoomToSelection"));
                                pop.add(zoomToSelection);
                        }
                        
                }
                JMenuItem findSameCells = new JMenuItem(
                        I18N.tr("Select same cell"),OrbisGISIcon.getIcon("selectsame_row"));
                findSameCells.setToolTipText(I18N.tr("Select all rows that match this cell value"));
                findSameCells.addActionListener(
                        EventHandler.create(ActionListener.class,
                        this,"onMenuSelectSameCellValue"));
                pop.add(findSameCells);
                popupActions.copyEnabledActions(pop);
                return pop;
        }

        /**
         * Used by the function "Zoom to selection"
         * This menu is shown only if the current data is loaded and shown in the toc
         */
        private boolean isDataOnShownMapContext() {
                EditorManager editorManager = Services.getService(EditorManager.class);
                for(EditableElement editable : editorManager.getEditableElements()) {
                        if(editable instanceof MapElement) {
                                MapElement mapEditable = (MapElement)editable;
                                MapContext mapContext = mapEditable.getMapContext();
                                for(ILayer layer : mapContext.getLayers()) {
                                        if(layer.isVisible()) {
                                                if(layer.getTableReference().equals(tableEditableElement.getTableReference())) {
                                                        return true;
                                                }
                                        }
                                }
                        }
                }
                return false;
        }

        public void onMenuZoomToSelection() {
                int[] viewSelection = table.getSelectedRows();
                if(viewSelection.length==0) {
                        return;
                }
                int[] modelSelection = new int[viewSelection.length];
                for(int i=0;i<viewSelection.length;i++) {
                        modelSelection[i] = table.convertRowIndexToModel(viewSelection[i]);
                }
                //Retrieve the MapContext
                MapContext mapContext=null;
                EditorManager editorManager = Services.getService(EditorManager.class);
                for(EditableElement editable : editorManager.getEditableElements()) {
                        if(editable instanceof MapElement) {
                                MapElement mapEditable = (MapElement)editable;
                                mapContext = mapEditable.getMapContext();
                                break;
                        }
                }
                if(mapContext==null) {
                        //Programmation error, useless to translate
                        LOGGER.error("MapContext lost between popup creation and click");
                        return;
                }                
                ZoomToSelectionJob zoomJob = new ZoomToSelectionJob(dataManager, tableEditableElement.getTableReference()
                        ,new IntegerUnion(modelSelection), mapContext);
                launchJob(zoomJob);                
        }
        
        /**
         * Show all rows of the data source (remove the filter)
         */
        public void onMenuClearFilter() {
                tableSorter.setRowsFilter(null);
        }
        /**
         * Invert the current table selection
         */
        public void onMenuReverseSelection() {
                IntegerUnion invertedSelection = new IntegerUnion();
                for(int viewId = 0; viewId<table.getRowCount();viewId++) {
                        if(!table.isRowSelected(viewId)) {
                                invertedSelection.add(viewId);
                        }
                }                
                setViewRowSelection(invertedSelection);
        }

        /**
         * The user can export the selected rows into a new datasource
         */
        public void onCreateDataSourceFromSelection() {
            Set<Integer> selection = getTableModelSelection();
            // If there is a nonempty selection, then ask the user to name it.
            if (!selection.isEmpty()) {
                try {
                    String newName = CreateSourceFromSelection.showNewNameDialog(
                            this, dataSource, tableEditableElement.getTableReference());
                    // If newName is not null, then the user clicked OK and entered
                    // a valid name.
                    if (newName != null) {
                        BackgroundManager bm = Services.getService(BackgroundManager.class);
                        bm.backgroundOperation(
                                new CreateSourceFromSelection(
                                        dataSource,
                                        selection, tableEditableElement.getTableReference(), newName));
                    }
                } catch (SQLException ex) {
                    LOGGER.error(ex.getLocalizedMessage(), ex);
                }
            }
        }

        /**
         * Select all rows that have the same value of the selected cell
         */
        public void onMenuSelectSameCellValue() {
                int viewColId = popupCellAdress.x;
                int viewRowId = popupCellAdress.y;
                int colId = table.convertColumnIndexToModel(viewColId);
                int rowId = table.convertRowIndexToModel(viewRowId);
                //
                //Build the appropriate search filter
                String cellValue = tableModel.getValueAt(rowId, colId).toString();
                DefaultActiveFilter filter = new FieldsContainsFilterFactory.
                        FilterParameters(colId, cellValue, true, true);
                //Clear current filter
                filterManager.clearFilters();
                //Add the find filter
                filterManager.addFilter(filter);
                //Trigger the filter job
                onApplySelectionFilter();
                
        }
        
        /**
         * Clear the table selection
         */
        public void onMenuClearSelection() {
                table.clearSelection();
        }
        
        /**
         * Show only selected rows
         */
        public void onMenuFilterRows() {
                IntegerUnion selectedModelIndex = getTableModelSelection();
                tableSorter.setRowsFilter(selectedModelIndex);
        }
        
        /**
         * Create the popup menu of the table header
         * @param col
         * @return 
         */
        private JPopupMenu makeTableHeaderPopup(Integer col) {
                JPopupMenu pop = new JPopupMenu();
                //Optimal width
                JMenuItem optimalWidth = 
                        new JMenuItem(I18N.tr("Optimal width"),
                        OrbisGISIcon.getIcon("text_letterspacing")
                        );
                optimalWidth.addActionListener(
                        EventHandler.create(ActionListener.class,this,
                        "onMenuOptimalWidth"));
                pop.add(optimalWidth);
                // Additionnal functions for specific columns
                boolean isGeometryField = false;
                try(Connection connection = dataSource.getConnection()) {
                    List<String> geomFields = SFSUtilities.getGeometryFields(connection, TableLocation.parse(tableEditableElement.getTableReference()));
                    ResultSetMetaData meta =  tableEditableElement.getRowSet().getMetaData();
                    for(String geomField : geomFields) {
                        int gIndex = JDBCUtilities.getFieldIndex(meta, geomField);
                        if(col.equals(gIndex - 1)) {
                            isGeometryField = true;
                        }
                    }
                } catch (SQLException | EditableElementException ex ){
                    LOGGER.error(ex.getLocalizedMessage(), ex);
                }
                if (!isGeometryField) {
                        pop.addSeparator();
                        //Sort Ascending
                        JMenuItem sortAscending =
                                new JMenuItem(I18N.tr("Sort ascending"),
                                UIManager.getIcon("Table.ascendingSortIcon")
                                );
                        sortAscending.addActionListener(
                        EventHandler.create(ActionListener.class,this,
                        "onMenuSortAscending"));
                        pop.add(sortAscending);
                        //Sort Descending
                        JMenuItem sortDescending =
                                new JMenuItem(I18N.tr("Sort descending"),
                                UIManager.getIcon("Table.descendingSortIcon")
                                );
                        sortDescending.addActionListener(
                        EventHandler.create(ActionListener.class,this,
                        "onMenuSortDescending"));
                        pop.add(sortDescending);
                        //No sort
                        JMenuItem noSort =
                                new JMenuItem(I18N.tr("No sort"),
                                OrbisGISIcon.getIcon("table_refresh")
                                );
                        noSort.addActionListener(
                        EventHandler.create(ActionListener.class,this,
                        "onMenuNoSort"));
                        pop.add(noSort);
                }
                pop.addSeparator();
                //Get Field informations
                JMenuItem showFieldInformations =
                        new JMenuItem(I18N.tr("Show column informations"),
                        OrbisGISIcon.getIcon("information")
                        );
                showFieldInformations.addActionListener(
                EventHandler.create(ActionListener.class,this,
                "onMenuShowInformations"));
                pop.add(showFieldInformations);                
                if(isNumeric(col)) {
                        //Get Statistics
                        String text = I18N.tr("Show column statistics");
                        if(table.getSelectedRowCount()>0) {
                                text = I18N.tr("Show column selection statistics");
                        } else if(tableSorter.isFiltered()) {
                                text = I18N.tr("Show filtered column statistics");                                
                        }
                        JMenuItem showStats =                         
                                new JMenuItem(text,
                                OrbisGISIcon.getIcon("sum")
                                );
                        showStats.addActionListener(
                        EventHandler.create(ActionListener.class,this,
                        "onMenuShowStatistics"));
                        pop.add(showStats);                                
                        
                }
                popupActions.copyEnabledActions(pop);
                return pop;

        }
        
        private void launchJob(BackgroundJob job) {
                Services.getService(BackgroundManager.class).nonBlockingBackgroundOperation(job);
        }
        
        /**
         * The user disable table sort
         */
        public void onMenuNoSort() {
                tableSorter.setSortKeys(null);
        }
        /**
         * Ascending sort
         */
        public void onMenuSortAscending() {
                tableSorter.setSortKey(new SortKey(popupCellAdress.x,SortOrder.ASCENDING));
        }
        /**
         * Descending sort
         */
        public void onMenuSortDescending() {
                tableSorter.setSortKey(new SortKey(popupCellAdress.x,SortOrder.DESCENDING));                
        }

        /**
         * Show the selected field informations
         */
        public void onMenuShowInformations() {
            int col = popupCellAdress.x + 1;
            try(Connection connection = dataSource.getConnection()) {
                String colName,typeName;
                DatabaseMetaData meta = connection.getMetaData();
                TableLocation table = TableLocation.parse(tableEditableElement.getTableReference());
                StringBuilder infos = new StringBuilder();
                try(ResultSet rs = meta.getColumns(table.getCatalog(), table.getSchema(), table.getTable(), null)) {
                    while (rs.next()) {
                        if(rs.getInt("ORDINAL_POSITION") == col) {
                            infos.append(I18N.tr("\nField name :\t{0}\n",rs.getString("COLUMN_NAME")));
                            infos.append(I18N.tr("Field type :\t{0}\n",rs.getString("TYPE_NAME")));
                            String remarks = rs.getString("REMARKS");
                            if(remarks != null && !remarks.isEmpty()) {
                                infos.append(I18N.tr("Field remarks :\t{0}\n",remarks));
                            }
                            break;
                        }
                    }
                }
                infos.append(I18N.tr("Constraints :\n"));
                try(ResultSet rs = meta.getIndexInfo(table.getCatalog(), table.getSchema(), table.getTable(), true, false)) {
                    while (rs.next()) {
                        if(rs.getInt("ORDINAL_POSITION") == col) {
                            String filter = rs.getString("FILTER_CONDITION");
                            if(filter != null && !filter.isEmpty()) {
                                infos.append(I18N.tr("\t{0} :\t{1}\n",
                                       rs.getString("INDEX_NAME"),filter));
                            }
                        }
                    }
                }
                LOGGER.info(infos.toString());
            } catch( SQLException ex) {
                LOGGER.error(ex.getLocalizedMessage(),ex);
            }
        }

    private IntegerUnion getTableModelSelection() {
                IntegerUnion selectionModelRowId = new IntegerUnion();
                for (int viewRowId : table.getSelectedRows()) {
                        selectionModelRowId.add(tableSorter.convertRowIndexToModel(viewRowId));
                }
                return selectionModelRowId;
        }

        /**
         * Compute and show the selected field statistics
         */
        public void onMenuShowStatistics() {
                //Compute row id selection
                Set<Integer> selectionModelRowId = getTableModelSelection();
                if (selectionModelRowId.isEmpty() && tableSorter.isFiltered()) {
                        selectionModelRowId.addAll(tableSorter.getViewToModelIndex());
                }
                launchJob(new ComputeFieldStatistics(selectionModelRowId, dataSource, popupCellAdress.x, tableEditableElement.getTableReference()));
        }

        /**
         * Compute the optimal width for this column
         */
        public void onMenuOptimalWidth() {
                launchJob(new OptimalWidthJob(table, popupCellAdress.x));
        }

        /**
         * Return the editable document
         * @return 
         */
        @Override
        public EditableSource getTableEditableElement() {
                return tableEditableElement;
        }

        @Override
        public boolean match(EditableElement editableElement) {
                //TODO Link table selection with layer selection
                return false; //This editor cannot take another editable
        }

        @Override
        public void addNotify() {
                super.addNotify();
                if(!initialised.getAndSet(true)) {
                        launchJob(new OpenEditableElement(this, tableEditableElement));
                }
        }
        
        private void quickAutoResize() {
                autoResizeColWidth(Math.min(5, tableModel.getRowCount()));
        }
        
        /**
         * When the editable element is open, 
         * the data model of the table can be set.
         * Called only once.
         */
        private void readDataSource() {
                tableModel = new DataSourceTableModel(tableEditableElement);
                tableModel.addTableModelListener(new FieldResetListener());
                table.setModel(tableModel);
                updateTableColumnModel();
                quickAutoResize();
                tableSorter = new DataSourceRowSorter(tableModel, dataSource);
                tableSorter.addRowSorterListener(
                        EventHandler.create(RowSorterListener.class,this,
                        "onShownRowsChanged"));
                table.setRowSorter(tableSorter);
                //Set the row count at left
                tableRowHeader = new TableRowHeader(table);
                tableScrollPane.setRowHeaderView(tableRowHeader);
                //Apply the selection
                setRowSelection(new IntegerUnion(tableEditableElement.getSelection()));
                table.getSelectionModel().addListSelectionListener(
                        EventHandler.create(ListSelectionListener.class,this,
                        "onTableSelectionChange",""));
                add(makeFilterManager(),BorderLayout.SOUTH);
                //Close the editable element on window close
                dockingPanelParameters.addPropertyChangeListener(
                        DockingPanelParameters.PROP_VISIBLE,
                        EventHandler.create(PropertyChangeListener.class,
                        this,"onChangeVisibility","newValue"));
                updateTitle();
                // Add a selection listener on the editable element
                tableEditableElement.addPropertyChangeListener(TableEditableElement.PROP_SELECTION,
                        editableSelectionListener);
                dockingPanelParameters.setDockActions(getDockActions());
                initPopupActions();
        }
        private void initPopupActions() {
                // TODO Edition
                // popupActions.addAction(new ActionRemoveColumn(this));
        }
        /**
         * Frame visibility state change
         * @param visible 
         */
        public void onChangeVisibility(boolean visible) {
                if(!visible) {                        
                        tableModel.dispose();
                        for(Action action : dockingPanelParameters.getDockActions()) {
                            if(action instanceof ActionDispose){
                                try {
                                    ((ActionDispose) action).dispose();
                                }catch (Exception ex) {
                                    LOGGER.error(ex.getLocalizedMessage(),ex);
                                }
                            }
                        }
                        try {
                                LOGGER.debug("Close table "+dockingPanelParameters.getTitle());
                                tableEditableElement.close(new NullProgressMonitor());                                
                                tableEditableElement.removePropertyChangeListener(editableSelectionListener);
                        } catch (UnsupportedOperationException | EditableElementException ex) {
                                LOGGER.error(ex.getLocalizedMessage(),ex);
                        }
                }
        }
        /**
         * Convert index from model to view then update the table selection
         * @param modelSelection ModelIndex selection
         */
        private void setRowSelection(Set<Integer> modelSelection) {
                Set<Integer> newSelection;
                if(tableSorter.isFiltered() || !tableSorter.getSortKeys().isEmpty()) {
                        newSelection = new IntegerUnion();
                        for(Integer modelId : modelSelection) {
                                int viewRowId = table.convertRowIndexToView(modelId);
                                if(viewRowId!=-1) {
                                        newSelection.add(viewRowId);
                                }
                        }
                } else {
                        newSelection = modelSelection;
                }
                setViewRowSelection(newSelection);
        }
        
        /**
         * Update the table selection
         * @param viewSelection View index selection
         */
        private void setViewRowSelection(Set<Integer> viewSelection) {
                // Integer union is able to compute range of integer from a set of integer
                Iterator<Integer> intervals = new IntegerUnion(viewSelection).getValueRanges().iterator();
                final int maxRow = table.getRowCount();
                try {
                        table.getSelectionModel().setValueIsAdjusting(true);
                        table.clearSelection();
                        while(intervals.hasNext()) {
                                // If the DataSource here and in other editors is not the same (uncommitted changes)
                                // Then the selected row index may not be the same and can be out of range.
                                // The check is done here.
                                int begin = intervals.next();
                                int end = Math.min(intervals.next(),maxRow - 1);
                                if(begin < maxRow) {
                                    table.addRowSelectionInterval(begin, end);
                                }
                        }
                }finally {
                        table.getSelectionModel().setValueIsAdjusting(false);
                }
                
        }
        
        /**
         * The model or the sorted have updated the table
         */
        public void onShownRowsChanged() {
                updateTitle();
                tableRowHeader.tableChanged();
        }
        
        /**
         * Table selection change
         * @param evt Selection event, used to test if the selection is final
         */
        public void onTableSelectionChange(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                        updateTitle();
                        if (!onUpdateEditableSelection.getAndSet(true)) {
                                SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                                try {
                                                        updateEditableSelection();
                                                } finally {
                                                        onUpdateEditableSelection.set(false);

                                                }
                                        }
                                });
                        }
                }
        }

        private void updateEditableSelection() {
                try {
                        tableEditableElement.setSelection(getTableModelSelection());
                } finally {
                        onUpdateEditableSelection.set(false);
                }
        }

        /**
         *  Update the title label.
         */
        private void updateTitle() {
                String sourceName = tableEditableElement.getTableReference();
                int tableSelectedRowCount = table.getSelectedRowCount();
                int tableRowCount = table.getRowCount();
                // Message is different if the table is filtered
                if(tableSorter==null || !tableSorter.isFiltered()) {
                        dockingPanelParameters.setTitle(
                                I18N.tr("Table Editor of {0} {1}/{2}",
                                sourceName,tableSelectedRowCount,tableRowCount));
                }else{
                        dockingPanelParameters.setTitle(
                                I18N.tr("Table Editor of {0} (Filtered) {1}/{2}",
                                sourceName,tableSelectedRowCount,tableRowCount));
                }                
        }
        
        private void resetRenderers() {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        TableColumn col = table.getColumnModel().getColumn(i);
                        if (isNumeric(i)) {
                                col.setCellRenderer(new TableNumberColumnRenderer(table,cellHighlight));
                        } else {
                                col.setCellRenderer(new TableDefaultColumnRenderer(table,tableModel.getColumnClass(i),cellHighlight));
                        } 
                }                
        }

        private void autoResizeColWidth(int rowsToCheck) {
                TableColumnModel colModel = table.getColumnModel();
                int maxWidth = 200;
                for (int i = 0; i < colModel.getColumnCount(); i++) {
                        TableColumn col = colModel.getColumn(i);
                        int colWidth = OptimalWidthJob.getColumnOptimalWidth(table, rowsToCheck, maxWidth, i,
                                        new NullProgressMonitor());
                        col.setPreferredWidth(colWidth);
                }
                resetRenderers();
        }

        /**
         * Sync the table column model with the DataSource
         */
        private void updateTableColumnModel() {
                TableColumnModel colModel = new DefaultTableColumnModel();
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        TableColumn col = new TableColumn(i);
                        String columnName = tableModel.getColumnName(i);
                        col.setHeaderValue(columnName);
                        colModel.addColumn(col);
                }
                table.setColumnModel(colModel);
        }

        /**
         * @param column Column index
         * @return True if the field type is numeric
         */
        private boolean isNumeric(int column) {
                try {
                    int columnType = tableModel.getColumnType(column);
                    switch (columnType) {
                            case Types.FLOAT:
                            case Types.DOUBLE:
                            case Types.TINYINT:
                            case Types.SMALLINT:
                            case Types.INTEGER:
                            case Types.BIGINT:
                            case Types.DECIMAL:
                            case Types.NUMERIC:
                                    return true;
                            default:
                                    return false;
                    }
                }catch (SQLException ex) {
                    LOGGER.error(ex.getLocalizedMessage(), ex);
                    return false;
                }
        }

        @Override
        public EditableElement getEditableElement() {
                return tableEditableElement;
        }

        @Override
        public void setEditableElement(EditableElement editableElement) {
                
        }

        @Override
        public DockingPanelParameters getDockingParameters() {
                return dockingPanelParameters;
        }

        @Override
        public JComponent getComponent() {
                return this;
        }
        
        private static class OpenEditableElement implements BackgroundJob {

                private TableEditor tableEditor;
                private TableEditableElement tableEditableElement;

                private OpenEditableElement(TableEditor tableEditor, TableEditableElement el) {
                    this.tableEditor = tableEditor;
                    this.tableEditableElement = el;
                }

                @Override
                public void run(ProgressMonitor pm) {
                        try {
                                tableEditableElement.open(pm);
                        } catch (UnsupportedOperationException | EditableElementException ex) {
                                LOGGER.error(I18N.tr("Error while loading the table editor"), ex);
                        }
                    if(SwingUtilities.isEventDispatchThread()) {
                        tableEditor.readDataSource();
                    } else {
                        SwingUtilities.invokeLater(new ReadDataSource(tableEditor));
                    }
                }

                @Override
                public String getTaskName() {
                        return I18N.tr("Open the table {0}", tableEditableElement.getTableReference());
                }
        }
        private static class ReadDataSource implements Runnable {
            private TableEditor tableEditor;

            private ReadDataSource(TableEditor tableEditor) {
                this.tableEditor = tableEditor;
            }

            @Override
            public void run() {
                tableEditor.readDataSource();
            }
        }

        @Override
        public JTable getTable() {
                return table;
        }

        @Override
        public Point getPopupCellAdress() {
                return new Point(popupCellAdress);
        }

        private class FieldResetListener implements TableModelListener {
                @Override
                public void tableChanged(TableModelEvent tableModelEvent) {
                        if(tableModelEvent.getFirstRow() == TableModelEvent.HEADER_ROW) {
                                updateTableColumnModel();
                                reloadFilters();
                                resetRenderers();
                        }
                }
        }
}
