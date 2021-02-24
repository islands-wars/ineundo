# <img src="https://raw.githubusercontent.com/islands-wars/guidelines/master/assets/icon.png" width="64"> Islands Wars - Ineundo

> Islands Wars is a Minecraft multiplayer server network.

> This project is a velocity plugin to handle player connection.

---

# Contributing

Please refer to this [file](https://github.com/islands-wars/guidelines/blob/master/README.md) when contributing to ensure everything is setup correctly. Don't forget to use our code style and header,
write good commit message and use Java conventions. Ensure your code compile and your tests run.

---

# Debugging and setup

In order to setup a live proxy, you need to run a few gradle tasks.

- ``gradlew setupServer`` will create serv folder and download velocity.
- ```gradlew startDevServer``` will launch velocity in a gradle java process.

You can test your code by running ``gradlew deployPlugin``.

---
# Docker

To create a local docker image, make sure to execute ``gradlew setupServer`` first, then run :

```docker build . -t ineundo[:tag]```

You can run this image with custom jvm arguments through env var.

```docker run -d --name is_proxy -e JVM_ARGS="" ineundo[:tag]```

See this [line](https://github.com/islands-wars/ineundo/blob/master/core/build.gradle#L11) for a recommended jvm tuning.

---
# Wiki

todo

---

# Javadoc

todo

---

# License

---

> GNU GENERAL PUBLIC LICENSE Version 3, see [LICENSE](https://github.com/islands-wars/islands/blob/master/LICENSE) file.