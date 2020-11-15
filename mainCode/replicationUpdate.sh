#Script for running tests...
numFaults=1
numOperations=50
numPrimaries=3
for(( retries =1; retries <= 3; retries++))
do
	echo "Trial = $retries"
	echo "--LL_REP_UPDATE"
	for ((  opFactor = 1 ;  opFactor <= 10;  opFactor++  ))
	do

		pkill -9 java

		for ((  i = 0 ;  i <= ($numFaults \* $numPrimaries);  i++  ))
		do
		java ReplicatedLinkedList $i &
		sleep 10
		done

		for ((  i = 0 ;  i < $numPrimaries;  i++  ))
		do
		java OriginalLinkedList $i &
		sleep 10
		done

		java ReplicationLinkedListOracle  $numPrimaries $numFaults $[numOperations*opFactor]
	done

	echo "--MAP_REP_UPDATE"
	for ((  opFactor = 1 ;  opFactor <= 10;  opFactor++  ))
	do

		pkill -9 java

		for ((  i = 0 ;  i <= ($numFaults \* $numPrimaries);  i++  ))
		do
		java ReplicatedMap $i &
		sleep 10
		done

		for ((  i = 0 ;  i < $numPrimaries;  i++  ))
		do
		java OriginalMap $i &
		sleep 10
		done

		java ReplicationMapOracle  $numPrimaries $numFaults $[numOperations*opFactor]
	done
done
