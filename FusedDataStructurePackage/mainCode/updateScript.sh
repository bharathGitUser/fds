#Script for running tests...
numFaults=1
numOperations=500
numPrimaries=3
for(( retries =1; retries <= 3; retries++))
do
	echo "Trial = $retries"

	echo "--LL_NEW_FUSION_UPDATE"
	for (( opFactor =1 ; opFactor<=10;  ++opFactor  ))

	do
		pkill -9 java
		for ((  i = 0 ;  i < $numFaults;  i++  ))
		do
		java FusedList $i $numPrimaries $numFaults &
		sleep 10
		done

		for ((  i = 0 ;  i < $numPrimaries;  i++  ))
		do
		java FusionPrimaryList $i $numFaults &
		sleep 10
		done
		java FusionListClient  $numPrimaries $numFaults $[numOperations*opFactor]
	done

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
		java StandardPrimaryList $i &
		sleep 10
		done

		java ReplicationListClient $numPrimaries $numFaults $[numOperations*opFactor]
	done

	echo"--LL_OLD_FUSION_UPDATE"
	for (( opFactor =1 ; opFactor<=10;  ++opFactor  ))

	do
		pkill -9 java

		for ((  i = 0 ;  i < $numFaults;  i++  ))
		do
		java OldFusedLinkedList $i $numPrimaries $numFaults &
		sleep 10
		done

		for ((  i = 0 ;  i < $numPrimaries;  i++  ))
		do
		java StandardPrimaryList  $i &
		sleep 10
		done
		java FusionLinkedListOracle  $numPrimaries $numFaults $[numOperations*opFactor]
	done

	echo "--MAP_FUSION_UPDATE"
	for (( opFactor =1 ; opFactor<=10;  ++opFactor  ))

	do
		pkill -9 java
		for ((  i = 0 ;  i < $numFaults;  i++  ))
		do
		java FusedMap $i $numPrimaries $numFaults &
		sleep 10
		done

		for ((  i = 0 ;  i < $numPrimaries;  i++  ))
		do
		java FusionPrimaryMap $i $numFaults &
		sleep 10
		done
		java FusionMapClient  $numPrimaries $numFaults $[numOperations*opFactor]
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
		java StandardPrimaryMap $i &
		sleep 10
		done

		java ReplicationMapClient $numPrimaries $numFaults $[numOperations*opFactor]
	done

done
