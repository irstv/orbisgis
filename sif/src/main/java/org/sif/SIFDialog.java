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

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JPanel;

public class SIFDialog extends AbstractOutsideFrame {

	private JButton btnOk;

	private JButton btnCancel;

	private SimplePanel simplePanel;

	private boolean test;

	public SIFDialog(Window owner) {
		super(owner);
		init();
	}

	private void init() {
		this.setLayout(new BorderLayout());

		btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				exit(true);
			}

		});
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				exit(false);
			}

		});
		JPanel pnlButtons = new JPanel();
		pnlButtons.add(btnOk);
		pnlButtons.add(btnCancel);

		this.add(pnlButtons, BorderLayout.SOUTH);

		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				if (test) {
					exit(true);
				} else {
					if (btnOk.isEnabled()) {
						btnOk.requestFocus();
					} else {
						btnCancel.requestFocus();
					}
				}
			}

		});

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void setComponent(SimplePanel simplePanel,
			HashMap<String, String> inputs) {
		this.simplePanel = simplePanel;
		this.add(simplePanel, BorderLayout.CENTER);
		listen(this);
		loadInput(inputs);
		getPanel().initialize();
		this.setIconImage(getPanel().getIconImage());
	}

	public void canContinue() {
		btnOk.setEnabled(true);
	}

	public void cannotContinue() {
		btnOk.setEnabled(false);
	}

	@Override
	protected SimplePanel getPanel() {
		return simplePanel;
	}

	@Override
	protected void saveInput() {
		simplePanel.saveInput();
	}

	protected void loadInput(HashMap<String, String> inputs) {
		if (simplePanel.loadInput(inputs)) {
			test = true;
		} else {
			test = false;
		}
	}

}
