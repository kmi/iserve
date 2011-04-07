rm -rf ./iserve/*
cp -R ./iserve-discovery-engine/target/iserve-discovery-engine/* ./iserve/
cp -R ./iserve-sal-rest/target/iserve-sal-rest/* ./iserve/
cp -R ./iserve-sal-gwt/target/iserve-sal-gwt-1.0.0-SNAPSHOT/* ./iserve/
cp -R ./web.xml ./iserve/WEB-INF/
cd ./iserve/
jar -cvfM0 iserve.war ./
cd ..
mv ./iserve/iserve.war ./
rm /Users/dl3962/Workspace/apache-tomcat-6.0.29/webapps/iserve.war
rm -rf /Users/dl3962/Workspace/apache-tomcat-6.0.29/webapps/iserve
cp ./iserve.war /Users/dl3962/Workspace/apache-tomcat-6.0.29/webapps/iserve.war
/Users/dl3962/Workspace/apache-tomcat-6.0.29/bin/catalina.sh run

