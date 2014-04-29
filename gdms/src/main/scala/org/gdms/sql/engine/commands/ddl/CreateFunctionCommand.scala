/**
 * The GDMS library (Generic Datasource Management System)
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
 * Copyright (C) 2007-2014 IRSTV FR CNRS 2488
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
package org.gdms.sql.engine.commands.ddl

import org.gdms.sql.engine.GdmSQLPredef._
import org.gdms.sql.engine.SemanticException
import org.gdms.sql.engine.commands._
import org.orbisgis.progress.ProgressMonitor

/**
 * Creates (register) a function.
 * 
 * The <tt>language</tt> parameter is completely ignored for now. It is checked to be 'java' during
 * validation of the logical query plan.
 * 
 * @param name name of the function
 * @param as content of the function definition
 * @param language language name
 * @param replace true if any existing function must be silently replaced
 * @author Antoine Gourlay
 * @since 0.3
 */
class CreateFunctionCommand(name: String, as: String, language: String, replace: Boolean) 
extends Command with OutputCommand {
  // will hold the class object of the function
  private var cc: Class[_ <: org.gdms.sql.function.Function] = _
  
  override def doPrepare() = {
    // checks if the function already exists
    if (!replace && dsf.getFunctionManager.contains(name)) {
      throw new SemanticException("There already is a function named '" + name + "' registered.")
    }
    
    val c = classOf[CreateFunctionCommand].getClassLoader.loadClass(as)

    // checks it is indeed a Function child
    if (!classOf[org.gdms.sql.function.Function].isAssignableFrom(c)) {
      throw new SemanticException("Class '" + as + "' is not a valid Gdms function.")
    }
    
    cc = c.asInstanceOf[Class[_ <: org.gdms.sql.function.Function]]
  }
  
  protected final def doWork(r: Iterator[RowStream])(implicit pm: Option[ProgressMonitor]) = {
    // registers the function
    dsf.getFunctionManager.addFunction(name, cc, replace)
    
    Iterator.empty
  }
  
  override def doCleanUp() = cc = null
  
  val getResult = null

  override val getMetadata = null
}