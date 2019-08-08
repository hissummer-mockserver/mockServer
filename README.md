# Hissummer Mockserver 

## Build and start the Mockserver
1. Build admin ui first.  <a href="https://github.com/hissummer-mockserver/mockserverAdminUI" target="_blank">See how to build mockserver adminUi</a>
2. Copy the admin ui build files into the mockserver source code.
```
cp the dist/* to src/main/resource/static/
cp the dist/index.html to src/main/webapp/templates/
```
3. Build the mock server.
```
mvn clean package -Dmaven.test.skip=true
```
4. Install mongodb. 
```
Please install mongodb server, version need greater than  or equal 3.2. Assumer mongodb server listen on the localhost:27017
<a href="https://docs.mongodb.com/manual/installation/">Mongodb install guide</a>
```
5. Start mockserver.
```
java -jar target/hissummer-mockserver-0.0.1-SNAPSHOT.jar  --server.port=8081 --spring.data.mongodb.host=localhost
```
6. Browser access http://localhost:8081/

```
we could change the port 8081 to others
```

## Feature
1. add mock rule
2. delete mock rule
3. copy mock rule
4. update mock rule
5. mock rule based hostname and uri path.  
