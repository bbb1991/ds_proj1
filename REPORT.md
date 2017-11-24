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



