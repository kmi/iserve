#!/bin/bash

if [ $# -lt 2 ]; then
    echo "Illegal number of parameters. Usage: `basename $0` rdf-host rdf-port setup"
    echo "Setups supported: in-memory (default) persistent owlim"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TEMPLATE=$DIR/repositories-config/sesame-rdfs.ttl 

# Check if persistent
if [ $# -eq 3 ]; then
	
	TEMPLATE=$(
	  case "$3" in
	    ("in-memory") echo "$DIR/repositories-config/sesame-rdfs.ttl" ;;
	    ("persistent") echo "$DIR/repositories-config/native-rdfs-dt.ttl" ;;
	    ("owlim") echo "$DIR/repositories-config/owlim-owl-horst-optimized.ttl" ;;
	    (*) echo "$3 is an unknown setup. Configure manually."
	        exit 1;;
	  esac)
fi

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

echo "iServe repository created"


