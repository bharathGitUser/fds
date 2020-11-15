#Script for running tests...
numStructs=$1
for(( retries =1; retries <= 5; retries++))
do
echo "Trial = $retries"
numFaults=1
#for the update time tests we maintain the same number of faults and change the number of operations

echo "LL_FUSION_UPDATE"
for ((  mulFactor = 1 ;  mulFactor <= 10;  mulFactor++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i < $numFaults;  i++  ))
do
java FusedLinkedList $i $numStructs $numFaults &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalLinkedList $i &
done

sleep 10
java FusionLinkedListOracle  $numStructs $numFaults $mulFactor
done

echo "LL_FUSION_NEXT_UPDATE"
for ((  mulFactor = 1 ;  mulFactor <= 10;  mulFactor++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i < $numFaults;  i++  ))
do
java FusedLinkedListWithNext $i $numStructs $numFaults &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalLinkedList $i &
done

sleep 10
java FusionLinkedListWithNextOracle  $numStructs $numFaults $mulFactor
done

echo "LL_REP_UPDATE"
for ((  mulFactor = 1 ;  mulFactor <= 10;  mulFactor++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i <= ($numFaults \* $numStructs);  i++  ))
do
java ReplicatedLinkedList $i &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalLinkedList $i &
done

sleep 60
java ReplicationLinkedListOracle  $numStructs $numFaults $mulFactor 
done

echo "Q_FUSION_UPDATE"
for ((  mulFactor = 1 ;  mulFactor <= 10;  mulFactor++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i < $numFaults;  i++  ))
do
java FusedQueue $i $numStructs $numFaults &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalQueue $i &
done

sleep 10
java FusionQueueOracle  $numStructs $numFaults $mulFactor
done

echo "Q_REP_UPDATE"
for ((  mulFactor = 1 ;  mulFactor <= 10;  mulFactor++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i <= ($numFaults \* $numStructs);  i++  ))
do
java ReplicatedQueue $i &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalQueue $i &
done

sleep 60
java ReplicationQueueOracle  $numStructs $numFaults $mulFactor
done


echo "LL_FUSION_REST"
for ((  numFaults = 1 ;  numFaults <= $numStructs;  numFaults++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i < $numFaults;  i++  ))
do
java FusedLinkedList $i $numStructs $numFaults &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalLinkedList $i &
done

sleep 10
java FusionLinkedListOracle  $numStructs $numFaults 10
done


echo "LL_FUSION_NEXT_REST"
for ((  numFaults = 1 ;  numFaults <= $numStructs;  numFaults++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i < $numFaults;  i++  ))
do
java FusedLinkedListWithNext $i $numStructs $numFaults &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalLinkedList $i &
done

sleep 10
java FusionLinkedListWithNextOracle  $numStructs $numFaults 10
done


echo "LL_REP_REST"
for ((  numFaults = 1 ;  numFaults <= $numStructs;  numFaults++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i <= ($numFaults \* $numStructs);  i++  ))
do
java ReplicatedLinkedList $i &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalLinkedList $i &
done

sleep 60
java ReplicationLinkedListOracle  $numStructs $numFaults 10
done

echo "Q_FUSION_REST"
for ((  numFaults = 1 ;  numFaults <= $numStructs;  numFaults++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i < $numFaults;  i++  ))
do
java FusedQueue $i $numStructs $numFaults &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalQueue $i &
done

sleep 10
java FusionQueueOracle  $numStructs $numFaults 10
done

echo "Q_REP_REST"
for ((  numFaults = 1 ;  numFaults <= $numStructs;  numFaults++  ))
do
pkill -9 java
#echo "---------------------"
#echo "Faults = $numFaults"

for ((  i = 0 ;  i <= ($numFaults \* $numStructs);  i++  ))
do
java ReplicatedQueue $i &
done

for ((  i = 0 ;  i < $numStructs;  i++  ))
do
java OriginalQueue $i &
done

sleep 60
java ReplicationQueueOracle  $numStructs $numFaults 10
done
$numFaults=5
done
