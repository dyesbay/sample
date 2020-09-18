./gradlew build
fuser -k 8808/tcp
nohup java -jar build/libs/expert-1.0.jar > log.log &
ls