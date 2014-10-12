#!/bin/bash

# Launch tomcat 
/run.sh &

# Wait for it
until [ "`curl --silent --show-error --connect-timeout 1 -I http://${RDFSTORE_PORT_8080_TCP_ADDR}:8080 | grep 'Coyote'`" != "" ];
do
  echo "--- sleeping for 10 seconds"
  sleep 10
done

echo "Tomcat is ready!"

# Setup the store 
/opt/iserve/scripts/setup-sesame.sh

