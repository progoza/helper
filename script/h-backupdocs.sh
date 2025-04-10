#/bin/bash
HOME_DIR=/Volumes/home
COMMONS_DIR=/Volumes/wspolne-dane

BACKUP_DIR=Documents

for MOUNT_DIR in $HOME_DIR $COMMONS_DIR; do
    IS_DIR_MOUNTED=`mount | grep $MOUNT_DIR | wc -l`
    if [ $IS_DIR_MOUNTED -eq "0" ]; then
        echo Please mount $MOUNT_DIR samba storage first
        echo mount $MOUNT_DIR
        exit -1
    fi
done 

WORK_DIR=$COMMONS_DIR/workdir/4backup

rm -rf $WORK_DIR
mkdir -p $WORK_DIR

echo "Step 1: Compressing and encrypting documents"

tar zcf -  -C $HOME_DIR $BACKUP_DIR | gpg --encrypt -r pawel.rogoza@gmail.com | split --bytes=100MB - $WORK_DIR/doc-backup.tar.gz.gpg.

ONEDRIVE_DIR=$HOME/OneDrive/Backup
NEXT_BACKUP_DIR=`cat $ONEDRIVE_DIR/next.txt`

echo Calculating md5 sum of the backup

MD5SUM=`cat $WORK_DIR/doc-backup.tar.gz.gpg.* | md5sum`
echo $MD5SUM

echo "Step 2: Copying to OneDrive directory"

NEXT_IS_RED=0
DEST_DIR=$ONEDRIVE_DIR/red

if [[ "$NEXT_BACKUP_DIR" == "blue" ]] ; then
    DEST_DIR=$ONEDRIVE_DIR/blue
    NEXT_IS_RED=1
fi

echo "Saving backup to ${DEST_DIR}.."

mkdir -p $DEST_DIR
rm -f $DEST_DIR/*

cp $WORK_DIR/* $DEST_DIR

echo Calculating md5 sum of the copied backup
MD5SUM2=`cat $DEST_DIR/doc-backup.tar.gz.gpg.* | md5sum`

if [[ "$MD5SUM" == "$MD5SUM2" ]] ; then
   echo "OK: MD5 sum match"
   echo "Backup made successfully on `date`" > $DEST_DIR/backup-info.txt
else
   echo "ERROR: MD5 sum does not match"
fi

if [ "$NEXT_IS_RED" -eq "1" ] ; then
  echo "red" > $ONEDRIVE_DIR/next.txt
else 
  echo "blue" > $ONEDRIVE_DIR/next.txt
fi

echo "Next backup is to be stored in.. `cat $ONEDRIVE_DIR/next.txt`"

rm -r $WORK_DIR
