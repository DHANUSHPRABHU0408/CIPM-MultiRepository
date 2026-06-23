# Commit-Based Continuous Integration of architectural Performance Models

This repository provides the prototypical implementation for the change extraction, change propagation, incremental model update, and adaptive instrumentation of the [CIPM approach](https://sdq.kastel.kit.edu/wiki/CIPM).

# Setup
**Note: the setup is still in development.**

This project requires Java 11 for all actions. In particular, if a script is executed in the following, it usually uses Maven to build projects. As a result, Maven must be able to locate and use a JDK 11.

When executing one of the scripts, it is possible that the error `Internal error: java.lang.IllegalArgumentException: bundleLocation not found: [home]/.m2/[...]` occurs. In such a case, it can help to delete the file `[home]/.m2/repository/.meta/p2-artifacts.properties` and restart the script.

## Setup with Eclipse

With the following steps, the project can be setup within Eclipse. As a reminder, this project requires Java 11. As Eclipse version, the Eclipse Modeling Tools 2022-09 are currently required. It also requires the installation of Xtext (from `https://download.eclipse.org/releases/2022-09/`), Vitruv (from `https://vitruv-tools.github.io/updatesite/release/change`, `https://vitruv-tools.github.io/updatesite/release/dsls`, `https://vitruv-tools.github.io/updatesite/release/framework`), and CIPM Metamodels (from `https://CIPM-tools.github.io/updatesite/release/cipm-aggr-metamodels-1.0.1/`).

1. Execute the script `scripts/setup.bat` (under Windows) or `scripts/setup` (under Linux-based systems).

1. In parallel, within Eclipse, install the required plugins for Xtext, Vitruv, and CIPM Metamodels. In Eclipse, use the menu `Help -> Install New Software`, and enter a URL from above into `Work with:`. Then, select the Xtext SDK (you can search for `Xtext`) for installing Xtext, select all plugins except `Vitruv Demo Metamodels`, `Vitruv Demos`, and `Vitruv Tool Adapters` for installing Vitruv, and select all plugins for installing the CIPM Metamodels. Continue each time with the installation of the plugins.

1. After executing the script, all bundles from `commit-based-cipm/bundles/fi`, `commit-based-cipm/bundles/si`, `commit-based-cipm/bundles/Vitruv/bundles`, `commit-based-cipm/releng-dev`, and `commit-based-cipm/tests` can be imported into the Eclipse instance. The `releng-dev` directory contains the bundle `cipm.consistency.targetplatform` with the `cipm.consistency.targetplatform.target` file. Within Eclipse, open this file and click on `Set as active target platform`. Wait until the target platform is set, loaded, and the plugins are successfully compiled. It is possible that the target platform needs to be reloaded.

## About the Internal Structure of the Setup

The current build process provides a replicable build. Therefore, dependencies are provided via Eclipse P2 Update Sites with fixed versions or via Git submodules. In particular, the submodules include a specialized older Vitruv version. As the submodules contain source code, they need to be compiled after cloning the repository or if they are cleaned. The build process contains necessary steps to build the submodules.

<!--
The Reactions language from Vitruv detects the meta-models from Vitruv domains. To find the Vitruv domains, the corresponding lookup mechanisms in Eclipse and in the build process require the domains and domain providers to be built and to be on the classpath. As a consequence, a separation between the bundles is performed. The first half of the bundles (located in `commit-based-cipm/bundles/fi`, also imported into the first Eclipse instance) contain two domains (one for the instrumentation model and one adjusted domain for Java) so that they are built in a first build step and for the second Eclipse instance. In the second build step and in the second Eclipse instance, the built domains can be found by the Reactions language.

Notes on generating code for Reactions in the build process: 

1. The plugin containing Reactions requires a `.maven_enable_dsls-generation` file in the plugin directory (the `[plugin name]` directory, not `[plugin name]/src`). 

2. Furthermore, all classes or methods imported within a Reactiosn file cannot be located in the same plugin as the Reactions. They need to be in separated plugins.

The manual to execute the pipeline with TeaStore or TEAMMATES can be found [here](commit-based-cipm/tests/cipm.consistency.vsum.test). Reference PCM repository models for these executions can be found [here](data).
-->
