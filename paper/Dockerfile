# Stage 1: Build the PaperMC server jar and EULA
FROM eclipse-temurin:21-jre AS builder

# Set environment variables
ENV PROJECT="paper"
ENV MINECRAFT_VERSION="1.20.6"

# Install necessary packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget jq && \
    rm -rf /var/lib/apt/lists/*

# Create the server directory
RUN mkdir -p /server

# Set the working directory
WORKDIR /server

# Copy the download script into the container
COPY run-paper.sh /server/run-paper.sh

# Make the script executable
RUN chmod +x /server/run-paper.sh

# Run the script to download the PaperMC jar
RUN /server/run-paper.sh && \
    [ -f server.jar ] || (echo "Failed to download server.jar" && exit 1)

# Run the server once to generate necessary files and accept the EULA
# Note: This command is only used for the build process; it does not actually start the server for gameplay.
RUN java -jar server.jar || true && \
    echo "eula=true" > eula.txt



# Stage 2: Create the final production image
FROM eclipse-temurin:21-jre

# Set environment variables
ENV JAVA_OPTS="-Xms4G -Xmx4G -XX:+AlwaysPreTouch -XX:+DisableExplicitGC -XX:+ParallelRefProcEnabled -XX:+PerfDisableSharedMem -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1HeapRegionSize=8M -XX:G1HeapWastePercent=5 -XX:G1MaxNewSizePercent=40 -XX:G1MixedGCCountTarget=4 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1NewSizePercent=30 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:G1ReservePercent=20 -XX:InitiatingHeapOccupancyPercent=15 -XX:MaxGCPauseMillis=200 -XX:MaxTenuringThreshold=1 -XX:SurvivorRatio=32 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true"

# Define environment variable for the Velocity secret
ENV VELOCITY_SECRET="jfei7Ua34jhQ"

# Install necessary packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends net-tools gettext-base && \
    rm -rf /var/lib/apt/lists/*
    
# Health check file
COPY mc-health.sh /usr/local/bin/mc-health.sh
RUN chmod +x /usr/local/bin/mc-health.sh

# Create a user and group for running the server
RUN groupadd -g 1000 minecraft && \
    useradd -m -u 1000 -g minecraft islandswars

# Create the server directory
RUN mkdir -p /server

# Set the working directory
WORKDIR /server

# Copy the PaperMC jar and eula.txt from the builder stage
COPY --from=builder /server/server.jar /server/server.jar
COPY --from=builder /server/eula.txt /server/eula.txt
COPY --from=builder /server/cache/ /server/cache/

COPY spigot.yml /server/spigot.yml
COPY bukkit.yml /server/bukkit.yml
COPY commands.yml /server/commands.yml
COPY bstatsconfig.yml /server/plugins/bStats/config.yml
COPY config/ /server/config/
COPY server.properties /server/server.properties

# Set velocity key
RUN envsubst '${VELOCITY_SECRET}' < /server/config/paper-global.yml.template > /server/config/paper-global.yml

# Fix permissions for plugins directory and ensure .paper-remapped directory can be created
RUN chown -R islandswars:minecraft /server/plugins && \
    chmod -R 755 /server/plugins

# Copy the wrapper script into the container
COPY start-paper.sh /server/start-paper.sh
RUN chmod +x /server/start-paper.sh

# Define the health check
HEALTHCHECK --start-period=12s --interval=5s --retries=12 CMD /usr/local/bin/mc-health.sh

# Expose the default Minecraft server port
EXPOSE 25565

# Set permissions
RUN chown -R islandswars:minecraft /server && \
    chmod -R 755 /server

# Switch to the islandswars user
USER islandswars

# Use the wrapper script as the entrypoint
ENTRYPOINT ["/server/start-paper.sh"]

# Default command to run the PaperMC server with `--nogui` flag
CMD ["--nogui"]

# Specify the signal to send to the process when stopping the container
STOPSIGNAL SIGTERM
