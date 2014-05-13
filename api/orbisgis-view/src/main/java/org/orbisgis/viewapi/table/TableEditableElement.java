package org.orbisgis.viewapi.table;

import org.orbisgis.viewapi.edition.EditableSource;

import java.util.Set;

/**
 * @author Nicolas Fortin
 */
public interface TableEditableElement extends EditableSource {
    // Properties names
    public static final String PROP_SELECTION = "selection";


    /**
     * @return the selected rows in the table
     */
    public Set<Integer> getSelection();

    /**
     * Set the selected geometries in the table
     * @param selection Row's id
     */
    public void setSelection(Set<Integer> selection);
}
