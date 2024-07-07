FROM itzg/mc-proxy

# Switch to root to configure the system
USER root

# Create docker group with ID 955 and add bungeecord user to it
RUN groupadd -g 955 docker || true \
    && usermod -aG docker bungeecord

# Fix permissions for the /server directory
RUN chown -R bungeecord:bungeecord /server \
    && chmod -R 775 /server

# Switch back to the default user
USER bungeecord