mvn clean install

mkdir -p ~/.local/bin/jar
cp ./target/helper-1.0-SNAPSHOT.jar ~/.local/bin/jar/helper.jar

rm -rf ~/.local/bin/jar/helper-libs
mkdir ~/.local/bin/jar/helper-libs
cp ./target/libs/jackson-*.jar ~/.local/bin/jar/helper-libs
cp ./target/libs/apiguardian-api-*.jar ~/.local/bin/jar/helper-libs

cp ./script/h-rentcalc.sh ~/.local/bin/

chmod u+x ~/.local/bin/h-rentcalc.sh


cp ./script/h-backupdocs.sh ~/.local/bin/

chmod u+x ~/.local/bin/h-backupdocs.sh