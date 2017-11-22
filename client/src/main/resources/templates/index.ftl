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
                    <th>File size</th>
                </tr>
                </thead>
                <tbody>
                <#list files as file>
                <tr>
                    <td>${file.id}</td>
                    <td>${file.datatype}</td>
                </tr>
                </#list>
                </tbody>
            </table>


            <p>${files?size}</p>
            <br>
            <form method="post" action="/mkdir">
                <input type="text" placeholder="Folder name" name="folderName">
                <input type="submit" value="Create folder">
            </form>
        </div>
    </div>
</div>

</body>
</html>