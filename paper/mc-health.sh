#!/usr/bin/env sh

# Check if the Minecraft server is listening on port 25565
if netstat -an | grep 25565 | grep LISTEN > /dev/null; then
  echo "Server is listening on port 25565"
  exit 0
else
  echo "Server is not listening on port 25565"
  exit 1
fi
