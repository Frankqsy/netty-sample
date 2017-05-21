#!/bin/bash

function isServerExist() {
   java -classpath netty-client.jar org.daisy.stevin.netty.sample.client.UdsClient /data/uds/auds.sock
   infos=$?
   for info in $infos
   do
      echo "info:$info"
   done
   echo "infos:$infos"
   return 0
}
isServerExist
isExist=$?
echo "isExist:$isExist"
echo "done"