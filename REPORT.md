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

### Folder structure
```text
foo
 |- bar
 |- biz
     |- file.txt
```
turns into:
| id | name | parent_id |
| --- | --- | --- |
| 1 | foo | 0 |



## Datanode
### Short description
This server stores files.
### How to run
```text
mvn clean install
mvn spring-boot:run -Drun.jvmArguments="-Ddatanode.port=9090 -Ddatanode.host=0.0.0.0" -pl datanode
```

When you run datanode, you need to specify port via parameter <code>-Ddatanode.port</code>, in example above, we open port
<code>9090</code> for listen. If you does not specify port, application will open random port.
Also, you can specify which host should listen with parameter <code>-Ddatanode.host</code>.

Both parameters are optional.


## Client
### How to run
```text
mvn clean install
mvn spring-boot:run -pl client
```



