#!/bin/bash

if [ $# -lt 2 ]; then
    echo "Illegal number of parameters. Usage: `basename $0` rdf-host rdf-port"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TEMPLATE=$DIR/repositories-config/owlim-owl-horst-optimized.ttl

echo "Creating iServe repository in $1:$2"

curl -X POST -H "Content-Type:application/x-turtle" -T $TEMPLATE http://$1:$2/openrdf-sesame/repositories/SYSTEM/rdf-graphs/service?graph=http://iserve.kmi.open.ac.uk/data#g1
RET=$?
while [[ RET -ne 0 ]]; do
    echo "=> Waiting for confirmation of Sesame Repository creation..."
    sleep 5
    curl -X POST -H "Content-Type:application/x-turtle" -T $TEMPLATE http://$1:$2/openrdf-sesame/repositories/SYSTEM/rdf-graphs/service?graph=http://iserve.kmi.open.ac.uk/data#g1
    RET=$?
done

curl -X POST -H "Content-Type:application/x-turtle" -d "<http://iserve.kmi.open.ac.uk/data#g1> a <http://www.openrdf.org/config/repository#RepositoryContext>." http://$1:$2/openrdf-sesame/repositories/SYSTEM/statements
RET=$?
while [[ RET -ne 0 ]]; do
    echo "=> Waiting for confirmation of Context creation..."
    sleep 5
    curl -X POST -H "Content-Type:application/x-turtle" -d "<http://iserve.kmi.open.ac.uk/data#g1> a <http://www.openrdf.org/config/repository#RepositoryContext>." http://$1:$2/openrdf-sesame/repositories/SYSTEM/statements
    RET=$?
done

echo "Configuring free text indexing..."

curl -X POST -d "update=INSERT+DATA+%7B%0A++%3Chttp%3A%2F%2Fwww.ontotext.com%2Fowlim%2Flucene%23include%3E+%3Chttp%3A%2F%2Fwww.ontotext.com%2Fowlim%2Flucene%23setParam%3E+%22literal+uri%22+.%0A++%3Chttp%3A%2F%2Fwww.ontotext.com%2Fowlim%2Flucene%23index%3E+%3Chttp%3A%2F%2Fwww.ontotext.com%2Fowlim%2Flucene%23setParam%3E+%22literals%2C+uri%22+.%0A++%3Chttp%3A%2F%2Fwww.ontotext.com%2Fowlim%2Flucene%23moleculeSize%3E+%3Chttp%3A%2F%2Fwww.ontotext.com%2Fowlim%2Flucene%23setParam%3E+%221%22+.%0A++%3Chttp%3A%2F%2Fwww.ontotext.com%2Fowlim%2Flucene%23entityIndex%3E+%3Chttp%3A%2F%2Fwww.ontotext.com%2Fowlim%2Flucene%23createIndex%3E+%22true%22+.%0A%7D%0A" http://$1:$2/openrdf-sesame/repositories/iserve/statements

echo "iServe repository created"


