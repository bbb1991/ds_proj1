# Report
## Authors:
- Bagdat Bimaganbetov. Server side programming
- Khayrullo Rustamov. Client side programming

## Short description
This project has 4 modules:
- **common** - contains frequently using classes, such as hibernate models, useful methods and etc.
- **client** - client logic. Provides web interface to explore commands such as upload, remove, download and etc.
- **namenode** - server side, that holds meta information.
- **datanode** - server side, that holds files.

Used technologies:
- **Java 8**
- **Hibernate**
- **Spring boot**
- **Jetty** server
- **H2** database
- **Freemarker** template engine
- **ehcache** caching library

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
To access data in runtime, after starting namenode go to [http://localhost:8000/console](http://localhost:8000/console) and
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
mvn spring-boot:run -Drun.jvmArguments="-Ddatanode.port=9090 -Ddatanode.host=0.0.0.0 -Ddatanode.dir=/tmp" -pl datanode
```

When you run datanode, you need to specify port via parameter <code>-Ddatanode.port</code>, in example above, we open port
<code>9090</code> for listen. If you does not specify port, application will open random port.
Also, you can specify which host should listen with parameter <code>-Ddatanode.host</code>.
All data stored in folder ```-Ddatanode.dir``. If this parameter not passed, then we use temp folder (Depends on system).`

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
To caching, we use library called **ehcache**.
Memory store eviction Policy is **LFU**.
#### How you can check it?
1. Upload file
1. Download file
1. Turn off namenode/datanode
1. Download file again.

Also you can download file and when you downloading in second time you can just look at datanode/namenode logs (no records about
downloading second time).

Node. In first, we also used caching list of files. (if namenode is down, we can refresh page and get list of files and directories from cache)
But then we figured out, that it is not good idea, because after removing file/folder, instead of getting list of files from namenode client gets it from cache


## Hashsum of project:
```text
TODO
```
How we calculated: 
```bash
wget https://github.com/bbb1991/ds_proj1/archive/master.zip
sha512sum master.zip 
```