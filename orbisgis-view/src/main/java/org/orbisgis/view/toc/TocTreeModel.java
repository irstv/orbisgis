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
package org.orbisgis.view.toc;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.view.components.resourceTree.AbstractTreeModel;

public class TocTreeModel extends AbstractTreeModel {

	private final ILayer root;

	public TocTreeModel(ILayer root, JTree tree) {
		super(tree);
		this.root = root;
	}

	public void refresh() {
		fireEvent();
	}

	@Override
	public Object getChild(Object parent, int index) {
		ILayer l = (ILayer) parent;
		if (l.acceptsChilds()) {
			return l.getChildren()[index];
		} else {
			return l.getStyle(index);
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof ILayer) {
			ILayer layer = (ILayer) parent;
			if (layer.acceptsChilds()) {
				return layer.getChildren().length;
			} else {
				return layer.getStyles().size();
			}
		} else {
			return 0;
		}
	}

        @Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof ILayer) {
			if (child instanceof Style) {
				return ((ILayer) parent).indexOf((Style)child);
			} else {
				return ((ILayer) parent).getIndex((ILayer) child);
			}
		} else {
			return 0;
		}
	}

        @Override
	public Object getRoot() {
		return root;
	}

        @Override
	public boolean isLeaf(Object node) {
		if (node instanceof ILayer) {
			ILayer layer = (ILayer) node;
			return layer.acceptsChilds() && (layer.getChildren().length == 0);
		} else {
			return true;
		}
	}

        @Override
	public void valueForPathChanged(TreePath path, Object newValue) {
	}

}
