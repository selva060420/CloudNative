#Extract data from installed helm chart's secret
function read {
  SECRET=$(kubectl get secret $1 -n $NAMESPACE -o jsonpath='{ .data.release }' ) 
  # decode the secrets
  DECODED_SECRET=$(echo $SECRET | base64 -d | base64 -d | gunzip -c )
  # parse the decoded secrets, pulling out the templates and removing whitespace
  DATA=$(echo $DECODED_SECRET | jq '.chart.templates[]' | tr -d '[:space:]' )
  # assign each entry in templates to an array
  ARRAY=($(echo $DATA | tr '} {' '\n'))
 
  # loop through each entry in the array
  for i in "${ARRAY[@]}"
  do
	# splitting name and data into separate items in another array
	ITEMS=($(echo $i | tr ',' '\n'))

	# parsing the name field
	echo "${ITEMS[0]}" | sed -e 's/name/""/g; s/templates/""/g' | tr -d '/:"'

	# decoding and parsing the data field
	echo "${ITEMS[1]}" | sed -e 's/data/""/g' | tr -d '":' | base64 -d

	# adding a blank line at the end
	echo ''
  done  
}