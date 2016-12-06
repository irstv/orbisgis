package org.orbisgis.wpsservice

import org.orbisgis.wpsgroovyapi.attributes.TranslatableString
import org.orbisgis.wpsgroovyapi.attributes.LanguageString
import org.orbisgis.wpsgroovyapi.attributes.MetadataAttribute
import org.orbisgis.wpsgroovyapi.input.EnumerationInput
import org.orbisgis.wpsgroovyapi.output.EnumerationOutput
import org.orbisgis.wpsgroovyapi.process.Process

/********************/
/** Process method **/
/********************/

/**
 * Test script for the Enumeration
 * @author Sylvain PALOMINOS
 */
@Process(title = ["Enumeration test","en","Test du Enumeration","fr"],
        description = ["Test script using the Enumeration ComplexData.","en",
                "Scripts test pour l'usage du ComplexData Enumeration.","fr"],
        keywords = ["test,script,wps","en","test,scripte,wps","fr"],
        identifier = "orbisgis:test:enumeration",
        metadata = [
                @MetadataAttribute(title = "metadata", role = "website", href = "http://orbisgis.org/")
        ]
)
def processing() {
    sleep(500)
    enumerationOutput = inputEnumeration;
}


/****************/
/** INPUT Data **/
/****************/

/** This Enumeration is the input data source. */
@EnumerationInput(
        title = ["Input Enumeration","en","Entrée Enumeration","fr"],
        description = ["A Enumeration input.","en","Une entrée Enumeration.","fr"],
        keywords = ["input","en","entrée","fr"],
        multiSelection = true,
        isEditable = true,
        values = ["value1", "value2"],
        selectedValues = ["value2"],
        names = ["name1", "name2"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "orbisgis:test:enumeration:input",
        metadata = [
                @MetadataAttribute(title = "metadata", role = "website", href = "http://orbisgis.org/")
        ]
        )
String[] inputEnumeration

/*****************/
/** OUTPUT Data **/
/*****************/

/** This Enumeration is the output data source. */
@EnumerationOutput(
        title = ["Output Enumeration","en","Sortie Enumeration","fr"],
        description = ["A Enumeration output.","en","Une sortie Enumeration.","fr"],
        keywords = ["output","en","sortie","fr"],
        values = ["value1", "value2"],
        identifier = "orbisgis:test:enumeration:output",
        metadata = [
                @MetadataAttribute(title = "metadata", role = "website", href = "http://orbisgis.org/")
        ]
)
String[] enumerationOutput

