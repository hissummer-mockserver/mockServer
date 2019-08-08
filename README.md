# Hissummer Mockserver 

## Build and start the Mockserver manually
```
If we want run one server integrated with mockServer and mockServer adminUi. If we don't do step1 and step2, we need serve the Mockserver AdminUI in another webserver like nginx. 
```
1. Build admin ui first.  <a href="https://github.com/hissummer-mockserver/mockserverAdminUI" target="_blank">See how to build mockserver adminUi</a>
2. Copy the admin ui build files into the Mockserver source code.  
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
Please install mongodb server, version need greater than  or equal 3.2. 
```
* <a href="https://docs.mongodb.com/manual/installation/">Mongodb install guide</a>  or <a href="https://hub.docker.com/_/mongo"> start docker </a>
5. Start mockserver.
```
Assumer mongodb server listen on the default localhost:27017 and just run the application.
java -jar target/hissummer-mockserver-0.0.1-SNAPSHOT.jar  --server.port=8081 --spring.data.mongodb.host=localhost --spring.data.mongodb.port=27017
```
6. Browser access http://localhost:8081/
```
we could change the port 8081 to others
```
## Docker compose  to start Mockserver and Mockserver adminUI 

## Feature
1. add mock rule
2. delete mock rule
3. copy mock rule
4. update mock rule
5. mock rule based hostname and uri path.  
