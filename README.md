# CYpher TO Sql Mapper (Cytosm)

[![Build Status](https://travis-ci.org/cytosm/cytosm.svg?branch=master)](https://travis-ci.org/cytosm/cytosm.svg?branch=master)

This is a fork of the main [Cytosm](https://github.com/cytosm/cytosm) repository with some fixes and added features.

## Added Features
- Add Regex, StartsWith, EndWith, and Contains operators
- Add toLower() and toUpper() functions
- A release build to Maven POM
- Refactor build system and use Maven Wrapper

## Fixes
- Parse name and string literal escape sequences in Cypher queries correctly
- Escape string literals in SQL output
- Clean up and simplify the GTopInterface class
- Keep track of the labels on relationship variables during SQL generation
- Update to the newest library versions
- Misc fixes and improvements


