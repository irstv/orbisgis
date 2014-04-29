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

import org.gdms.data.{DataSource, DataSourceFactory, NoSuchTableException}
import org.gdms.data.types.{IncompatibleTypesException, TypeFactory}
import org.gdms.data.values.SQLValueFactory
import org.gdms.sql.engine.SemanticException
import org.gdms.sql.engine.commands._
import org.gdms.sql.engine.operations._
import org.gdms.sql.engine.GdmSQLPredef._
import org.gdms.sql.evaluator.Expression
import org.orbisgis.progress.ProgressMonitor

/**
 * Command for altering the actual schema of a table (columns and types).
 * 
 * @param name a table to alter
 * @param elems some altering elements
 * @author Antoine Gourlay
 * @since 0.1
 */
class AlterTableCommand(name: String, elems: Seq[AlterElement]) 
extends Command with OutputCommand with ExpressionCommand {
  
  private var ds: DataSource = _
  
  protected var exp: Seq[Expression] = Seq.empty

  override def doPrepare() = {
    // checks that the table actually exists
    if (!dsf.getSourceManager.exists(name)) {
      throw new NoSuchTableException(name)
    }
    
    ds = dsf.getDataSource(name, DataSourceFactory.EDITABLE)
    ds.open
    
    elems foreach {
      case a @ AddColumn(_, t) => {
          try {
            a.sqlTypeInstance = buildType(t)
          } catch {
            case e: IllegalArgumentException => throw new SemanticException(e)
          }
        }
      case a @ AlterTypeOfColumn(n, nT, init) => {
          if (ds.getFieldIndexByName(n) == -1) {
            throw new SemanticException("Column '" + n + "' does not exist in table '" + name + "'")
          }
          try {
            a.sqlTypeInstance = buildType(nT)
          } catch {
            case e: IllegalArgumentException => throw new SemanticException(e)
          }
          
          exp = init.toSeq
        }
      case DropColumn(n, exists) => if (!exists && ds.getFieldIndexByName(n) == -1) {
          throw new SemanticException("Column '" + n + "' does not exist in table '" + name + "'")
        }
      case RenameColumn(n, newName) => if (ds.getFieldIndexByName(n) == -1) {
          throw new SemanticException("Column '" + n + "' does not exist in table '" + name + "'")
        } else if (ds.getFieldIndexByName(newName) != -1) {
          throw new SemanticException("There already is a column '" + n + "' in table '" + name + "'")
        }
    }
    
    children = List(new DummyReferenceCommand(SQLMetadata(name, ds.getMetadata)))
    
    // init expressions
    super.doPrepare()
    
    elems foreach {
      case a @ AlterTypeOfColumn(n, nT, init) => {
          init match {
            case Some(ex) => 
              val s = ex.evaluator.sqlType
              val t = a.sqlTypeInstance.getTypeCode
              if (!TypeFactory.canBeCastTo(s, t)) {
                throw new IncompatibleTypesException("The field '" + n + "' cannot be changed: the expression cannot be implicitly cast " +
                                                     "from type '" + TypeFactory.getTypeName(s) + "' to type '" + TypeFactory.getTypeName(t) + "'")
              }
            case None =>
              val s = ds.getFieldType(ds.getFieldIndexByName(n)).getTypeCode
              val t = a.sqlTypeInstance.getTypeCode
              if (!TypeFactory.canBeCastTo(s, t)) {
                throw new IncompatibleTypesException("The field '" + n + "' cannot be changed: the expression cannot be implicitly cast " +
                                                     "from type '" + TypeFactory.getTypeName(s) + "' to type '" + TypeFactory.getTypeName(t) +
                                                     "'. Use an explicit cast with USING instead.")
              }
          }
        }
      case _ =>
    }
    
    children = Nil
  }
  
  /**
   * Builds a gdms Type object from an SQL type name.
   * 
   * @param str an SQL type name
   */
  private def buildType(str: String) = {
    TypeFactory.createType(SQLValueFactory.getTypeCodeFromSqlIdentifier(str))
  }

  protected final def doWork(r: Iterator[RowStream])(implicit pm: Option[ProgressMonitor]) = Iterator.empty
    
  override def execute(implicit pm: Option[ProgressMonitor]): RowStream = {
    elems foreach { e => e match {
        case a @ AddColumn(n, _) => ds.addField(n, a.sqlTypeInstance)
        case DropColumn(n, _) => {
            val index = ds.getFieldIndexByName(n)
            if (index != -1) {
              ds.removeField(index)
            }
          }
        case a @ AlterTypeOfColumn(n, newType, _) => {
            val index = ds.getFieldIndexByName(n)
            val ntemp = n + '$'
            ds.addField(ntemp, a.sqlTypeInstance)
            val newIndex = ds.getFieldIndexByName(ntemp)
            exp.headOption match {
              case Some(e) => {
                  (0l until ds.getRowCount) foreach { i =>
                    ds.setFieldValue(i, newIndex, e.evaluate(Row(ds.getRow(i))))
                  }
                }
              case None => {
                  (0l until ds.getRowCount) foreach { i =>
                    ds.setFieldValue(i, newIndex, ds.getFieldValue(i, index))
                  }
                }
            }
            
            ds.removeField(index)
            ds.setFieldName(ds.getFieldIndexByName(ntemp), n)
          }
        case RenameColumn(n, newName) => {
            ds.setFieldName(ds.getFieldIndexByName(n), newName)
          }
      }}
    
    ds.commit()

    Iterator.empty
  }
  
  override def doCleanUp() {
    ds.close
    ds = null
  }

  val getResult = null

  // no result
  override val getMetadata = null
}