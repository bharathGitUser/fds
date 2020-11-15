numFaults=1
numOperations=50
numPrimaries=1
for (( opFactor =1 ; opFactor<=10;  ++opFactor  ))

do
   pkill -9 java

   for ((  i = 0 ;  i < $numFaults;  i++  ))
   do
      java FusedLinkedList $i $numPrimaries $numFaults &
      sleep 10
   done
   
   for ((  i = 0 ;  i < $numPrimaries;  i++  ))
   do
     java OriginalLinkedList $i &
     sleep 10
   done
   java FusionLinkedListOracle  $numPrimaries $numFaults $[numOperations*opFactor]
done


