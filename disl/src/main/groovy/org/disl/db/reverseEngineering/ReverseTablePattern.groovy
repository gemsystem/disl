/*
 * Copyright 2015 - 2016 Karel Hübl <karel.huebl@gmail.com>.
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

import groovy.json.StringEscapeUtils
import org.disl.meta.Column
import org.disl.pattern.FileOutputStep
import org.disl.pattern.TablePattern

/**
 * Pattern for generating source code for DISL data model table.
 * */
class ReverseTablePattern extends TablePattern<ReverseEngineeredTable> {
	/**
	 * Output directory for generated source code.
	 * */
	File outputDir=new File("src")
	
	/**
	 * Package name of generated table class.
	 * */
	String packageName
	
	/**
	 * Class name of table class parent.
	 * */
	String parentClassName
	
	File getFile() {
		File directory=new File(outputDir,packageName.replace('.', '/'))
		new File(directory,"${CreateDislTable.tableName(table.name)}.groovy")
	}

	@Override
	public void init() {	
		add CreateDislTable
	}
	
	static class CreateDislTable extends FileOutputStep {
		
		ReverseTablePattern getPattern() {
			super.pattern
		}
		
		File getFile() {
			getPattern().getFile()
		}

		String description(String text) {
			if (text) {
				return "@Description(\"\"\"${escape(text)}\"\"\")"
			}
			return ''
		}
		Boolean isColumnSpecialName(String name) {
			if (name.contentEquals("Name")) return true
			if (name.contentEquals("name")) return true
			if (name.contentEquals("Class")) return true
			if (name.contentEquals("class")) return true
			return !name.matches("[a-zA-Z_][a-zA-Z0-9_]*")
		}

		String realColumnName(String name) {
			if (isColumnSpecialName(name)) {
				return """\n\t\t@Name(\"\"\"${escape(name)}\"\"\")"""
			} else {
				return ''
			}
		}

		String columnName(String name) {
			if (isColumnSpecialName(name)) {
				return "${name.toUpperCase().replaceAll('[^a-zA-Z0-9]', '_')}"
			} else {
				return name
			}
		}

		static Boolean isTableSpecialName(String name) {
			return !name.matches("[A-Z][a-zA-Z0-9]*")
		}

		static String realTableName(String name) {
			if (isTableSpecialName(name)) {
				return "@Name(\"\"\"${escape(name)}\"\"\")"
			} else {
				return ''
			}
		}

		static String tableName(String name) {
			if (isTableSpecialName(name)) {
				return "${name.toUpperCase().replaceAll('[^A-Z0-9]', '_')}"
			} else {
				return name
			}
		}

		static String escape(String text) {
			return StringEscapeUtils.escapeJava(text)
		}
		
		String getCode() {
			"""\
package $pattern.packageName

import org.disl.meta.*

${description(pattern.table.description)}${realTableName(pattern.table.name)}$foreignKeyDefinition
@groovy.transform.CompileStatic
class ${tableName(pattern.table.name)} extends ${pattern.parentClassName} {

$columnDefinitions
}"""
	}

	String getColumnDefinitions() {
		pattern.table.getColumns().collect {getColumnDefinitionCode(it)}.join("\n\n")
	}
	
	String getForeignKeyDefinition() {
		if (pattern.table.foreignKeys.size()==0) {
			return ''
		}
		return """
@ForeignKeys([${pattern.table.foreignKeys.collect({"@ForeignKey(name='${it.name}',targetTable=${it.targetTableClassName},sourceColumn='${it.sourceColumn}',targetColumn=('${it.targetColumn}'))"}).join(',\n')}])"""
	}

	String getColumnDefinitionCode(Column column) {
		String notNull =column.notNull?"\n\t\t@NotNull":""
		String primaryKey =column.primaryKey?"\n\t\t@PrimaryKey":""
		
		"""\
		${description(column.description)}${realColumnName(column.name)}
		@DataType("$column.dataType")$primaryKey$notNull
		Column ${columnName(column.name)}"""
		}
	}
}