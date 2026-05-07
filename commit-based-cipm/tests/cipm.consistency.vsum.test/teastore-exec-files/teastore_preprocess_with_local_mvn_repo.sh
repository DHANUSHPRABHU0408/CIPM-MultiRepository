cp -a ../mvn-wrapper/. ./
chmod u+x ./mvnw
java -version
./mvnw clean install -Dmaven.test.skip=true -Dmaven.repo.local=../../mvn-local && ./mvnw dependency:copy-dependencies -Dmaven.repo.local=../../mvn-local
