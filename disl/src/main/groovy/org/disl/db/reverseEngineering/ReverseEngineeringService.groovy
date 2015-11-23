/*
 * Copyright 2015 Karel H�bl <karel.huebl@gmail.com>.
 *
 * This file is part of disl.
 *
 * Disl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Disl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Disl.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.disl.db.reverseEngineering

import groovy.sql.GroovyResultSet
import groovy.sql.GroovyResultSetProxy
import groovy.sql.Sql

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Types

import org.disl.meta.Column
import org.disl.meta.Context
import org.disl.meta.Table
import org.disl.pattern.Pattern

/**
 * Provides DISL model reverse engineering functionality.
 * */
class ReverseEngineeringService {
	protected static String SRC_FOLDER="src"

	String logicalSchemaName
	
	//ReverseTablePattern reverseTablePattern=new ReverseTablePattern()

	/**
	 * Reverse data dictionary tables and views into DISL data model.
	 * @param targetPackage Package to create reversed data model objects in.
	 * @param tablePattern Database object name like filter pattern. Default null.
	 * @param sourceSchemaFilterPattern Source scheme like filter pattern. Default null.
	 * @param outputDir Output directory.  Default SRC_FLDER.
	 * @param tableTypes  @see java.sql.DatabaseMetaData#getTables Default null.
	 * @parentClassName Parent class name for generated data model objects. Default @see getAbstractParentTableClassSimpleName. 
	 * */
	public Collection<Table> reverseSchemaTables(String targetPackage,String tablePattern=null,String sourceSchemaFilterPattern=null,File outputDir=new File(SRC_FOLDER),String[] tableTypes=null,String parentClassName=getAbstractParentTableClassSimpleName(targetPackage)){
		Sql sql=Context.getSql(getLogicalSchemaName())
		if (sourceSchemaFilterPattern==null) {
			sourceSchemaFilterPattern=Context.getContext().getPhysicalSchema(getLogicalSchemaName()).getSchema()
		}
		checkAbstractParentTableExist(targetPackage,outputDir)
		List<Table> tables=reverseEngineerTables(sql,tablePattern,tableTypes,sourceSchemaFilterPattern)
		tables.each {
			Table t=it
			ReverseTablePattern reverseTablePattern=new ReverseTablePattern()			
			reverseTablePattern.setPackageName(targetPackage)
			reverseTablePattern.setOutputDir(outputDir)
			reverseTablePattern.setParentClassName(parentClassName)			
			reverseTablePattern.setTable(t)
			reverseTablePattern.init()			
			reverseTablePattern.execute()
		}
	}

	protected List<Table> reverseEngineerTables(Sql sql,String tablePattern, String tableTypes,String sourceSchemaFilterPattern) {
		ResultSet res=sql.getConnection().getMetaData().getTables(null, sourceSchemaFilterPattern, tablePattern, tableTypes)
		GroovyResultSet gRes=new GroovyResultSetProxy(res).getImpl()
		List<Table> tables=collectRows(res,{new ReverseEngineeredTable(name: it.TABLE_NAME,description: it.REMARKS, schema:logicalSchemaName, physicalSchema: it.TABLE_SCHEM)})
		res.close()
		tables.each {
			Table table=it
			res=sql.getConnection().getMetaData().getColumns(null, sourceSchemaFilterPattern, table.getName(), null)
			eachRow(res,{table.columns.add(new Column(name: it.COLUMN_NAME,description: it.REMARKS,dataType: getDataType(it.TYPE_NAME,it.COLUMN_SIZE,it.DECIMAL_DIGITS,it.DATA_TYPE)))})
		}
		res.close()
		return tables
	}

	protected String getDataType(String dataTypeName,BigDecimal size, BigDecimal decimalDigits,BigDecimal sqlDataType) {
		if (size==null || isIgnoreSize(sqlDataType.intValue())) {
			return "${dataTypeName}"
		}
		if (decimalDigits==null) {
			return "${dataTypeName}(${size})"
		}
		return "${dataTypeName}(${size},${decimalDigits})"
	}
	boolean isIgnoreSize(int sqlType) {
		List ignoredTypes=[Types.DATE, Types.TIME, Types.ROWID]
		return ignoredTypes.contains(sqlType)
	}


	protected void checkAbstractParentTableExist(String packageName,File outputDir=new File(SRC_FOLDER)) {
		String abstractParentTableFileName=getAbstractParentTableClassSimpleName(packageName)
		String path="${packageName.replace('.','/')}/${abstractParentTableFileName}.groovy"
		File f=new File(outputDir,path)
		if (f.exists()) {
			return
		}
		f.getParentFile().mkdirs()
		f.createNewFile()
		f.write(getAbstractParentTableSourceCode(packageName))
	}


	public String getAbstractParentTableSourceCode(String packageName) {
		"""\
package ${packageName}

import org.disl.meta.Table

public abstract class ${getAbstractParentTableClassSimpleName(packageName)}  extends Table {
		String schema="${logicalSchemaName}"
}
"""
	}

	protected String getAbstractParentTableClassSimpleName(String packageName) {
		String simplePackageName=packageName.substring(packageName.lastIndexOf('.')+1).capitalize()
		"Abstract${simplePackageName}Table"
	}

	protected Collection collectRows(ResultSet res,Closure collectClosure) {
		def resultList=[]
		eachRow(res,{resultList.add(collectClosure(it))})
		resultList
	}

	protected void eachRow(ResultSet res,Closure eachRowClosure) {
		GroovyResultSet gRes=new GroovyResultSetProxy(res).getImpl()
		gRes.eachRow eachRowClosure
	}

	/**
	 * Try to parse SQL query stored in system clipboard and trace column mapping to system output and clipboard.
	 * Assumes valid SQL query is stored in system clipboard.
	 * */
	public void traceColumnMappings(Sql sql) {
		String query=Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor)
		traceColumnMappings(sql, query)
	}

	public void traceColumnMappings(Sql sql,String query) {
		String selectList=query.substring(query.toUpperCase().indexOf("SELECT")+6,query.toUpperCase().indexOf("FROM"))
		List columnExpressions=getColumnExpressions(new StringBuffer(selectList))

		PreparedStatement stmt=sql.getConnection().prepareStatement(query)
		ResultSetMetaData metadata=stmt.executeQuery().getMetaData()

		StringBuffer sb=new StringBuffer()
		for (int i=1;i<=metadata.columnCount;i++) {
			sb.append("ColumnMapping ${metadata.getColumnLabel(i)}=e ${nvl(columnExpressions[i-1],metadata.getColumnLabel(i))}\n")
		}


		StringSelection ss = new StringSelection(sb.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss,null);

		println sb.toString()
		println "ColumnMappings stored in clipboard."
	}

	/**
	 * Transform selectList into columnExpressions. Separate by commas not enclosed within parenthesis. Make uppercase evrything not within apostrophes.
	 * */
	protected List getColumnExpressions(StringBuffer selectList) {
		char SEPARATOR_CHAR='~'
		int bracketLevel=0
		int apostropheLevel=0
		for (int i=0;i<selectList.size();i++) {
			char actualChar=selectList.charAt(i)
			if (actualChar=='(') {
				bracketLevel++
			} else if (actualChar==')') {
				bracketLevel--
			} else if (actualChar==',' && bracketLevel==0) {
				selectList.setCharAt(i, SEPARATOR_CHAR)
			} else if (actualChar=="'") {
				apostropheLevel++
			} else if (apostropheLevel%2==0) {
				selectList.setCharAt(i, actualChar.toUpperCase())
			}
		}

		selectList.toString().split(new String(SEPARATOR_CHAR)).collect ({
			String expression=it.trim()
			if (expression.contains(" as ")) {
				return expression.substring(0, expression.lastIndexOf(" as "))
			}  else if (!expression.endsWith(')') && expression.contains(' ')) {
				return expression.substring(0,expression.lastIndexOf(' '))
			}
			return expression
		})
	}

	protected String nvl(String s1,String s2) {
		if (s1!=null) {
			return s1
		}
		return s2
	}
}