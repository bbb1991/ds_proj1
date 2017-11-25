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
                    <td>${file.originalName}</td>
                    <td>${file.datatype}</td>
                    <td>${file.fileSize}</td>
                </tr>
                </#list>
                </tbody>
            </table>


        <#--<p>${files?size}</p>-->
            <br>
            <form method="post" action="/mkdir">
                <input type="text" placeholder="Folder name" name="folderName" required>
                <input type="submit" value="Create folder">
            </form>

            <br>
            <form method="post" action="/upload" enctype="multipart/form-data">
                <input type="file" name="file">
                <input type="submit" value="Upload file">
            </form>
        </div>
    </div>
</div>
<script src="https://code.jquery.com/jquery-3.2.1.min.js"
        integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
</body>
</html>