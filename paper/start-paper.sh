#!/bin/bash
# Replace placeholders in the configuration files with environment variables
envsubst '${VELOCITY_SECRET}' < /server/config/paper-global.yml.template > /server/config/paper-global.yml

# Start the PaperMC server
exec java $JAVA_OPTS -jar server.jar "$@"
