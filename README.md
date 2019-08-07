# Hissummer Mock server 

## Build and start the Mockserver
1. build admin ui first  <a href="https://github.com/hissummer-mockserver/mockserverAdminUI" target="_blank">See how to build mockserver adminUi</a>
2. copy the admin ui build files into the mockserver source code
```
cp the dist/* to src/main/resource/static/
cp the dist/index.html to src/main/webapp/templates/
```
3. build the mock server
```
mvn clean package -Dmaven.test.skip=true
```
4.  java -jar target/hissummer-mockserver-0.0.1-SNAPSHOT.jar  --server.port=8081 --spring.data.mongodb.host=localhost

```
Please install mongodb server, version need greater than  or equal 3.2 
```

5. browser access http://localhost:8081/

```
we could change the port 8081 to others
```

## Feature
1. add mock rule
2. delete mock rule
3. copy mock rule
4. update mock rule
5. mock rule based hostname and uri path.  
