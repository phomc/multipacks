# Transformations: Multiple Sprites
```json
{
    "type": "multi-sprites",
    "from": "assets/mynamespace/textures/block",
    "to": "assets/mynamespace/textures/block",
    "templates": {
        "_up": { "x": 16, "y": 0, "width": 16, "height": 16 },

        "_east": { "x": 0, "y": 16, "width": 16, "height": 16 },
        "_north": { "x": 16, "y": 16, "width": 16, "height": 16 },
        "_west": { "x": 32, "y": 16, "width": 16, "height": 16 },
        "_south": { "x": 48, "y": 16, "width": 16, "height": 16 },

        "_down": { "x": 16, "y": 32, "width": 16, "height": 16 }
    }
}
```

## Properties
### ``from`` and ``to`` (Required)
Source and destination folder of the images.

### ``templates`` (Required)
An object with its key as file name suffix and the value is an another JSON object that describes the rectangle area to cut. The result image will be ``<to>/<orignal filename><suffix>.png`` (for example, ``_up`` would generates ``<to>/mytexture_up.png``).

```json
{
    "x": 0,
    "y": 0,
    "width": 16,
    "height": 16
}
```

An area outside of the source image would yields transparent pixel.
