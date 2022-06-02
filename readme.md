![Multipacks banner](docs%5Cimages%5Cbanner.png)

# Multipacks
Welcome to Multipacks, the resources and data pack packager!

## Using Multipacks CLI
Multipacks CLI distributions can be installed by using ``./gradlew :multipacks-cli:installDist``.

```sh
# Initialize pack
multipacks-cli pack init path/to/pack
vim path/to/pack/multipacks.json

# View pack info
multipacks-cli pack info path/to/pack

# Build and bundle
multipacks-cli pack build path/to/pack --output myResourcesPack.zip --include resources
multipacks-cli pack build path/to/pack --output myDataPack.zip --include data

# Install to repository
multipacks-cli pack install pack/to/pack
ls ~/.multipacks

# List
multipacks-cli list repo
multipacks-cli list pack
```

## Using Multipacks Engine
You can use Multipacks Engine with your project by including ``multipacks-engine`` as dependency.

### Using with Spigot plugin
Beside from shading ``multipacks-engine``, you can also depends on the plugin itself:

```yml
depend: ["Multipacks"]
```
