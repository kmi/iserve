#!/bin/sh

#####
# Simple script for setting up RDF Repositories for hosting iServe
# Arguments -s rdf-server -c config-file -r repo-name
#####

usage()
{
cat << EOF
usage: $0 options

This script create the given rdf repository in sesame.

OPTIONS:
   -c      The configuration file to use for the repository
   -h      Show this message
   -r      The repository name
   -s      The Sesame repository URL, e.g., http://localhost:8080/openrdf-sesame/

EOF
}

SERVER=
REPO=
CONFIG=
while getopts “:hs:r:c:” OPTION
do
     case $OPTION in
         h)
             usage
             exit 1
             ;;
         s)
             SERVER=$OPTARG
             ;;    
         r)
             REPO=$OPTARG
             ;;
         c)
             CONFIG=$OPTARG
             ;;
         ?)
             usage
             exit
             ;;
     esac
done

if [[ -z $SERVER ]] || [[ -z $REPO ]] || [[ -z $CONFIG ]]
then
     usage
     exit 1
fi

echo "Creating repository ${REPO} \n in the Sesame server at ${SERVER} \n based on the configuration file ${CONFIG} \n "

# Create the repository, see http://owlim.ontotext.com/display/OWLIMv51/OWLIM+FAQ

# Upload the configuration file
curl -X POST -H "Content-Type:application/x-turtle" \
	-T ${CONFIG} \
    ${SERVER}/repositories/SYSTEM/rdf-graphs/service?graph=http://iserve.kmi.open.ac.uk#${REPO}

# Update the system repository
curl -X POST -H "Content-Type:application/x-turtle" \
    -d "<http://iserve.kmi.open.ac.uk#${REPO}> a <http://www.openrdf.org/config/repository#RepositoryContext>." \
    ${SERVER}/repositories/SYSTEM/statements
    
echo "\nRepository created! Enjoy. \n"    