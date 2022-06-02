# Transformations: Font Icons
```json
{
    "type": "font-icons",
    "source": [
        "assets/mynamespace/fonticons",
        "..."
    ],
    "target": {
        "font": "mynamespace:myfont",
        "textures": "mynamespace:font/myfont",
        "mapping": [
            { "from": "A", "to": "Z" },
            { "from": "0", "to": "9" },
            { "from": "\ue000", "to": "\uffff" },
            { "...": "..." }
        ]
    }
}
```

## Properties
### ``source`` (Required)
A list of paths that points to folders. Those folders must contains valid JSON format, which will be described below. You can have more than 1 source folder.

### ``target``: ``font`` (Required)
The output font, which is used by Minecraft JSON Chat (``{"font": "mynamespace:myfont"}`` for example). If you set this value to ``minecraft:default``, you'll modify the main game font directly.

### ``target``: ``textures`` (Required)
The output textures location. For location ``mynamespace:font/myfont``, the actual path will be ``assets/mynamespace/textures/font/myfont/*.png``

### ``target``: ``mapping`` (Required)
The characters mapping array. You must have at least 1 entry to map an icon to character.

### ``target``: ``charsMapping`` (Optional)
Specify the path for JSON file to generate, which can be read directly from applications (icons picker, Spigot plugin, etc).

## Source JSON format
Example for ``assets/mynamespace/fonticons/myicon.json``:

```json
{
    "id": "mynamespace:myiconid",
    "texture": "mynamespace:fonticons/myicontexture",
    "ascent": 8,
    "height": 8
}
```

### ``id`` (Optional)
The font icon id, which is needed for ``charsMapping`` to generates JSON file. We recommend setting this value, otherwise you'll get randomized name when unpack your ``.zip`` file (and you won't get your font icon inside ``charsMapping`` JSON file).

### ``texture`` (Required)
The location of your font texture. Unlike other kind of locations, you can specify outside ``assets/namespace/textures/`` folder. So ``mynamespace:fonticons/myicontexture`` would points to ``assets/mynamespace/fonticons/myicontexture.png``

### ``ascent`` and ``height`` (Required)
Font features. You may want to set font ascent equals to your icon height if you want your icon to stays on the line. The recommended ascent for both texture and JSON file is 8.
