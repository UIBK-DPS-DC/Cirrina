# Cirrina-Pkl

[Pkl](https://pkl-lang.org/) is a configuration as code language with rich validation and tooling.

## Setup

1. Download the jar file:
    ```bash
    curl -o pkl-tools-0.26.2.jar https://repo1.maven.org/maven2/org/pkl-lang/pkl-tools/0.26.2/pkl-tools-0.26.2.jar
    ```

2. Generate the code for the CSM descriptions:
    ```bash
    java -cp pkl-tools-0.26.2.jar org.pkl.codegen.java.Main -o generated/ descriptions/CollaborativeStateMachineDescription.pkl --generate-getters --generate-javadoc
    java -cp pkl-tools-0.26.2.jar org.pkl.codegen.java.Main -o generated/ .\descriptions\JobDescription.pkl --generate-getters --generate-javadoc
    java -cp pkl-tools-0.26.2.jar org.pkl.codegen.java.Main -o generated/ .\descriptions\ServiceImplementationDescription.pkl --generate-getters --generate-javadoc
    java -cp pkl-tools-0.26.2.jar org.pkl.codegen.java.Main -o generated/ .\descriptions\HttpServiceImplementationDescription.pkl --generate-getters --generate-javadoc
    ```

**NOTE:** The code could also be generated with the Gradle plugin, however, there are some issues that need to be fixed first.

## Recommended Editors

For editing Pkl files, we recommend the following editors:

- [VSCode](https://code.visualstudio.com/) for Windows
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) for MacOS or Linux

To improve the editing experience, you can install the recommended plugins for the editors.

