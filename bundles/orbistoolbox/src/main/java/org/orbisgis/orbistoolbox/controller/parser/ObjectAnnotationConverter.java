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

package org.orbisgis.orbistoolbox.controller.parser;

import org.orbisgis.orbistoolbox.model.*;
import org.orbisgis.orbistoolbox.model.Process;
import org.orbisgis.orbistoolboxapi.annotations.model.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Class able to convert annotation into object and object into annotation.
 *
 * @author Sylvain PALOMINOS
 **/

public class ObjectAnnotationConverter {

    public static void annotationToObject(DescriptionTypeAttribute descriptionTypeAttribute,
                                          DescriptionType descriptionType){

        if(!descriptionTypeAttribute.title().equals("")){
            descriptionType.setTitle(descriptionTypeAttribute.title());
        }
        if(!descriptionTypeAttribute.abstrac().equals(DescriptionTypeAttribute.defaultAbstrac)){
            descriptionType.setAbstrac(descriptionTypeAttribute.abstrac());
        }
        if(!descriptionTypeAttribute.identifier().equals(DescriptionTypeAttribute.defaultIdentifier)){
            descriptionType.setIdentifier(URI.create(descriptionTypeAttribute.identifier()));
        }
        if(!descriptionTypeAttribute.keywords().equals(DescriptionTypeAttribute.defaultKeywords)){
            descriptionType.setKeywords(Arrays.asList(descriptionTypeAttribute.keywords().split(",")));
        }
        //TODO : implements for metadata.
        if(!descriptionTypeAttribute.metadata().equals(DescriptionTypeAttribute.defaultMetadata)){
            List<Metadata> metadatas = new ArrayList<>();
            for(MetadataAttribute metadata : descriptionTypeAttribute.metadata()) {
                metadatas.add(ObjectAnnotationConverter.annotationToObject(metadata));
            }
            descriptionType.setMetadata(metadatas);
        }
    }

    public static Format annotationToObject(FormatAttribute formatAttribute){
        Format format = new Format(formatAttribute.mimeType(), URI.create(formatAttribute.schema()));
        format.setDefaultFormat(formatAttribute.isDefaultFormat());
        format.setMaximumMegaBytes(formatAttribute.maximumMegaBytes());
        return format;
    }

    public static Metadata annotationToObject(MetadataAttribute descriptionTypeAttribute){
        URI href = URI.create(descriptionTypeAttribute.href());
        URI role = URI.create(descriptionTypeAttribute.role());
        String title = descriptionTypeAttribute.title();

        return new Metadata(title, role, href);
    }

    public static Values annotationToObject(ValuesAttribute valueAttribute){
        Values value;
        if(valueAttribute.type().equals(ValuesType.VALUE)){
            value = new Value<>(valueAttribute.value());
        }
        else{
            if(valueAttribute.spacing().equals("")) {
                value = new Range(
                                Double.parseDouble(valueAttribute.minimum()),
                                Double.parseDouble(valueAttribute.maximum())
                );
            }
            else{
                value = new Range(
                                Double.parseDouble(valueAttribute.minimum()),
                                Double.parseDouble(valueAttribute.maximum()),
                                Double.parseDouble(valueAttribute.spacing())
                );
            }
        }
        return value;
    }

    public static LiteralDataDomain annotationToObject(LiteralDataDomainAttribute literalDataDomainAttribute){

        PossibleLiteralValuesChoice possibleLiteralValuesChoice = ObjectAnnotationConverter.annotationToObject(
                literalDataDomainAttribute.plvc());

        DataType dataType = DataType.valueOf(literalDataDomainAttribute.dataType());

        Values defaultValue = ObjectAnnotationConverter.annotationToObject(literalDataDomainAttribute.defaultValue());

        LiteralDataDomain literalDataDomain = new LiteralDataDomain(
                possibleLiteralValuesChoice,
                dataType,
                defaultValue
        );

        return literalDataDomain;
    }

    public static LiteralData annotationToObject(LiteralDataAttribute literalDataAttribute) {
        List<Format> formatList = new ArrayList<>();
        for (FormatAttribute formatAttribute : literalDataAttribute.formats()) {
            formatList.add(ObjectAnnotationConverter.annotationToObject(formatAttribute));
        }

        List<LiteralDataDomain> lddList = new ArrayList<>();
        for (LiteralDataDomainAttribute literalDataDomainAttribute : literalDataAttribute.validDomains()) {
            lddList.add(ObjectAnnotationConverter.annotationToObject(literalDataDomainAttribute));
        }


        LiteralValue literalValue = ObjectAnnotationConverter.annotationToObject(literalDataAttribute.valueAttribute());

        return new LiteralData(formatList, lddList, literalValue);
    }

    public static RawData annotationToObject(RawDataAttribute rawDataAttribute) {
        List<Format> formatList = new ArrayList<>();
        for(FormatAttribute formatAttribute : rawDataAttribute.formats()){
            formatList.add(ObjectAnnotationConverter.annotationToObject(formatAttribute));
        }

        //Instantiate the RawData
        return new RawData(formatList);
    }

    public static LiteralValue annotationToObject(LiteralValueAttribute literalValueAttribute){
        LiteralValue literalValue = new LiteralValue();
        if(!literalValueAttribute.uom().equals(LiteralValueAttribute.defaultUom)) {
            literalValue.setUom(URI.create(literalValueAttribute.uom()));
        }
        if(!literalValueAttribute.dataType().equals(LiteralValueAttribute.defaultDataType)) {
            literalValue.setDataType(DataType.valueOf(literalValueAttribute.dataType()));
        }
        return literalValue;
    }

    public static void annotationToObject(InputAttribute inputAttribute, Input input){
        input.setMinOccurs(0);
        input.setMaxOccurs(inputAttribute.maxOccurs());
        input.setMinOccurs(inputAttribute.minOccurs());
    }

    public static PossibleLiteralValuesChoice annotationToObject(
            PossibleLiteralValuesChoiceAttribute possibleLiteralValuesChoiceAttribute){

        PossibleLiteralValuesChoice possibleLiteralValuesChoice = null;
        if(possibleLiteralValuesChoiceAttribute.allowedValues().length != 0){
            List<Values> valuesList = new ArrayList<>();
            for(ValuesAttribute va : possibleLiteralValuesChoiceAttribute.allowedValues()){
                valuesList.add(ObjectAnnotationConverter.annotationToObject(va));
            }
            possibleLiteralValuesChoice = new PossibleLiteralValuesChoice(valuesList);
        }
        return possibleLiteralValuesChoice;
    }

    public static void annotationToObject(ProcessAttribute processAttribute, Process process){
        process.setLanguage(Locale.forLanguageTag(processAttribute.language()));
    }
}