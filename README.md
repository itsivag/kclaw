````markdown
# kclaw

`kclaw` is a Kotlin-based terminal application built using Clikt.  
It provides a clean and extensible CLI interface for running and managing kclaw workflows directly from your terminal.

---

## Prerequisites

- Java 17
- Gradle (or use the included Gradle wrapper `./gradlew`)
- macOS or Linux (for global installation steps below)

Verify Java:

```bash
java -version
````

---

## Build
To create an installable distribution:

```bash
./gradlew installDist
```

---

## Global Installation (Mac/Linux)

After running `installDist`, navigate to the generated binary directory:

```bash
cd build/install/kclaw/bin
```

Set required environment variables:

```bash
export GOOGLE_API_KEY={api}
export TAVILY_API_KEY={api}
```

Run the CLI:

```bash
./kclaw onboard
./kclaw start
```

---

## Optional: Add to PATH

To use `kclaw` globally:

```bash
export PATH="$PATH:$(pwd)"
```

Or move it to a global location:

```bash
sudo mv kclaw /usr/local/bin/
```

---

## Development

Run without installation:

```bash
./gradlew run
```

---

## Tech Stack

* Kotlin
* Clikt
* Gradle

---

# License

```
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                    Version 2, December 2004

 Copyright (C) 2004 Sam Hocevar
 14 rue de Plaisance, 75014 Paris, France
 Everyone is permitted to copy and distribute verbatim or modified
 copies of this license document, and changing it is allowed as long
 as the name is changed.

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

  0. You just DO WHAT THE FUCK YOU WANT TO.
```
