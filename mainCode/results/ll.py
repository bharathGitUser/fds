#!/usr/bin/python

import os
import sys

import stats
import sys

import numpy as np
import matplotlib.pyplot as plt
from operator import add 
def foo(section, dict):
	dict[section] = {}
	dict[section]['xval'] = []
	dict[section]['yval'] = []

def boo(d, dict):
	for (key,val) in d.items():
		dict[key]['yval'].append(map(lambda x : x[1], val))

if __name__ == "__main__":
	assert len(sys.argv)>1

	file2Parse = str(sys.argv[1])

	handleFile2Parse = open(file2Parse,'r')

	data = {}
	trialNumber = -1
	opNumber = 10
	for line in handleFile2Parse:
		if line.startswith('Trial'):
			trialNumber = trialNumber+1
			data[trialNumber] = {}
		elif line.startswith('-'):
			section = line[:-1].strip('-')
			data[trialNumber][section] = []
		else:
			tmp = line.split(',')
			list = []
			list.append(float(tmp[1]))
			list.append(float(tmp[2]))
			list.append(float(tmp[3]))
			list.append(float(tmp[0]))
			list.append(float(tmp[4]))
			list.append(float(tmp[5]))
			list.append(float(tmp[6]))
			list.append(float(tmp[7]))
			list.append(float(tmp[8]))
			data[trialNumber][section].append(list)

			#data[trialNumber][section].append(map(float,line.split(',')[1:3]))
	handleFile2Parse.close()

	print trialNumber
	#Linked List Fusion Updates
	operations = map(lambda x : float(x[0]), data[0]['LL_FUSION_UPDATE'])
	# print operations
	nodes = map(lambda x : float(x[1]), data[0]['LL_FUSION_UPDATE'])
	# print nodes
	ll_fusion_update_time = map(lambda x : float(x[2]), data[0]['LL_FUSION_UPDATE'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[2]), data[i]['LL_FUSION_UPDATE'])
		ll_fusion_update_time = map(add,ll_fusion_update_time,tmp) 		
	
	for j in range (0,opNumber):
		ll_fusion_update_time[j] = ll_fusion_update_time[j]/(trialNumber+1)
	
	#Linked List Next Updates
	ll_next_update_time = map(lambda x : float(x[2]), data[0]['LL_FUSION_NEXT_UPDATE'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[2]), data[i]['LL_FUSION_NEXT_UPDATE'])
		ll_next_update_time = map(add,ll_next_update_time,tmp) 		
	
	for j in range (0,opNumber):
		ll_next_update_time[j] = ll_next_update_time[j]/(trialNumber+1)
	
	#Linked List Rep Updates
	ll_rep_update_time = map(lambda x : float(x[2]), data[0]['LL_REP_UPDATE'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[2]), data[i]['LL_REP_UPDATE'])
		ll_rep_update_time = map(add,ll_rep_update_time,tmp) 		
	
	for j in range (0,opNumber):
		ll_rep_update_time[j] = ll_rep_update_time[j]/(trialNumber+1)
	else:  print ll_rep_update_time 
		
	print "****Updates*******"
	print ll_fusion_update_time
	print ll_next_update_time
	print ll_rep_update_time
	plt.xlabel('Number of Updates')
	plt.ylabel('Time Taken per update in micros')
	plt.plot(operations,ll_fusion_update_time,'k:',operations,ll_next_update_time,'k-',operations,ll_rep_update_time,'k--')
	plt.legend(('Fusion','Fusion-Next','Replication'),2);
	plt.savefig('.'.join(["LL_UPDATE","ps"]))
	plt.close();
	
	ll_rep_rec = map(lambda x : float(x[4]), data[0]['LL_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[4]), data[i]['LL_REP_REST'])
		ll_rep_rec= map(add,ll_rep_rec,tmp) 		
	
	for j in range (0,opNumber):
		ll_rep_rec[j] = ll_rep_rec[j]/(trialNumber+1)
	
	ll_rep_rrcrashtime = map(lambda x : float(x[8]), data[0]['LL_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_REP_REST'])
		ll_rep_rrcrashtime= map(add,ll_rep_rrcrashtime,tmp) 		
	
	for j in range (0,opNumber):
		ll_rep_rrcrashtime[j] = ll_rep_rrcrashtime[j]/(trialNumber+1)
		ll_rep_rrcrashtime[j] = ll_rep_rrcrashtime[j]/10
	
	time_for_origs = ll_rep_rrcrashtime[0]*10 

	ll_rep_rec= map(add,ll_rep_rec,ll_rep_rrcrashtime) 		
	for j in range (0,opNumber):
		ll_rep_rec[j] = ll_rep_rec[j]/1000000


	#Linked List Fusion Recovery 
	faults = map(lambda x : float(x[3]), data[0]['LL_FUSION_REST'])
	# print faults 
	ll_fusion_rec = map(lambda x : float(x[4]), data[0]['LL_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[4]), data[i]['LL_FUSION_REST'])
		ll_fusion_rec= map(add,ll_fusion_rec,tmp) 		
	
	for j in range (0,opNumber):
		ll_fusion_rec[j] = ll_fusion_rec[j]/(trialNumber+1)

	ll_fusion_rrcrashtime = map(lambda x : float(x[8]), data[0]['LL_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_FUSION_REST'])
		ll_fusion_rrcrashtime= map(add,ll_fusion_rrcrashtime,tmp) 		
	
	for j in range (0,opNumber):
		ll_fusion_rrcrashtime[j] = ll_fusion_rrcrashtime[j]/(trialNumber+1)
		ll_fusion_rrcrashtime[j] = ll_fusion_rrcrashtime[j] + time_for_origs

	ll_fusion_rec= map(add,ll_fusion_rec,ll_fusion_rrcrashtime) 		

	for j in range (0,opNumber):
		ll_fusion_rec[j] = ll_fusion_rec[j]/1000000

	#Linked List Next Recovery 
	ll_next_rec = map(lambda x : float(x[4]), data[0]['LL_FUSION_NEXT_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[4]), data[i]['LL_FUSION_NEXT_REST'])
		ll_next_rec= map(add,ll_next_rec,tmp) 		
	
	for j in range (0,opNumber):
		ll_next_rec[j] = ll_next_rec[j]/(trialNumber+1)
	
	ll_next_rrcrashtime = map(lambda x : float(x[8]), data[0]['LL_FUSION_NEXT_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_FUSION_NEXT_REST'])
		ll_next_rrcrashtime= map(add,ll_next_rrcrashtime,tmp) 		
	
	for j in range (0,opNumber):
		ll_next_rrcrashtime[j] = ll_next_rrcrashtime[j]/(trialNumber+1)
		ll_next_rrcrashtime[j] = ll_next_rrcrashtime[j] + time_for_origs

	ll_next_rec= map(add,ll_next_rec,ll_next_rrcrashtime) 		

	for j in range (0,opNumber):
		ll_next_rec[j] = ll_next_rec[j]/1000000

	print "****Recovery****"
	print ll_fusion_rec
	print ll_next_rec
	print ll_rep_rec
	plt.xlabel('Number of Faults')
	plt.ylabel('Time Taken for recovery in seconds')
	plt.plot(faults,ll_fusion_rec,'k:',faults,ll_next_rec,'k-',faults,ll_rep_rec,'k--')
	plt.legend(('Fusion','Fusion-Next','Replication'),2);
	plt.savefig('.'.join(["LL_REC","ps"]))
	plt.close()

	# Byz
	ll_fusion_byz = map(lambda x : float(x[5]), data[0]['LL_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[5]), data[i]['LL_FUSION_REST'])
		ll_fusion_byz= map(add,ll_fusion_byz,tmp) 		
	
	for j in range (0,opNumber):
		ll_fusion_byz[j] = ll_fusion_byz[j]/(trialNumber+1)
	
	ll_fusion_rrbyztime = map(lambda x : float(x[8]), data[0]['LL_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_FUSION_REST'])
		ll_fusion_rrbyztime= map(add,ll_fusion_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		ll_fusion_rrbyztime[j] = ll_fusion_rrbyztime[j]/(trialNumber+1)
		ll_fusion_rrbyztime[j] = ll_fusion_rrbyztime[j]+ time_for_origs

	ll_fusion_byz= map(add,ll_fusion_byz,ll_fusion_rrbyztime) 		

	for j in range (0,opNumber):
		ll_fusion_byz[j] = ll_fusion_byz[j]/1000000

	#Linked List Next Byz 
	ll_next_byz = map(lambda x : float(x[5]), data[0]['LL_FUSION_NEXT_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[5]), data[i]['LL_FUSION_NEXT_REST'])
		ll_next_byz= map(add,ll_next_byz,tmp) 		
	
	for j in range (0,opNumber):
		ll_next_byz[j] = ll_next_byz[j]/(trialNumber+1)
	
	ll_next_rrbyztime = map(lambda x : float(x[8]), data[0]['LL_FUSION_NEXT_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_FUSION_NEXT_REST'])
		ll_next_rrbyztime= map(add,ll_next_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		ll_next_rrbyztime[j] = ll_next_rrbyztime[j]/(trialNumber+1)
		ll_next_rrbyztime[j] = ll_next_rrbyztime[j]+ time_for_origs

	ll_next_byz= map(add,ll_next_byz,ll_next_rrbyztime) 		
	for j in range (0,opNumber):
		ll_next_byz[j] = ll_next_byz[j]/1000000

	#Linked List Rep Byz
	ll_rep_byz = map(lambda x : float(x[5]), data[0]['LL_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[5]), data[i]['LL_REP_REST'])
		ll_rep_byz= map(add,ll_rep_byz,tmp) 		
	
	for j in range (0,opNumber):
		ll_rep_byz[j] = ll_rep_byz[j]/(trialNumber+1)
	
	ll_rep_rrbyztime = map(lambda x : float(x[8]), data[0]['LL_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_REP_REST'])
		ll_rep_rrbyztime= map(add,ll_rep_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		ll_rep_rrbyztime[j] = ll_rep_rrbyztime[j]/(trialNumber+1)
		ll_rep_rrbyztime[j] = ll_rep_rrbyztime[j]+time_for_origs
	
	ll_rep_byz= map(add,ll_rep_byz,ll_rep_rrbyztime) 		

	for j in range (0,opNumber):
		ll_rep_byz[j] = ll_rep_byz[j]/1000000

	print "***Byz****"
	print ll_fusion_byz
	print ll_next_byz
	print ll_rep_byz	
	plt.xlabel('Number of Faults')
	plt.ylabel('Time Taken for Byzantine Sanity Check in seconds')
	plt.plot(faults,ll_fusion_byz,'k:',faults,ll_next_byz,'k-',faults,ll_rep_byz,'k--')
	plt.legend(('Fusion','Fusion-Next','Replication'),2);
	plt.savefig('.'.join(["LL_BYZ","ps"]))
	plt.close()

	# print "size"
	ll_fusion_size = map(lambda x : float(x[6]), data[0]['LL_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[6]), data[i]['LL_FUSION_REST'])
		ll_fusion_size= map(add,ll_fusion_size,tmp) 		
	
	for j in range (0,opNumber):
		ll_fusion_size[j] = ll_fusion_size[j]/(trialNumber+1)
	
	#Linked List Next Byz 
	ll_next_size = map(lambda x : float(x[6]), data[0]['LL_FUSION_NEXT_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[6]), data[i]['LL_FUSION_NEXT_REST'])
		ll_next_size= map(add,ll_next_size,tmp) 		
	
	for j in range (0,opNumber):
		ll_next_size[j] = ll_next_size[j]/(trialNumber+1)
	
	#Linked List Rep Byz
	ll_rep_size = map(lambda x : float(x[6]), data[0]['LL_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[6]), data[i]['LL_REP_REST'])
		ll_rep_size= map(add,ll_rep_size,tmp) 		
	
	for j in range (0,opNumber):
		ll_rep_size[j] = ll_rep_size[j]/(trialNumber+1)
		
	print "****size****"
	print ll_fusion_size
	print ll_next_size
	print ll_rep_size
	plt.xlabel('Number of Faults')
	plt.ylabel('Total number of nodes in the backup structures')
	plt.plot(faults,ll_fusion_size,'k:',faults,ll_next_size,'k-',faults,ll_rep_size,'k--')
	plt.legend(('Fusion','Fusion-Next','Replication'),2);
	plt.savefig('.'.join(["LL_SIZE","ps"]))
	plt.close()

	# Random Byz
	ll_fusion_randbyz = map(lambda x : float(x[7]), data[0]['LL_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[7]), data[i]['LL_FUSION_REST'])
		ll_fusion_randbyz= map(add,ll_fusion_randbyz,tmp) 		
	
	for j in range (0,opNumber):
		ll_fusion_randbyz[j] = ll_fusion_randbyz[j]/(trialNumber+1)
	else:  print ll_fusion_randbyz
	
	ll_fusion_rrbyztime = map(lambda x : float(x[8]), data[0]['LL_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_FUSION_REST'])
		ll_fusion_rrbyztime= map(add,ll_fusion_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		ll_fusion_rrbyztime[j] = ll_fusion_rrbyztime[j]/(trialNumber+1)
		ll_fusion_rrbyztime[j] = ll_fusion_rrbyztime[j] + time_for_origs
	else:  print ll_fusion_rrbyztime

	#ll_fusion_randbyz= map(add,ll_fusion_randbyz,ll_fusion_rrbyztime) 		

	for j in range (0,opNumber):
		ll_fusion_randbyz[j] = ll_fusion_randbyz[j]/1000000
	else:  print ll_fusion_randbyz


	#Linked List Next Byz 
	ll_next_randbyz = map(lambda x : float(x[7]), data[0]['LL_FUSION_NEXT_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[7]), data[i]['LL_FUSION_NEXT_REST'])
		ll_next_randbyz= map(add,ll_next_randbyz,tmp) 		
	
	for j in range (0,opNumber):
		ll_next_randbyz[j] = ll_next_randbyz[j]/(trialNumber+1)
	else:  print ll_next_randbyz
	
	ll_next_rrbyztime = map(lambda x : float(x[8]), data[0]['LL_FUSION_NEXT_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_FUSION_NEXT_REST'])
		ll_next_rrbyztime= map(add,ll_next_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		ll_next_rrbyztime[j] = ll_next_rrbyztime[j]/(trialNumber+1)
		ll_next_rrbyztime[j] = ll_next_rrbyztime[j] + time_for_origs
	else:  print ll_next_rrbyztime

	#ll_next_randbyz= map(add,ll_next_randbyz,ll_next_rrbyztime) 		
	for j in range (0,opNumber):
		ll_next_randbyz[j] = ll_next_randbyz[j]/1000000
	else:  print ll_next_randbyz

	#Linked List Rep Byz
	ll_rep_randbyz = map(lambda x : float(x[7]), data[0]['LL_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[7]), data[i]['LL_REP_REST'])
		ll_rep_randbyz= map(add,ll_rep_randbyz,tmp) 		
	
	for j in range (0,opNumber):
		ll_rep_randbyz[j] = ll_rep_randbyz[j]/(trialNumber+1)
	else:  print ll_rep_randbyz
	
	ll_rep_rrbyztime = map(lambda x : float(x[8]), data[0]['LL_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['LL_REP_REST'])
		ll_rep_rrbyztime= map(add,ll_rep_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		ll_rep_rrbyztime[j] = ll_rep_rrbyztime[j]/(trialNumber+1)
		ll_rep_rrbyztime[j] = ll_rep_rrbyztime[j]+ time_for_origs
	else:  print ll_rep_rrbyztime
	
	#ll_rep_randbyz= map(add,ll_rep_randbyz,ll_rep_rrbyztime) 		

	for j in range (0,opNumber):
		ll_rep_randbyz[j] = ll_rep_randbyz[j]/1000000
	else:  print ll_rep_randbyz


	plt.xlabel('Number of Faults')
	plt.ylabel('Time Taken for Byzantine Sanity Check in seconds')
	plt.plot(faults,ll_fusion_randbyz,'k:',faults,ll_next_randbyz,'k-',faults,ll_rep_randbyz,'k--')
	plt.legend(('Fusion','Fusion-Next','Replication'),2);
	plt.savefig('.'.join(["LL_RAND_BYZ","ps"]))
	plt.close()

