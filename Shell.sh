#Download "Bluefin Web portal services" artifact

mvn dependency:get -DremoteRepositories=http://phxioicobldd02.internal.mcmcg.com:8081/nexus/content/repositories/snapshots/ -Dartifact=com.mcmcg.gbs.bluefin:bluefin-web-portal-services:$Artifact_Version -Dpackaging=war -Ddest=./bluefin-wp-services.war

#Download "Bluefin Web portal services" artifact