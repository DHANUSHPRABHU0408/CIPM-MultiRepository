cp -a ../mvn-wrapper/. ./
chmod u+x ./mvnw
java -version
./mvnw clean install -Dmaven.test.skip=true && ./mvnw dependency:copy-dependencies
