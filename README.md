# CYpher TO Sql Mapper (Cytosm)

[![Build and Test](https://github.com/Stephen-Allen/cytosm/actions/workflows/test.yml/badge.svg)](https://github.com/Stephen-Allen/cytosm/actions/workflows/test.yml)

This is a fork of [Cytosm](https://github.com/cytosm/cytosm) with new features and bug fixes.

## Added Features
- Add Regex, StartsWith, EndWith, and Contains operators
- Add toLower() and toUpper() functions
- Handle directional edges when constructing joins
- Support edges with no label
- A release build to Maven POM
- Refactor build system and use Maven Wrapper
- Replace log4j with slf4j

## Fixes
- Parse name and string literal escape sequences in Cypher queries correctly
- Escape string literals in SQL output
- Clean up and simplify the GTopInterface class
- Keep track of the labels on relationship variables during SQL generation
- Misc fixes and improvements

## Dependency Updates
- Update Java 8 to 17 (necessary for ANTLR 4.10+)
- Refactor and remove unused/underused dependencies
- Update JUnit 4 to 5
- Update to the latest maven plugin and 3rd party library versions

## Usage
Include the following in your pom.xml:
```xml
<dependency>
  <groupId>io.github.stephen-allen.cytosm</groupId>
  <artifactId>cypher2sql</artifactId>
  <version>1.3</version>
</dependency>
```
The main entry points to use the library are the static methods in the [`PassAvailables`](cypher2sql/src/main/java/org/cytosm/cypher2sql/PassAvailables.java) class.
See [README_ORIG.md](README_ORIG.md) for additional detail.
