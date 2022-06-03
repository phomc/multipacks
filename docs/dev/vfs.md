# Transformative Virtual File System (``TransformativeFileSystem``)
The ``TransformativeFileSystem`` class allow you to apply data transformations to user's pack (such as adding font icons, which can be seen in ``FontIconsTransformPass``). You would starts with VFS that contains all the files from the pack (and maybe generated files from previous transform passes) and you can add or remove files (the remove operation doesn't affect user's pack), which then passed to either another transform pass or to output.

## Working with VFS
The VFS contains some methods, and some of them are documented using JavaDocs. Files should be opened with ``openRead(String path)`` and the stream must be closed once you've done with it, and files can be written using ``put(String path, byte[] binaryData)`` (tip: use ``ByteArrayOutputStream``).

### Mark a file as deleted
Sometimes you might want to mark a file as deleted so it won't shows up in output package. You can do that by using ``TransformativeFileSystem#delete()``:

```
fs.put("assets/minecraft/font/default.json", myFontJsonAsBinary);
fs.delete("assets/mynamespace/font/default.json");
```
