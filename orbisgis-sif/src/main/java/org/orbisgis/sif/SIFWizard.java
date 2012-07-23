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
package org.orbisgis.sif;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

public class SIFWizard extends AbstractOutsideFrame {

        private JPanel wizardButtons;
        private JButton btnPrevious;
        private JButton btnNext;
        private JButton btnFinish;
        private JButton btnCancel;
        private JPanel mainPanel;
        private SimplePanel[] panels;
        private int index = 0;
        private CardLayout layout = new CardLayout();

        public SIFWizard(Window owner) {
                super(owner);
                init();
        }

        private void init() {
                this.setLayout(new BorderLayout());

                this.add(getWizardButtons(), BorderLayout.SOUTH);              

                this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        private JPanel getWizardButtons() {
                if (wizardButtons == null) {
                        wizardButtons = new JPanel();
                        wizardButtons.add(getBtnPrevious());
                        wizardButtons.add(getBtnNext());
                        wizardButtons.add(getBtnFinish());
                        wizardButtons.add(getBtnCancel());
                }

                return wizardButtons;
        }

        private void buildMainPanel(SimplePanel[] panels) {
                mainPanel = new JPanel();
                mainPanel.setLayout(layout);

                for (int i = 0; i < panels.length; i++) {
                        mainPanel.add(panels[i], Integer.toString(i));
                }
        }

        public JButton getBtnPrevious() {
                if (btnPrevious == null) {
                        btnPrevious = new JButton(I18N.tr("Previous"));
                        btnPrevious.setBorderPainted(false);
                        btnPrevious.setEnabled(false);
                        btnPrevious.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        index--;
                                        layout.previous(mainPanel);
                                }
                        });
                }

                return btnPrevious;
        }

        public JButton getBtnNext() {
                if (btnNext == null) {
                        btnNext = new JButton(I18N.tr("Next"));
                        btnNext.setBorderPainted(false);
                        btnNext.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        if (validateInput()) {
                                                index++;
                                                layout.next(mainPanel);
                                                setDefaultButton();
                                        }
                                }
                        });
                }

                return btnNext;
        }

        private void setDefaultButton() {
                if (index == panels.length - 1) {
                        getRootPane().setDefaultButton(btnFinish);
                } else {
                        getRootPane().setDefaultButton(btnNext);
                }
        }

        public JButton getBtnFinish() {
                if (btnFinish == null) {
                        btnFinish = new JButton(I18N.tr("Finish"));
                        btnFinish.setBorderPainted(false);
                        btnFinish.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        exit(true);
                                }
                        });
                }

                return btnFinish;
        }

        public JButton getBtnCancel() {
                if (btnCancel == null) {
                        btnCancel = new JButton(I18N.tr("Cancel"));
                        btnCancel.setBorderPainted(false);
                        btnCancel.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        exit(false);
                                }
                        });
                }

                return btnCancel;
        }

        public void setComponent(SimplePanel[] panels) {
                this.panels = panels;
                this.index = 0;
                buildMainPanel(panels);
                this.add(mainPanel, BorderLayout.CENTER);             
                this.setIconImage(getSimplePanel().getIconImage());
                setDefaultButton();
        }

       

        @Override
        protected SimplePanel getSimplePanel() {
                return panels[index];
        }

        
}
