#!/bin/bash

while read line           
do       
    echo $line    
    ./baixaTabela.sh $line
done <arquivoOxidoredutases.txt
