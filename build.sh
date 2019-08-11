#!/bin/bash

#./gradlew installDebug

cd ./download_skin/
path=$1
files=$(ls $path)
for filename in $files
do
    echo ${filename}
    if [ -d ${filename} ];then
        rm ${filename}.zip
        zip -r ${filename}.zip ${filename}
        adb push ${filename}.zip /sdcard/Android/data/com.huison.easytheme/files/skin
    fi
done