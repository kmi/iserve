#!/bin/bash

if [ $# -ne 2 ]; then
    echo "Illegal number of parameters. Usage: `basename $0` rdf-host rdf-port"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Creating iServe repository in $1:$2"

curl -X POST -H "Content-Type:application/x-turtle" -T $DIR/repositories-config/sesame-rdfs.ttl http://$1:$2/openrdf-sesame/repositories/SYSTEM/rdf-graphs/service?graph=http://iserve.kmi.open.ac.uk/data#g1
RET=$?
while [[ RET -ne 0 ]]; do
    echo "=> Waiting for confirmation of Sesame Repository creation..."
    sleep 5
    curl -X POST -H "Content-Type:application/x-turtle" -T $DIR/repositories-config/sesame-rdfs.ttl http://$1:$2/openrdf-sesame/repositories/SYSTEM/rdf-graphs/service?graph=http://iserve.kmi.open.ac.uk/data#g1
    RET=$?
done

curl -X POST -H "Content-Type:application/x-turtle" -d "<http://iserve.kmi.open.ac.uk/data#g1> a <http://www.openrdf.org/config/repository#RepositoryContext>." http://$1:$2/openrdf-sesame/repositories/SYSTEM/statements
RET=$?
while [[ RET -ne 0 ]]; do
    curl -X POST -H "Content-Type:application/x-turtle" -d "<http://iserve.kmi.open.ac.uk/data#g1> a <http://www.openrdf.org/config/repository#RepositoryContext>." http://$1:$2/openrdf-sesame/repositories/SYSTEM/statements
    RET=$?
    echo "=> Waiting for confirmation of Context creation..."
    sleep 5
done

echo "Repository created"

