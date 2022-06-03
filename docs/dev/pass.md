# Transform Pass (``TransformPass``)
A transform pass processes user's pack data and previous passes generated outputs and put new generated data.

## Making your own transform pass
You would begin with an empty class that's inherts ``TransformPass``:

```java
class MyPass extends TransformPass {
    public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
        // ... Your code here ...
        // Log messages using "logger"
        // Store transformation results using "result"
        // Manipulate files using "fs"
    }
}
```

From here, you can put whatever you want inside ``transform()`` method, which will transforms user's pack data. For example, we will delete ``diamond.png`` and replace it with ``diamond_sword.png``:

```java
// Stop our pass when that file doesn't exists
if (fs.ls("assets/minecraft/textures/item/diamond.png").length == 0) {
    logger.warning("diamond.png doesn't exists, duh");
    return;
}

byte[] data = fs.get("assets/minecraft/textures/item/diamond.png");
fs.put("assets/minecraft/textures/item/diamond_sword.png", data);
```

To register, simply put your pass to ``TransformPass.REGISTRY`` map where the key is your pass type (which is ``type`` field in JSON object) and the value is a function that accepts ``JsonObject`` and returns new transform pass. For convenience, we'll add a new constructor that accepts ``JsonObject`` inside our pass:

```java
// MyPass.java
public MyPass(JsonObject json) {
    // ...You might want to set something inside your pass fields...
}

// Main.java
TransformPass.REGISTRY.put("myPass", MyPass::new);

// ...or you can do this:
TransformPass.REGISTRY.put("myPass", json -> new MyPass(json));
```

## Storing results for programming purpose
To supports programming purpose, we've added ``BundleResult``, which stores transformation results (an exmaple would be ``FontIconsTransformPass``, which you can obtain its result by using ``PackBundler#bundle(...).getOrCreate(GlyphsResult.class, GlyphsResult::new)``).

Adding your own result is pretty easy. Just create a new class that stores your outputs, then inside your ``transform()`` method, you would use ``result.getOrCreate()``:

```java
MyPassResult r = result.getOrCreate(MyPassResult.class, () -> new MyPassResult() /* or MyPassResult::new */);
r.setWhatever = true;
```
