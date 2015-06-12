#!/bin/sh

if [ $# -ne 3 ]; then
    echo "Illegal number of parameters."
    echo "Usage: `basename $0` server-url file-or-folder format"
    echo "Format should be one of: n3, ttl, owls, wsdl, swagger, hrests."
    exit 1
fi

SERVER=$1
INPUT=$2

case "$3" in
    ("n3")
        MEDIA_TYPE="text/n3"
        FILE_EXT="n3";;
    ("ttl")
        MEDIA_TYPE="text/turtle"
        FILE_EXT="ttl";;
    ("owls")
        MEDIA_TYPE="application/owl+xml"
        FILE_EXT="owls";;
    ("wsdl")
        MEDIA_TYPE="application/wsdl+xml"
        FILE_EXT="wsdl";;
    ("hrests")
        MEDIA_TYPE="text/html"
        FILE_EXT="html";;
    ("swagger")
        MEDIA_TYPE="application/json"
        FILE_EXT="json";;
    (*) echo "$3 is an unknown type. Nothing will be imported."
        exit 1;;
esac

# Process the input
if [[ -d $INPUT ]]; then
    # It is a folder
    ERRORS=0
    UPLOADS=0
    echo "Importing folder: $INPUT"
    for FILE in $INPUT/*.$FILE_EXT
    do
        if [[ -f $FILE ]]; then
            echo "Importing $FILE with media type $MEDIA_TYPE"
            curl -v -include --form "file=@$FILE;type=$MEDIA_TYPE" $SERVER/id/services
            RET=$?
            if [[ $RET -ne 0 ]]; then
                ERRORS=$[ERRORS + 1]
                if [[ $ERRORS -ge 5 ]]; then
                    echo "Too many errors. Stopping"
                    exit 1
                fi
            else
                UPLOADS=$[UPLOADS + 1]
            fi
        fi
    done

    echo "Imported $UPLOADS file(s)"

elif [[ -f $INPUT ]]; then
    # It is a file
    echo "Importing $INPUT with media type $MEDIA_TYPE"
    curl -v -include --form "file=@$INPUT;type=$MEDIA_TYPE" $SERVER/id/services

else
    echo "$INPUT is not a valid file or folder."
    exit 1
fi

