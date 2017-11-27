<!doctype html>
<html>
<head>
    <title>Index page</title>
    <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"/>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="col-md-12">
            <h1 class="text-center">Index page</h1>
        <#if files??>

            <table class="table table-hover">
                <thead>
                <tr>
                    <th>File name</th>
                    <th>File type</th>
                    <th>File size</th>
                </tr>
                </thead>
                <tbody>
                    <#list files as file>
                    <tr>
                        <td><a href="/get/${file.originalName}">${file.originalName}</a></td>
                        <td>${file.datatype}</td>
                        <td>${file.fileSize}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        <#else>
            <h3>No files available to display!</h3>
        </#if>


        <#--<p>${files?size}</p>-->
            <hr>
            <h3 class="text-center">Create new folder</h3>
            <br>
            <form method="post" action="/mkdir">
                <input type="text" placeholder="Folder name" name="folderName" required>
                <input type="hidden" name="current-folder" value="${currentFolderId}">
                <input type="submit" value="Create folder" class="btn btn-success">
            </form>

            <br>
            <hr>
            <h3 class="text-center">Upload new file</h3>
            <form method="post" action="/upload" enctype="multipart/form-data">
                <input type="hidden" name="current-folder" value="${currentFolderId}">
                <input type="file" name="file">
                <input type="submit" value="Upload file" class="btn btn-success">
            </form>

            <br>
            <hr>
            <h3 class="text-center">Remove file/folder</h3>
            <form method="post" action="/remove">
                <input type="text" placeholder="File or folder name" name="name" required>
                <input type="hidden" name="current-folder" value="${currentFolderId}">
                <input type="submit" value="Remove file/folder" class="btn btn-danger">
            </form>
        </div>
    </div>
</div>
<script src="//code.jquery.com/jquery-3.2.1.min.js"
        integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
</body>
</html>