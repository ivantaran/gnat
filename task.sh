#/bin/bash

NAME=TLSE00FRA_R_2019
DIR=./rnx
START=1
STOP=366
JAR=gnat-1.0-SNAPSHOT.jar
CONFIG='--minsnr=30.0 --minelv=5.0'
FILTER=--filter=${NAME}
ODIR=./output

mkdir -p ${ODIR}

for I in $(seq -f "%03g" ${START} ${STOP}); do
    echo "${JAR} ${CONFIG} ${FILTER}${I} --output=${NAME}${I}.txt ${DIR}"
    java -jar ${JAR} ${CONFIG} ${FILTER}${I} --output=${ODIR}/${NAME}${I}.txt ${DIR}
done

cat ./output/*.txt > output.txt
