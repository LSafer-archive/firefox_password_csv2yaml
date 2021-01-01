# firefox_password_csv2yaml
Firefox exports userpasswords into a `.csv` file. So, this project converts the csv file into a yaml file.


# How to use?
Compile then run :)

# How to compile then run?
There is 2 methods: 
- Use `javac csv2yaml.java` then `java csv2yaml`
- Use `jrun csv2yaml` (using the [jrun](https://github.com/lsafer/jrun) file)

# Default function
Generates a file with all the entities in the original `csv` file.
The output will be:
- `<name>.yaml` contains all the entities
- `<name> malformed.yaml` contains the entities the app could not parse.

Standard: 
- `javac csv2yaml.java&java csv2yaml <name>`
Using JRun:
- `jrun csv2yaml <name>`

# Filter function
Does everything the default function does + A file with filtered entities.
The outpuut will be:
- `<name>.yaml` contains all the entities
- `<name> malformed.yaml` contains the entities the app could not parse.
- `<name> <regex>.yaml` contains any entity that have a value that contains a sequence that matches the regex.

Standard:
- `javac csv2yaml.java&java csv2yaml <name> <regex>`
Using JRun:
- `jrun csv2yaml <name> <regex>`
