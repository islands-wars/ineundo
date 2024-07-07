#!/bin/bash

# Read the username and password from Docker secrets
RMQ_USERNAME=$(cat /run/secrets/rmq_username)
RMQ_PASSWORD=$(cat /run/secrets/rmq_password)

# Create the rabbitmq.conf file with the custom configuration
cat <<EOF > /etc/rabbitmq/rabbitmq.conf
listeners.tcp.default = 5672
default_user = ${RMQ_USERNAME}
default_pass = ${RMQ_PASSWORD}
EOF

# Execute the original entrypoint of RabbitMQ
exec docker-entrypoint.sh rabbitmq-server