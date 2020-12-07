mvn clean package -DskipTests -s settings.xml  -Ddb.url=jdbc:postgresql://localhost:5432/core -Ddb.password=postgres -Ddb.user=postgres -Ddb.driver=org.postgresql.Driver  >> build.log &

tail -f build.log
