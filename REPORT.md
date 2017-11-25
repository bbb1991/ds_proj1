# Report
## Namenode
### Short description
This server holds meta information such as file name, file size, in which datanode
file saved and etc.
### How to run
```text
mvn clean install
mvn spring-boot:run -pl namenode
```
### DB
By default, this project uses **H2** database. But you can change it to another DB.
To access data in runtime, after starting namenode go to [http://localhost:8000/console](http://localhost:8000) and
change JDBC URL to ```jdbc:h2:./test```

## Datanode
### Short description
This server stores files.
### How to run
```text
mvn clean install
mvn spring-boot:run -pl datanode
```


## Client
### How to run
```text
mvn clean install
mvn spring-boot:run -pl client
```



