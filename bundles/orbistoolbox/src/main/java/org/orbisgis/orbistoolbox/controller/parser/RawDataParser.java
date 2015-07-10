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

import org.orbisgis.orbistoolbox.model.*;
import org.orbisgis.orbistoolboxapi.annotations.input.RawDataInput;
import org.orbisgis.orbistoolboxapi.annotations.model.*;
import org.orbisgis.orbistoolboxapi.annotations.output.RawDataOutput;

import java.lang.reflect.Field;
import java.net.URI;

/**
 * Parser dedicated to the RawDataParsing.
 *
 * @author Sylvain PALOMINOS
 **/

public class RawDataParser implements Parser {

    @Override
    public Input parseInput(Field f, String processName) {
        //Instantiate the RawData
        RawData rawData = ObjectAnnotationConverter.annotationToObject(f.getAnnotation(RawDataAttribute.class));
        rawData.setData(f, f.getType());

        //Instantiate the returned input
        Input input = new Input(f.getName(),
                URI.create("orbisgis:wps:"+processName+":input:"+f.getName()),
                rawData);

        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(InputAttribute.class), input);
        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(DescriptionTypeAttribute.class), input);

        return input;
    }

    @Override
    public Output parseOutput(Field f, String processName) {
        //Instantiate the RawData
        RawData rawData = ObjectAnnotationConverter.annotationToObject(f.getAnnotation(RawDataAttribute.class));
        rawData.setData(f, f.getType());

        //Instantiate the returned output
        Output output = new Output(f.getName(),
                URI.create("orbisgis:wps:"+processName+":output:"+f.getName()),
                rawData);

        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(DescriptionTypeAttribute.class), output);

        return output;
    }

    @Override
    public Class getAnnotationInput() {
        return RawDataInput.class;
    }

    @Override
    public Class getAnnotationOutput() {
        return RawDataOutput.class;
    }
}