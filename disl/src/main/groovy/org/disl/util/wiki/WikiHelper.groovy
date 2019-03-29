/*
 * Copyright 2015 - 2017 Karel H�bl <karel.huebl@gmail.com>.
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
package org.disl.util.wiki

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.disl.meta.*
import org.disl.util.doc.MetaManager
import org.disl.util.wiki.visjs.Edge
import org.disl.util.wiki.visjs.LineageNode

class WikiHelper extends MetaManager {

    Map<String,Set<LineageNode>> mappingLineageUsage =new HashMap<>()
    Map<String,Set<LineageNode>> tableLineageUsage =new HashMap<>()
    Map<String,Set<LineageNode>> mappingTargetUsage =new HashMap<>()
    Map<String,Set<LineageNode>> reportLineageUsage =new HashMap<>()

    static String wikiRootDir='../build/wiki'
    static boolean uglyUrls=Boolean.parseBoolean(System.getProperty('uglyUrls','false'))

    static String url(Table t) {
        "/data-model/${fileName(t)}"
    }

    static String url(Lookup t) {
        "/data-model/${fileName(t)}"
    }

    static String url(Mapping m) {
        "/data-mapping/${fileName(m)}"
    }

    static String url(Report r) {
        "/report/${fileName(r)}"
    }

    static String url(Perspective p) {
        "/perspective/${fileName(p)}"
    }

    @Override
    protected boolean includeType(Class type) {
        (MappingSource.isAssignableFrom(type) || Perspective.isAssignableFrom(type) || Report.isAssignableFrom(type))
    }


    static String fileName(Base base) {
        if (isUglyUrls()) {
            return "${fileName(base.class.name)}.html"
        }
        return fileName(base.class.name)
    }

    static String fileName(String className) {
        "${className.toLowerCase().replace('.','/').replace("\$","")}"
    }

     static File getLineageDataFile(String className) {
        new File("${wikiRootDir}/static/data-mapping/${fileName(className)}.json")
    }

    static File getDataModelDataFile(String className) {
        new File("${wikiRootDir}/static/data-model/${fileName(className)}.json")
    }

    static File getWikiPageFile(Table table) {
        new File("${wikiRootDir}/content/data-model/${fileName(table.class.name)}.md")
    }

    static File getWikiPageFile(Report report) {
        new File("${wikiRootDir}/content/report/${fileName(report.class.name)}.md")
    }

    static File getWikiPageFile(Lookup lookup) {
        new File("${wikiRootDir}/content/data-model/${fileName(lookup.class.name)}.md")
    }

    static File getWikiPageFile(Mapping mapping) {
        new File("${wikiRootDir}/content/data-mapping/${fileName(mapping.class.name)}.md")
    }

    static File getWikiPageFile(Perspective perspective) {
        new File("${wikiRootDir}/content/perspective/${fileName(perspective.class.name)}.md")
    }

    void generate(Table table) {
        if (table.getPattern()!=null) {
            new TablePageStep(table:table).execute()
            new TableDataModelData(table:table).execute()
            new TableLineageDataStep(table:table).execute()
        }
    }

    void generate(Report report) {
        new ReportPageStep(report:report).execute()
        new ReportLineageDataStep(report:report).execute()
    }

    void generate(Lookup lookup) {
        new LookupPageStep(lookup:lookup).execute()
    }

    void generate(Mapping mapping) {
        new MappingPageStep(mapping:mapping).execute()
        new MappingLineageDataStep(mapping:mapping).execute()
    }

    void generate(Perspective perspective) {
        new PerspectiveDataModelStep(perspective: perspective).execute()
        new PerspectiveWikiPageStep(perspective: perspective).execute()
    }

     static String renderDataModel(Table table) {
        if (table.foreignKeys.isEmpty()) {
            return ''
        }
        return """{{< datamodel "${fileName(table.class.name)}.json" >}}"""
    }

    static String renderDataModel(Perspective perspective) {
        return """{{< datamodel "${fileName(perspective.class.name)}.json" >}}"""
    }

    static String renderDataLineage(MappingSource mapping) {
        """{{< lineage "${fileName(mapping.class.name)}.json" >}}"""
    }

    static String renderDataLineage(Report report) {
        """{{< lineage "${fileName(report.class.name)}.json" >}}"""
    }

    void generateWiki() {
        process {
            if (it instanceof Table || it instanceof Mapping || it instanceof Perspective || it instanceof Report) {
                generate(it)
            }
        }
        insertMappingLineageUsage()
        insertTableLineageUsage()
        insertMappingTargetUsage()
        insertReportLineageUsage()
    }

    protected void insertMappingTargetUsage() {
        mappingTargetUsage.keySet().each { target ->
            try {
                Set<LineageNode> nodes = mappingTargetUsage.get(target)
                List<Edge> edges = nodes.collect({ node -> new Edge(from: node.id, to: target) })
                def lineageNetwork
                if (getLineageDataFile(target).exists()) {
                    lineageNetwork = new JsonSlurper().parse(getLineageDataFile(target))
                    lineageNetwork.nodes.addAll(nodes)
                    lineageNetwork.edges.addAll(edges)
                } else {
                    lineageNetwork = [nodes: nodes, edges: edges]
                }
                getLineageDataFile(target).write(new JsonBuilder(lineageNetwork).toPrettyString())
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    protected void insertTableLineageUsage() {
        tableLineageUsage.keySet().each { source ->
            try {
                Set<LineageNode> nodes = tableLineageUsage.get(source)
                List<Edge> edges = nodes.collect({ node -> new Edge(from: source, to: node.id) })
                def lineageNetwork
                if (getLineageDataFile(source).exists()) {
                    lineageNetwork = new JsonSlurper().parse(getLineageDataFile(source))
                    lineageNetwork.nodes.addAll(nodes)
                    lineageNetwork.edges.addAll(edges)
                } else {
                    lineageNetwork = [nodes: nodes, edges: edges]
                }
                getLineageDataFile(source).write(new JsonBuilder(lineageNetwork).toPrettyString())
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Insert source usage data into mapping lineage data files.
     * */
    protected void insertMappingLineageUsage() {
        mappingLineageUsage.keySet().each { source ->
            try {
                Set<LineageNode> nodes = mappingLineageUsage.get(source)
                List<Edge> edges = nodes.collect({ node -> new Edge(from: source, to: node.id) })
                def lineageNetwork
                if (getLineageDataFile(source).exists()) {
                    lineageNetwork = new JsonSlurper().parse(getLineageDataFile(source))
                    lineageNetwork.nodes.addAll(nodes)
                    lineageNetwork.edges.addAll(edges)
                } else {
                    lineageNetwork = [nodes: nodes, edges: edges]
                }
                getLineageDataFile(source).write(new JsonBuilder(lineageNetwork).toPrettyString())
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    protected void insertReportLineageUsage() {
        reportLineageUsage.keySet().each { source ->
            try {
                Set<LineageNode> nodes = reportLineageUsage.get(source)
                List<Edge> edges = nodes.collect({ node -> new Edge(from: source, to: node.id) })
                def lineageNetwork
                if (getLineageDataFile(source).exists()) {
                    lineageNetwork = new JsonSlurper().parse(getLineageDataFile(source))
                    lineageNetwork.nodes.addAll(nodes)
                    lineageNetwork.edges.addAll(edges)
                } else {
                    lineageNetwork = [nodes: nodes, edges: edges]
                }
                getLineageDataFile(source).write(new JsonBuilder(lineageNetwork).toPrettyString())
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    @Override
    void addUsage(Mapping mapping) {
        mapping.sources.each {
            addLineageSourceUsage(it,mapping)
        }
        mapping.setOperations.each {
            addLineageSourceUsage(it.source,mapping)
        }
        if (mapping instanceof  TableMapping && mapping.getTarget().getPattern() != null) {
            addMappingTargetUsage(mapping,mapping.getTarget())
        }
    }

    @Override
    void addUsage(Report report) {
        report.dependsOn.each {
            def src=MetaFactory.create(it)
            addLineageReportUsage(src,report)
        }
        report.sources.each {
            addLineageReportUsage(it,report)
        }
    }

    void addMappingTargetUsage(Mapping sourceMapping, Table targetTable) {
        Set<LineageNode> l=mappingTargetUsage.get(targetTable.class.name.replace("\$",""))
        if (!l) {
            l=new HashSet<LineageNode>()
            mappingTargetUsage.put(targetTable.class.name.replace("\$",""),l)
        }
        l.add(new LineageNode(sourceMapping,2))
    }

    void addLineageReportUsage(Mapping sourceMapping, Report report) {
        Set<LineageNode> l=reportLineageUsage.get(sourceMapping.class.name.replace("\$",""))
        if (!l) {
            l=new HashSet<LineageNode>()
            reportLineageUsage.put(sourceMapping.class.name.replace("\$",""),l)
        }
        l.add(new LineageNode(report,0))
    }

    void addLineageReportUsage(Table sourceTable, Report report) {
        Set<LineageNode> l=reportLineageUsage.get(sourceTable.class.name.replace("\$",""))
        if (!l) {
            l=new HashSet<LineageNode>()
            reportLineageUsage.put(sourceTable.class.name.replace("\$",""),l)
        }
        l.add(new LineageNode(report,0))
    }

    void addLineageSourceUsage(Mapping sourceMapping, Mapping mapping) {
        Set<LineageNode> l=mappingLineageUsage.get(sourceMapping.class.name.replace("\$",""))
        if (!l) {
            l=new HashSet<LineageNode>()
            mappingLineageUsage.put(sourceMapping.class.name.replace("\$",""),l)
        }
        l.add(new LineageNode(mapping,0))
    }

    void addLineageSourceUsage(Table sourceTable, Mapping mapping) {
        Set<LineageNode> l=tableLineageUsage.get(sourceTable.class.name.replace("\$",""))
        if (!l) {
            l=new HashSet<LineageNode>()
            tableLineageUsage.put(sourceTable.class.name.replace("\$",""),l)
        }
        l.add(new LineageNode(mapping,0))
    }

    void addLineageSourceUsage(Lookup sourceLookup, Mapping mapping) {
        //do nothing for now
        //TODO: implement later
    }

    static String renderElementDescription(Base base) {
        if (base.description && !'null'.equals(base.description)) {
            if (base.description.contains('.')) {
                return base.description.replaceFirst('\\.','.\n<!--more-->\n')
            }
            return "```\n${base.description}\n```\n<!--more-->\n"
        }
        return '<!--more-->'
    }

    static String renderColumnDescription(String description) {
        if (description==null || description.equals('null')) {
            return ''
        }
        return description
    }
}
