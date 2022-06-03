# Multipacks: Quick Start guide
## Before you begin
Make sure you have
- Java 17
- A terminal (Powershell, Bash or whatever that can run commands)
- Any plain text editor (Microsoft Notepad, Notepad++, vim, etc...)
- Knowledge about JSON basics

## Getting Multipacks CLI
Go to [this Release page](https://github.com/MangoPlex/Multipacks/releases/) and download ``multipacks-cli.zip``, then extract the archive to somewhere else (in your Desktop for example).

## Multipacks CLI
Once you've extracted the zip file, open a new terminal inside ``multipacks-cli/bin``. You'll see these messages when you run ``./multipacks-cli``:

```console
$ ./multipacks-cli
Multipacks CLI
Usage: java multipacks.cli.Main <subcommand...> [options...]
Subcommands:
  list            List repositories or packs
  pack            Pack command

Options:
  -F  --filter <ID> [>=, >, <=, <]<Version>
        Set filter for querying packs
  ... Output trimmed ...
```

Type ``./multipacks-cli <Subcommand>`` to view its help, such as ``pack``:

```console
$ ./multipacks-cli pack
No subcommand for 'pack'
All subcommands for 'pack':
  init [path]     Create an empty Multipacks pack
  info [path]     Gather Multipacks pack informations
  build [path]    Build Multipacks pack to game resources or data pack
  install [path]  Install Multipacks pack to selected repository
```

## Create new Multipacks pack
Simply run ``./multipacks-cli pack init path/to/pack`` (with ``path/to/pack`` points to your pack location) to create a new Multipacks pack:

```sh
# Unix
./multipacks-cli pack init ./myPack # Generate inside myPack
./multipacks-cli pack init .        # Generate at current folder
```

```bat
rem Windows
.\multipacks-cli pack init C:\Users\YourUsername\Desktop\myPack
.\multipacks-cli pack init .
```

## Edit your pack
Open ``myPack/`` and you will see all the pack contents, including resources pack and data pack. You can add your own textures inside ``assets/`` and recipes + world generators inside ``data/``.

### multipacks.json
``multipacks.json`` is the index of your pack, which contains your pack id, pack name and its description, pack version, targetted game version and pack dependencies. The JSON should look like this:

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
    }
}
```

For more information, please look at [Pack Index](packindex.md) page.

### License file
You can also add ``LICENSE`` or ``LICENSE.md`` inside your pack folder to add a license. Your license text will be located in ``pack.zip/licenses/packid``.

## Pack it up!
Once you've done with your Multipacks pack, you can pack it into a single ``.zip`` file with ``./multipacks-cli pack build`` command:

```sh
# Build packid-v1.0.0.zip at current folder
./multipacks-cli pack build path/to/pack

# Build pack into path/to/output.zip
./multipacks-cli pack build path/to/pack --output path/to/output.zip

# Build 2 packs separately
./multipacks-cli pack build path/to/pack --output path/to/output-resources.zip --include resources
./multipacks-cli pack build path/to/pack --output path/to/output-data.zip --include data
```

## Ok but what's next?
You've created a pack using Multipacks! You can make more advanced packs with pack [transformations](transforms/index.md), or you can combine pre-existing packs to modify some textures without directly modifying the resources pack itself.
