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
package org.disl.workflow

import org.disl.meta.Column
import org.disl.meta.ColumnMapping
import org.disl.meta.ForeignKey
import org.disl.meta.ForeignKeys
import org.disl.meta.Mapping
import org.disl.meta.MetaFactory
import org.disl.meta.PrimaryKey
import org.disl.meta.Table
import org.disl.meta.TableMapping
import org.disl.pattern.generic.CreateOrReplaceTablePattern
import org.disl.pattern.generic.TruncateInsertPattern
import org.disl.test.DislTestCase
import org.junit.Assert
import org.junit.Test

class TestDependency extends DislTestCase {

    static class TestingMappingA extends Mapping {
        Tab2 s2a
        Tab2 s2b

        ColumnMapping KEY = e s2a.KEY

        void initMapping() {
            from s2a
            innerJoin s2b on(s2a.KEY, s2b.KEY)
        }
    }

    static class TestingMappingB extends TableMapping {
        TruncateInsertPattern pattern

        Tab1 target

        TestingMappingA mA
        Tab3 s3

        ColumnMapping KEY = e mA.KEY

        void initMapping() {
            from mA
            innerJoin s3 on "$mA.KEY=$s3.KEY"
        }
    }

    @ForeignKeys([@ForeignKey(targetTable = Tab2, targetColumn = "KEY")])
    static class Tab1 extends Table {
        CreateOrReplaceTablePattern pattern
        @PrimaryKey
        Column KEY
    }

    static class Tab2 extends Table {
        CreateOrReplaceTablePattern pattern
        @PrimaryKey
        Column KEY
    }

    static class Tab3 extends Table {
        CreateOrReplaceTablePattern pattern
        @PrimaryKey
        Column KEY
    }

    @Test
    public void testChart() {
        def tab1 = MetaFactory.create(Tab1)
        def tab2 = MetaFactory.create(Tab2)
        def tab3 = MetaFactory.create(Tab3)
        def mapB = MetaFactory.create(TestingMappingB)
        Dependency dependency = new Dependency([tab1,tab2,tab3,mapB])

        DependencySetProperties dependencySetProperties = new DependencySetProperties(dependency)
        dependencySetProperties.setGraphicalStyle()

        DependencyDrawChart dependencyDrawChart = new DependencyDrawChart(dependency)
        String chart = dependencyDrawChart.getChart()

        dependencyDrawChart.createFile()
    }

    @Test
    public void testDependency() {
        def tab1 = MetaFactory.create(Tab1)
        def tab2 = MetaFactory.create(Tab2)
        def tab3 = MetaFactory.create(Tab3)
        def mapB = MetaFactory.create(TestingMappingB)
        Dependency dependency = new Dependency([tab1,tab2,tab3,mapB])

        Assert.assertArrayEquals(dependency.getDependencyTypes(tab1,tab2).toArray(),["FK"].toArray())
        Assert.assertTrue(dependency.isDependant(tab1,tab2))

        Assert.assertArrayEquals(dependency.getDependencyTypes(tab1,mapB).toArray(),["target"].toArray())
        Assert.assertTrue(dependency.isDependant(tab1,mapB))

        Assert.assertArrayEquals(dependency.getDependencyTypes(mapB,tab2).toArray(),["source"].toArray())
        Assert.assertTrue(dependency.isDependant(mapB,tab2))

        Assert.assertArrayEquals(dependency.getDependencyTypes(mapB,tab3).toArray(),["source"].toArray())
        Assert.assertTrue(dependency.isDependant(mapB,tab3))

        Assert.assertArrayEquals(dependency.getDependencyTypes(tab1,tab3).toArray(),[].toArray())
        Assert.assertFalse(dependency.isDependant(tab1,tab3))

        //only type
        Assert.assertArrayEquals(dependency.getDependencyTypes(mapB,tab3,[],["source"]).toArray(),["source"].toArray())
        Assert.assertArrayEquals(dependency.getDependencyTypes(mapB,tab3,[],["nic"]).toArray(),[].toArray())
        //ignore type
        Assert.assertArrayEquals(dependency.getDependencyTypes(mapB,tab3,["source"],[]).toArray(),[].toArray())
        Assert.assertArrayEquals(dependency.getDependencyTypes(mapB,tab3,["vse"],[]).toArray(),["source"].toArray())
    }
}