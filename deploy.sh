rm -rf ./iserve2/*
cp -R ./iserve2-discovery-engine/target/iserve2-discovery-engine/* ./iserve2/
cp -R ./iserve2-sal-rest/target/iserve2-sal-rest/* ./iserve2/
cp -R ./iserve2-sal-gwt/target/iserve2-sal-gwt-0.0.1-SNAPSHOT/* ./iserve2/
cp -R ./web.xml ./iserve2/WEB-INF/
cd ./iserve2/
jar -cvfM0 iserve2.war ./
cd ..
mv ./iserve2/iserve2.war ./
rm /Users/dl3962/Workspace/apache-tomcat-6.0.29/webapps/iserve2.war
rm -rf /Users/dl3962/Workspace/apache-tomcat-6.0.29/webapps/iserve2
cp ./iserve2.war /Users/dl3962/Workspace/apache-tomcat-6.0.29/webapps/iserve2.war
/Users/dl3962/Workspace/apache-tomcat-6.0.29/bin/catalina.sh run

