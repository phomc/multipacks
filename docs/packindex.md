# Pack Index (``multipacks.json``)
In order to get Multipacks knows your pack info, you need to add ``multipacks.json`` inside your pack folder, which we will call it "pack index". This index contains your pack id, pack name, targetted game version and dependencies.

## Structure
```json
{
    "id": "packid",
    "name": "Pack Name",
    "author": "Your Name!",
    "description": "Pack Description",
    "version": "1.0.0",
    "gameVersion": ">=1.19",
    "include": {
        "otherpackid": ">=1.0.0",
        "anotherpackid": "<15.1.2",
        "...": "..."
    },
    "ignore": [
        ".gitignore",
        ".git*"
    ]
}
```

### ``id`` (Required)
This is your pack id. This value shouldn't have any spaces to ensure that your pack repository won't be corrupted (You can actually use spaces but it's not recommended).

### ``name``, ``author`` and ``description`` (Optional)
Some basic metadata stuffs. These informations will be shown with ``multipacks-cli pack info`` command and maybe some packs explorer.

### ``version`` (Required)
This is your pack version (not game version, which is in ``gameVersion`` field). This one is required for dependencies resolution to works.

### ``gameVersion`` (Required)
The targetted game version. Putting ``>``, ``<``, ``>=`` or ``<=`` doesn't really do anything... for now.

### ``include`` (Optional)
This field is a JSON object, with its key as pack id and its value is a version filter. ``>``, ``<``, ``>=`` and ``<=`` version prefixes does works so you can target any pack versions in the range.

### ``ignore`` (Optional)
An array that contains all paths to ignore. By default, Multipacks will excludes these files from package:
- ``.git/*``
- ``.gitignore``
- Whatever that starts with ``.git``, really.
