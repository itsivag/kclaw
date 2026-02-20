# kclaw

`kclaw` is a Kotlin-based terminal application built using [Clikt](https://ajalt.github.io/clikt/).

## Prerequisites
- Java 17
- Gradle

## Build and Run
To build the application and run the `helloworld` command:

```bash
./gradlew run --args="helloworld"
```

## Global Installation (Mac/Linux)
To install the `kclaw` command locally so it can be run from anywhere in your terminal:

1. Build the installation distribution:
```bash
./gradlew installDist
```

2. Create a symlink to a local binaries directory (like `~/bin`):
```bash
mkdir -p ~/bin
ln -sf $(pwd)/build/install/kclaw/bin/kclaw ~/bin/kclaw 
```

3. Ensure that `~/bin` is in your system's `PATH`. If you are using ZSH (default on macOS), add the following to your `~/.zshrc`:
```bash
export PATH="$HOME/bin:$PATH"
```

4. Reload your shell or run `source ~/.zshrc`. Now you can use `kclaw` globally:
```bash
kclaw helloworld
```
