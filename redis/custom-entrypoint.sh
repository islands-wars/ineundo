#!/bin/sh

# Paths to the secrets files
REDIS_USERNAME_FILE=/run/secrets/redis_username
REDIS_PASSWORD_FILE=/run/secrets/redis_password

# Read the contents of the secrets files
REDIS_USERNAME=$(cat $REDIS_USERNAME_FILE)
REDIS_PASSWORD=$(cat $REDIS_PASSWORD_FILE)

# Set up Redis configuration directory
mkdir -p /usr/local/etc/redis

# Dynamically generate Redis configuration and ACL files
echo "aclfile /usr/local/etc/redis/custom_aclfile.acl" > /usr/local/etc/redis/redis.conf

# Generate ACL file using secrets contents
if [ -n "$REDIS_USERNAME" ] && [ -n "$REDIS_PASSWORD" ]; then
    echo "user $REDIS_USERNAME on allkeys allchannels allcommands >$REDIS_PASSWORD" > /usr/local/etc/redis/custom_aclfile.acl
fi

# Disable default user
if [ "$REDIS_DISABLE_DEFAULT_USER" = "true" ]; then
    echo "user default off nopass nocommands" >> /usr/local/etc/redis/custom_aclfile.acl
fi

# Call the original Docker entrypoint script with redis-server and the path to the custom Redis configuration
exec docker-entrypoint.sh redis-server /usr/local/etc/redis/redis.conf