# /bin/bash

NAME=TLSG
for i in $(seq -f "%03g" 61 120); do
    wget -nv "ftp://gssc.esa.int/gnss/data/daily/2020/${i}/${NAME}00*_MO.crx.gz"
    wget -nv "ftp://gssc.esa.int/gnss/data/daily/2020/${i}/${NAME}00*_RN.rnx.gz"
done

find . -maxdepth 1 -type f -name '*.gz' -exec gzip -df {} \;
find . -maxdepth 1 -type f -name '*.crx' -exec ./CRX2RNX -d {} \;
