# Transformations
To make more advanced packs without spending too much time (hopefully), Multipacks introduces _packs transformations_. These transformations will read your files, apply some magic then put it inside ``.zip`` file.

## transformations.json
To add transforms, you need to create ``transformations.json`` inside your pack folder:

```json
[
    {
        "type": "transform-type-#1",
        "someStuffs": "abcxyz"
    },
    {
        "type": "another-transform"
    },
    { "...": "..." }
]
```

The file contains an array as top-level object, with JSON objects for each elements. All of those objects must have ``type`` field, with the key points to transformation type:

```json
{
    "type": "font-icons"
}
```

## All transformations
- [Font Icons: ``font-icons``](font-icons.md)
