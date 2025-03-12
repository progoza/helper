#/bin/bash
IS_MOUNTED=`mount | grep /Volumes/home | wc -l`

if [ $IS_MOUNTED -eq "0" ]; then
    echo Please mount /Volumes/home samba storage first
    echo mount /Volumes/home
    exit -1
fi

SILENT=
if [ "$1" == "-silent" ] ; then
   SILENT=$1
fi

java -cp "$HOME/.local/bin/jar/helper.jar:$HOME/.local/bin/jar/helper-libs/*" io.github.progoza.helper.App $SILENT

LAST_RENTCALC_FILE=/tmp/last-rentcalc-file.txt

if [ -f $LAST_RENTCALC_FILE ]; then
    STORED_MD_FULLPATH=`cat $LAST_RENTCALC_FILE`

    echo "Exporting PDF file from md $STORED_MD_FULLPATH"

    FILENAME=$(basename -- "$STORED_MD_FULLPATH")
    FILENAME="${FILENAME%.*}"

    pandoc -o /Volumes/home/Documents/wynajem/pdf/${FILENAME}.pdf $STORED_MD_FULLPATH

    rm $STORED_MD_FULLPATH
    rm $LAST_RENTCALC_FILE
fi
