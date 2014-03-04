#!/bin/sh
cp $1 $1.bak
cat copyright-header-template.txt $1.bak > $1
rm -f $1.bak
