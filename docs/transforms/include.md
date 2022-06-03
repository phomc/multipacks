# Transformations: Include
```json
{
    "type": "include",
    "include": "my-transformations-chain.json"
}
```

## Properties
### ``include`` (Required)
Include transformations chain from other JSON file. The file format is exactly the same as 3[``transformations.json``](index.md#transformationsjson)

## Recursion warning
If you ended up including the file itself, there will be recursions, which causes the entire packaging process fail. Avoid using ``include`` as much as possible, unless your main transformations chain is too long.
