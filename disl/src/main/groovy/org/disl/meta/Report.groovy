/*
 * Copyright 2015 - 2019 Antonin Krotky <antoninkrotky@gmail.com>.
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
package org.disl.meta

import groovy.transform.CompileStatic

/**
 * Any report
 * */
@CompileStatic
abstract class Report extends Base {
	String schema = "report"

	List<MappingSource> sources=[]

	@Override
	void init() {
		super.init()
		initSourceAliases()
	}

	void initSourceAliases() {
		getPropertyNamesByType(MappingSource).each { initSourceAlias(it) }
	}

	void initSourceAlias(String property) {
		MetaProperty metaProperty=this.getMetaClass().getProperties().find {it.name==property}
		MappingSource p=(MappingSource)MetaFactory.create(metaProperty.getType())
		p.sourceAlias=property
		this[property]=p
		sources.add(p)
	}

	public String toString() {
		return getNameWithoutParenthesis()
	}
}
