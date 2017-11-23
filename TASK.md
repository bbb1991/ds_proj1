# Group Assignment: Distributed File System
## Project description
In this project, you will implement a simple Distributed File System (DFS). Files will be hosted remotely on one or 
more storage servers. Separately, a single naming server will index the files, indicating which one is stored where. 
When a client wishes to access a file, it first contacts the naming server to obtain information about the storage 
server hosting it. After that, it communicates directly with the storage server to complete the operation.

Your file system will support file reading, writing, creation, deletion, and size queries. It will also support certain 
directory operations - listing, creation and deletion. Files will be replicated on multiple storage servers. Large 
files will be split across different storage servers.

## Storage Servers
The primary function of storage servers is to provide clients with access to file data. Clients access storage servers 
in order to read and write files. Since storage servers store the data, they are also, in this design, the entities 
that report file sizes. Storage servers also must respond to certain commands from the naming server. 

Servers and clients need a way to identify files. Each file is identified by its path in the Distributed File System.

The storage server is required to put all its files in a directory on the machine that it is running on - this will be 
referred to as the storage server’s local or underlying file system. The structure within this directory should match 
the storage server’s view of the structure of the entire Distributed File System. For example, if a storage server is 
storing its files in the local directory */var/storage*, and it is hosting a file whose Distributed File System path 
is */directory/README.txt*, then that file’s full path on the local File System should be 
*/var/storage/directory/README.txt*. If a storage server is not aware of the existence of a file in the file system 
(because it is hosted by another storage server), it need not store anything for the file. This scheme provides a
convenient way to make data persist across storage server restarts.

## Naming Server
Clients do not normally have direct access to storage servers. Instead, their view of the file system is defined by the 
file system’s single naming server. The naming server tracks the file system directory tree, and associates each file in 
the file system to storage servers. When a client wishes to perform an operation on a file, it first contacts the naming 
server to obtain information about the storage server hosting the file, and then performs the operation on a storage 
server. Naming servers also provide a way for storage servers to register their presence.

The naming server can be thought of as an object containing a data structure which represents the current state of the 
file system directory tree, and providing several operations on it.

The naming server allows a client to create, list, and delete directories, create and delete files, determine whether a 
path refers to a directory or a file (or neither).

The naming server transparently performs replication of files, causing multiple storage servers to maintain copies of 
the same file.

## Client
Responsible for making the distributed nature of the system transparent to the user. File interface should include next 
functions:
* **Initialize**. Initialize the client storage on a new system, should remove any existed file in
the dfs root directory and return available size.
* **File read**. Should allow to read any file from DFS.
* **File write**. Should allow to put any file to DFS
* **File delete**. Should allow to delete any file from DFS
* **File info**. Should provide information about the file (any useful information - size, node id, etc.)
* **Open directory**. Should allow to change directory
* **Read directory**. Should return list of files, which are stored in the directory.
* **Make directory**. Should allow to make a directory.
* **Delete directory**. Should allow to delete directory. In case if the directory contains files the system should ask 
confirmation from the user before deletion. 

Each client should write the log file

## Replication and Sharding
Each file should be replicated on 2 Storage Servers. If one of the Storage Server goes down, files, that is stored 
should be replicated to another Storage Server.

Files might be split into chunks. Chunk size is under your consideration.

## Deliverables
Source code should be uploaded to the GitHub. The DFS write-up (white paper) should be uploaded on Moodle. DFS write-up 
MUST contain hash sum of the source code uploaded to GitHub (this won’t make you push some commits after the Deadline). 
Docker images should be uploaded to the DockerHub.
* Work
    * working in groups of 3(4)
    * deadline check - Nov, 21st. At the check phase you should present to your TA the finalised architecture and the 
    functionality that is already implemented. Each module implementation should have been started by the moment of 
    deadline check.
    * deadline - 11:55 p.m. Nov, 30th*

* Application
    * Source code of the application (you're free to pick any language of your choice)
    * Docker image(s) - should be uploaded to the Docker Registry
* Write-up
    * Architectural diagrams
    * Information about design decisions you made
    * Documentation how to launch and use your system

## Extra points
* Splitting files into chunks (+5%)
* File caching at the client (+10%)
* Concurrent operations handling (+5%)
* The system should be robust. It should be able to continue operation even if one of the participant nodes crashes, 
and it should be able to recover the state of a node following a crash so that it can resume operation (+15%)

## Grading criteria
* Application - 85%
* Write-up - 15%