#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ $# -ne 7 ]; then
    echo "Illegal number of parameters. Usage: `basename $0` iserve-host iserve-port iserve-context rdf-host rdf-port repository-name type"
    exit 1
fi

SPARQL_ENDPOINT=$(
  case "$7" in
    ("sesame") echo "http://$4:$5/openrdf-sesame/repositories/$6" ;;
    ("fuseki") echo "http://$4:$5/$6/query" ;;
    (*) echo "$7 is an unknown RDF store. Configure manually."
        exit 1;;
  esac)

#echo "Endpoint: $SPARQL_ENDPOINT"

sed -e "s@%ISERVE_APP_NAME%@$3@g" -e "s@%ISERVE_PATH%@http://$1:$2/$3@g" -e "s@%SPARQL_ENDPOINT%@$SPARQL_ENDPOINT@g" elda-spec-template.ttl > elda-spec-iserve.ttl
echo "Configuration file generated at $DIR/elda-spec-iserve.ttl"