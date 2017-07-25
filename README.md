<<<<<<< HEAD
# Phase: Validate
### Check Applications
=======
# Bundle your mule applications together for deployment

[![Build Status](https://travis-ci.org/hoeggsoftware/mule-bundle-maven-plugin.svg?branch=master)](https://travis-ci.org/hoeggsoftware/mule-bundle-maven-plugin)
[![Analytics](https://hoegg-ga-beacon.appspot.com/UA-76227345-2/mule-bundle-maven-plugin)](https://github.com/hoeggsoftware/ga-beacon)

## Check Applications
1. Name collisions on global elements / spring beans
2. Name collisions on configuration properties
3. Path collisions on HTTP listeners
4. Class collisions in `src/main/java`
5. Name collisions for RAML files

# Phase: Process Resources
1. Include bundle config files from `src/main/bundle`
# Phase: Prepare Package
1. Prefix and copy mule config files from dependencies
2. filter out `*-unbundled.xml`
4. Generate `mule-deploy.properties`
5. Combine all jars from `lib/` (or use maven to resolve dependencies)
6. Combine classes from `classes/`
# Phase: Package
1. Create bundle application zip
# Phase: Verify
1. Check that all config files in `mule-deploy.properties` are in the zip?
