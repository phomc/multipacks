# Transformations: Overlays
```json
{
    "type": "overlay",
    "target": "assets/mynamespace/textures/item",
    "overlays": [
        {
            "path": "assets/mynamespace/textures/overlays/myoverlay.png",
            "x": 0, "y": 0
        },
        {
            "path": "assets/mynamespace/textures/overlays/anotheroverlay.png",
            "x": 8, "y": 8
        }
    ]
}
```

## Properties
### ``target`` (Required)
The target image that will be overlayed.

### ``overlays`` (Required)
An array of overlays declarations, with each element contains the overlay image path and overlay top-left corner location:

```json
{
    "path": "myoverlay.png",
    "x": 0,
    "y": 0
}
```
