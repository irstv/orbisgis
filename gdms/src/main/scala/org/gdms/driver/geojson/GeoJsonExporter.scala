/*
 * The GDMS library (Generic Datasources Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...).
 * 
 * Gdms is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * 
 * Copyright (C) 2007-2012 IRSTV FR CNRS 2488
 * 
 * This file is part of Gdms.
 * 
 * Gdms is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Gdms is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Gdms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For more information, please consult: <http://www.orbisgis.org/>
 * 
 * or contact directly:
 * info@orbisgis.org
 */

package org.gdms.driver.geojson

import com.fasterxml.jackson.core.{JsonFactory, JsonParser, JsonEncoding}
import java.io.File
import org.gdms.data.DataSourceFactory
import org.gdms.data.schema.{DefaultMetadata, DefaultSchema, MetadataUtilities}
import org.gdms.driver.io.FileExporter
import org.gdms.driver.{DataSet, DriverException}
import org.gdms.driver.driverManager.DriverManager.{DEFAULT_SINGLE_TABLE_NAME => MainTable}
import org.gdms.source.SourceManager

/**
 * A geo-json exporter for Gdms.
 * 
 * @author Antoine Gourlay
 */
class GeoJsonExporter extends FileExporter with Writer {
  
  // internal usefull stuff
  private var file: File = _
  private val metadata = new DefaultMetadata()
  private lazy val jsonFactory = loadJsonFactory
  

  // constant values for Exporter
  val getType = SourceManager.FILE | SourceManager.VECTORIAL
  val getSupportedType = getType
  val getTypeName = "GeoJSON"
  val getTypeDescription = "Geo-JSON file format"
  val getExporterId = "geojson"
  val getFileExtensions = Array("json", "js")
  val getSchema = new DefaultSchema("json")
  
  def open() {}
  
  def close() {}
  
  def export(ds: DataSet, table: String) {
    if (table != MainTable) {
      throw new DriverException("Unknown table '" + table + "'!")
    }
    
    val g = jsonFactory.createJsonGenerator(file, JsonEncoding.UTF8)
    val met = ds.getMetadata
    val spatialIndex = MetadataUtilities.getSpatialFieldIndex(met)
    
    val m = Map((0 until met.getFieldCount) map (i => met.getFieldName(i) -> i): _*)
    
    import scala.collection.JavaConversions._
    
    write(g, ds.iterator, m)
  }
  
  def setFile(f: File) {file = f}
    
  def setDataSourceFactory(dsf: DataSourceFactory) {}
  
  private def loadJsonFactory: JsonFactory = {
    import JsonParser.Feature._
    
    (new JsonFactory)
    .configure(ALLOW_COMMENTS, true)
    .configure(ALLOW_SINGLE_QUOTES, true)
    .configure(ALLOW_NON_NUMERIC_NUMBERS, true)
  }
}
