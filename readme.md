![Multipacks banner](docs/images/banner.png)

# Multipacks
Welcome to Multipacks, the resources and data pack packager!

## Getting Multipacks
Multipacks CLI tool can be obtained in [this Release page](https://github.com/MangoPlex/Multipacks/releases/). Download ``multipacks-cli.zip``, extract it and run ``multipacks-cli`` inside ``bin/`` folder with a terminal to use.

## Using Multipacks
The simplest way to get started with Multipacks is to take a look at our [Quick Start](./docs/quickstart.md). A basic knowledge of [JSON](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Objects/JSON) and terminal is required (well actually, you might not need to know too much JSON. Just look at examples in our documentation or quick start guide and you might know something).

## Use Multipacks for your Java projects
### Using Multipacks Engine
You can use Multipacks Engine with your project by including ``multipacks-engine`` as dependency (TODO: a way to use ``multipacks-engine`` outside ``multipacks`` project?).

### Using with Spigot plugin
Beside from shading ``multipacks-engine``, you can also depends on the plugin itself:

```yml
depend: ["Multipacks"]
```
