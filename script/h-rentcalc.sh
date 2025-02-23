#/bin/bash
IS_MOUNTED=`mount | grep /Volumes/home | wc -l`

if [ $IS_MOUNTED -eq "0" ]; then
    echo Please mount /Volumes/home samba storage first
    echo mount /Volumes/home
    exit -1
fi

java -cp "$HOME/.local/bin/jar/helper.jar:$HOME/.local/bin/jar/helper-libs/*" io.github.progoza.helper.App

STORED_MD_FULLPATH=`cat /tmp/last-rentcalc-file.txt`
FILENAME=$(basename -- "$STORED_MD_FULLPATH")
FILENAME="${FILENAME%.*}"

pandoc -o /Volumes/home/Documents/wynajem/pdf/${FILENAME}.pdf $STORED_MD_FULLPATH

rm $STORED_MD_FULLPATH
rm /tmp/last-rentcalc-file.txt
