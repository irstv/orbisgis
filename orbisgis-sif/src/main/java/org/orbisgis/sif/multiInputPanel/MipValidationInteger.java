/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.orbisgis.sif.multiInputPanel;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * An integer constraint for a specific field
 * @author Nicolas Fortin
 */
public class MipValidationInteger implements MIPValidation {
    private String fieldName;
    private String fieldLabel;
    private Integer minValue = Integer.MIN_VALUE;
    private Integer maxValue = Integer.MAX_VALUE;
    private final I18n i18n = I18nFactory.getI18n(MipValidationInteger.class);

    /**
     * Constructor
     * @param fieldName Field id
     * @param fieldLabel Field label, for error messages
     */
    public MipValidationInteger(String fieldName, String fieldLabel) {
        this.fieldName = fieldName;
        this.fieldLabel = fieldLabel;
    }

    /**
     * Constructor.
     * @param fieldName Field id
     * @param fieldLabel Field label, for error messages
     * @param minValue Minimum field value
     * @param maxValue Maximum field value
     */
    public MipValidationInteger(String fieldName, String fieldLabel, Integer minValue, Integer maxValue) {
        this.fieldName = fieldName;
        this.fieldLabel = fieldLabel;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Set minimum value
     * @param minValue
     */
    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    /**
     * Set maximum value
     * @param maxValue
     */
    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public String validate(MultiInputPanel mid) {
        String value = mid.getInput(fieldName);
        if(value!=null) {
            try {
                int val = Integer.valueOf(value);
                if(val<minValue) {
                    return i18n.tr("The {0} field must be greater or equal than {1}",fieldLabel,minValue);
                } else if(val>maxValue) {
                    return i18n.tr("The {0} field must be lower or equal than {1}",fieldLabel,maxValue);
                }
            } catch (NumberFormatException ex) {
                return i18n.tr("The {0} field must be an integer",fieldLabel);
            }
        }
        return null;
    }
}
