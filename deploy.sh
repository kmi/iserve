if [ -z "$TOMCAT_DIR" ]
then
    TOMCAT_DIR="/Users/dl3962/Workspace/apache-tomcat-6.0.29/"
fi

rm -rf ./iserve
mkdir ./iserve
echo copying disco engine
cp -R ./iserve-discovery-engine/target/iserve-discovery-engine/* ./iserve/
echo copying sal rest
cp -R ./iserve-sal-rest/target/iserve-sal-rest/* ./iserve/
echo copying sal gwt
cp -R ./iserve-sal-gwt/target/iserve-sal-gwt-1.0.0-SNAPSHOT/* ./iserve/
cp -R ./web.xml ./iserve/WEB-INF/
cd ./iserve/
echo making war
jar -cfM0 iserve.war ./
cd ..
mv ./iserve/iserve.war ./

if [ -z "$ONLYMAKE" ]
then
    rm "$TOMCAT_DIR"/webapps/iserve.war
    rm -rf "$TOMCAT_DIR"/webapps/iserve
    cp ./iserve.war "$TOMCAT_DIR"/webapps/iserve.war
    "$TOMCAT_DIR"/bin/catalina.sh run
fi

