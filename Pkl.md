# Cirrina-Pkl

## Setup

1. Download the jar file:

```bash
curl -o pkl-tools-0.26.2.jar https://repo1.maven.org/maven2/org/pkl-lang/pkl-tools/0.26.2/pkl-tools-0.26.2.jar
```

2. Generate the code for the CSM description:

```bash
java -cp pkl-tools-0.26.2.jar org.pkl.codegen.java.Main -o generated/ description/CollaborativeStateMachineDescription.pkl --generate-getters --generate-javadoc
```

**NOTE:** The code could also be generated with the Gradle plugin, however, there are some issues that need to be fixed first.
