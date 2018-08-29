#!/bin/bash
#Author: Moses Ike. http://mosesike.org
#This script needs 2 argument. path to config file, and netid

PROG=Project2

#command line arguments
CONFIG=$1
netid=$2

#clear a custom debug file b4 each run/test
echo -e "" > debug.txt

#Something the TA is making us do
config_file_name=$(echo $CONFIG | rev | cut -f1 -d"/" | rev | cut -f1 -d".") #without extension

# extract the important lines from the config file. the ones with no '#' or empty lines
sed -e "s/#.*//" $CONFIG | sed -e "/^\s*$/d" > temp
# insert a new line to EOF # necessary for the while loop
echo  >> temp

node_count=0
nodes_location="" #Stores a # delimited string of Location of each node
host_names=() #Stores the hostname of each node
neighbors_dict=() # Stores the Token path of each node

current_line=1
# Reading from the temp file created above
while read line; 
do
	#turn all spaces to single line spaces
	line=$(echo $line | tr -s ' ')
########Extract Number of nodes and, min and max per Active
	if [ $current_line -eq 1 ]; then
		#number of nodes
		node_count=$(echo $line | cut -f1 -d" ")
		#convert it to an integer
  		let node_count=$node_count+0 
  		
  		#root node
  		root_node=$(echo $line | cut -f2 -d" ")
  		
  	else
#########Extract Location of each node
  		if [ $current_line -le $(expr $node_count + 1) ]; then
  			nodes_location+=$( echo -e $line"#" )	
  			node_id=$(echo $line | cut -f1 -d" ")
  			hostname=$(echo $line | cut -f2 -d" ")
  			host_names[$node_id]="$hostname"	
  		else
###########Extract Neighbors
			
			let node_id=$current_line-$node_count-2
  			neighbors=$(echo $line)
  			neighbors_dict+=(['"$node_id"']="$neighbors")
  		fi
  	fi
  	let current_line+=1
done < temp
delay=30
# iterate through the date collected above and execute on the remote servers
for node_id in $(seq 0 $(expr $node_count - 1))
do
	host=${host_names[$node_id]}
	neighbors=${neighbors_dict["$node_id"]}
	#echo $netid@$host "java $PROG $node_id '$nodes_location' '$neighbors' '$root_node' '$config_file_name' '$(pwd)'" &
	#ssh -o StrictHostKeyChecking=no $netid@$host "cd $(pwd); java $PROG $node_id '$nodes_location' '$neighbors' '$node_count' '$root_node' '$config_file_name' " &
	ssh -o StrictHostKeyChecking=no $netid@$host "cd $(pwd); java -jar AOS_1.jar  $node_id '$nodes_location' '$neighbors' '$root_node' '$config_file_name' '$(pwd)' '$delay'" &
	
done
