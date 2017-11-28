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
This server stores files. All data stores on ```datanode.working_dir``` folder (you can change it by editing 
```application.properties``` file). 

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

## Operations
### Uploading file
1. Client connects to namenode and sends meta information about file: 
    - file size
    - file name
    - working directory
1. Namenode checks, is datanode available to store data. If all datanodes are dead then namenode sends status to client
```NO_DATANODE_AVAILABLE```, and closes the connection. If datanodes available, then namenode inserts data about file to
database, with field ```locked=true```.
1. Namenode response to client status ```OK```, and master datanode IP with port and witch name should file have.
1. Client uploads file to datanode with given name.
1. Datanode sends to namenode status ```OK``` with filename.
1. Namenode changes database field ```locked``` from ```true``` to ```false```, which indicates that file is available to 
download.
1. If datanodes more than one, namenode sends command ```UPLOAD_FILE``` with list of datanodes to master datanode, 
which indicates that uploaded file must be replicated to another datanodes.

### Creating folder
1. Client connects to namenode and sends command ```MKDIR``` with folder name
1. namenode inserts record to database.
1. namenode sends ```OK``` status to client

### Renaming file/folder
1. Client connects to namenode and sends command ```RENAME``` with old and new names
1. Namenode changes record in database.
1. Namenode sends ```OK``` status to client

### Removing file/folder
1. Client connects to namenode and sends command ```REMOVE``` with folder/file name and working dir.
1. If we removing is file, namenode iterates over list of datanodes and sends command ```REMOVE``` with filename. 
After file is removed from all datanode, namenode removed record from database, and return answer ```OK``` to client
1. If we removing folder:
    - get a list of files
    - remove files from datanodes
    - remove all records from db
 
## Additional features
### Caching
TODO add info about ehcache