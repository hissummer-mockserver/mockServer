# Hissummer Mockserver 

This is the mockserver backend server project. It is a java project based on springboot. 

## Start application
* Download war
* Start spring-boot application
```
The wars was packaged by following the steps in https://github.com/hissummer-mockserver/mockserver. The every version of war are including mockserver(backend) and mockserverAdminUI projects.
```
Assume mongodb server is up and listen on the localhost:27017.  Please note , mongodb server version need greater than 3.2.0 or equal. Run below command:
```
$java -jar hissummer-mockserver.war  --server.port=8081 --spring.data.mongodb.host=localhost --spring.data.mongodb.port=27017
```
After started succesfully , access http://localhost:8081/

## Feature
1. add mock rule
2. delete mock rule
3. copy mock rule
4. update mock rule
5. mock rule based hostname and uri path.  
6. support eureka mock server (register and heartbeat)

## Version
* <a href="#"> 0.0.1-alpha </a> 
* <a href="https://pkg.githubusercontent.com/199478482/34f13c00-1ba5-11ea-9074-b6b2365e7c6d?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIWNJYAX4CSVEH53A%2F20191210%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20191210T153505Z&X-Amz-Expires=300&X-Amz-Signature=7193bf34b93af1fce81246e49b03b53333725880d6aa0a46879d4fde9802772b&X-Amz-SignedHeaders=host&actor_id=784170&response-content-disposition=filename%3Dmockserver-0.0.2-alpha.war&response-content-type=application%2Foctet-stream"> 0.0.2-alpha </a>


