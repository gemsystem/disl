# disl
DISL = Data Integration Specific Language. 

DISL is one of the core parts of our complex Business Intelligence (BI) solution **GEM Data Integration Suite** developed by [GEM System a.s.](http://www.gemsystem.cz) 

Goal of this project is to implement groovy based domain specific language supporting modelling of data integration projects. DISL will support data modeling and data mapping including support for MDA transformations and unit testing.

Main features:
- Enable agile, high productivity development and maintenance of data integration projects
- Easy to learn for SQL developers
- One tool for modeling, testing, deployment and execution
- Data integration models represented by concise DSL code
- Enable configuration management using standard version control systems
- Support unit testing of data transformation logic
- Support embedding of test data within unit tests to prevent maintenance of test data in database.
- Open to integrate with open source and commercial data integration tools
- Ready for continuous integration and continuous delivery
- Smart parallel processing (automatically reflecting dependencies)
- Licensed under GNU General Public License

##More information
[DISL WIKI](https://github.com/kaja78/disl/wiki)

To see disl in action check sample disl project: https://github.com/gemsystem/disl-hsqldb-sample 

This is a fork of the original project. Credit to [Karel Hübl](https://github.com/kaja78). We also use [vis.js](http://visjs.org/).