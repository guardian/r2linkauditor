#! /bin/bash

export http_proxy=""
depth=1
now=$(date +"%F")
linkchecker -r$depth -Fcsv/utf_8/$1-report-$now.csv $1
