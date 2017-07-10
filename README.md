# Bundle your mule applications together for deployment

[![Analytics](https://hoegg-ga-beacon.appspot.com/UA-76227345-2/mule-bundle-maven-plugin)](https://github.com/hoeggsoftware/ga-beacon)

## Check Applications
1. Name collisions on global elements / spring beans
2. Name collisions on configuration properties
3. Path collisions on HTTP listeners
4. Class collisions in `src/main/java`
5. Name collisions for RAML files

## Create Bundle
1. Prefix and copy mule config files from dependencies
2. apply inclusions/exclusions to those config files
3. Include bundle config files from `src/main/bundle`
4. Generate `mule-deploy.properties`
5. Combine all jars from `lib/` (or use maven to resolve dependencies)
6. Combine classes from `classes/`
