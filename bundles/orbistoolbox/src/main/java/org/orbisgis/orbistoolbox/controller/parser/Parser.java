/**
 * OrbisToolBox is an OrbisGIS plugin dedicated to create and manage processing.
 *
 * OrbisToolBox is distributed under GPL 3 license. It is produced by CNRS <http://www.cnrs.fr/> as part of the
 * MApUCE project, funded by the French Agence Nationale de la Recherche (ANR) under contract ANR-13-VBDU-0004.
 *
 * OrbisToolBox is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * OrbisToolBox is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OrbisToolBox. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/> or contact directly: info_at_orbisgis.org
 */

package org.orbisgis.orbistoolbox.controller.parser;

import org.orbisgis.orbistoolbox.model.Input;
import org.orbisgis.orbistoolbox.model.Output;

import java.lang.reflect.Field;

/**
 * Interface to define a Parser associated to a data from the model (i.e. LiteralData or RawData).
 * A parse have to be associated to an input and an output groovy annotation used in the script.
 * It have to be also associated to a Data class from the model.
 *
 * @author Sylvain PALOMINOS
 **/

public interface Parser {

    /**
     * Parse the given field as an input and returns the corresponding DataDescription.
     * @param f Field to parse.
     * @param processId The process identifier.
     * @return Parsed DataDescription.
     */
    Input parseInput(Field f, String processId);

    /**
     * Parse the given field as an output and returns the corresponding DataDescription.
     * @param f Field to parse.
     * @param processId The process identifier.
     * @return Parsed DataDescription.
     */
    Output parseOutput(Field f, String processId);

    /**
     * Returns the groovy annotation associated to this parser.
     * @return The grovvy annotation associated to this parse.
     */
    Class getAnnotation();

}
