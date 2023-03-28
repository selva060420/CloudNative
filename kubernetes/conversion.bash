#!/bin/sh
# ---------------------------------------------------------------------------------------
# Script to convert pem files to jks and vice versa
# --------------------------------------------------------------------------------------
#
JKS_USAGE="     -k\t\tPath to keystore file
     -t\t\tPath to truststore file
     -p\t\tPass phrase of keystore and truststore"
PEM_USAGE="     -c\t\tPath to a client certificate file
     -p\t\tPath to a client key file
     -a\t\tCA certificate to verify peer against\n"
usage()
{
echo -e "\nUsage: $0 [options...]\n\n     -l\t\tPick any one of the following (jks, pem)\n$JKS_USAGE\n$PEM_USAGE"
exit 1
}
make_or_remove_dir(){
if [ -d $1 ]
then
    rm -rf $1    
fi
mkdir -p $1
chmod -R 777 $1
}
check_if_installed(){
if ! [ -x "$(command -v openssl)" ]; then
	echo 'Error: openssl is not installed'
	exit 1
fi
if ! [ -x "$(command -v keytool)" ]; then
	echo -e 'Error: keytool is not installed'
	exit 1
fi
}
TYPE=""
KEYSTORE_FILE=""
TRUSTSTORE_FILE=""
CERT_FILE=""
KEY_FILE=""
CA_FILE=""
PASSPHRASE=""
JKS_DIR=$PWD/jks/
PEM_DIR=$PWD/pem/
OPT=

while  getopts  l:k:t:p:c:x:a:  OPT
do
    case $OPT in
    l) TYPE=$OPTARG
	    ;;
    k)  KEYSTORE_FILE=$OPTARG
        ;;
    t)  TRUSTSTORE_FILE=$OPTARG
        ;;
	p)  PASSPHRASE=$OPTARG
        ;;
    c)  CERT_FILE=$OPTARG
        ;;
    x)  KEY_FILE=$OPTARG
        ;;
    a)  CA_FILE=$OPTARG
        ;;
    \?) usage
        ;;
    esac
done

shift `expr $OPTIND - 1`
if [ "$TYPE" = "" ]
then
	echo "\n-l argument is missing."
    usage
else
	if [ "$TYPE" = "pem" ]
	then
		if [ "$CERT_FILE" != "" ] || [ "$KEY_FILE" != "" ] || [ "$CA_FILE" != "" ]
		then
			echo -e "\nDon't specify below arguments:\n\n$PEM_USAGE\n"
			exit 1
		fi
		if [ "$KEYSTORE_FILE" == "" ] || [ "$TRUSTSTORE_FILE" == "" ] || [ "$PASSPHRASE" == "" ]
		then
			echo -e "\nSpecify below arguments:\n\n$JKS_USAGE\n"
			exit 1
		fi
		check_if_installed
		make_or_remove_dir $PEM_DIR
		ALIAS_NAME=$(keytool -list -rfc -keystore $KEYSTORE_FILE --storepass $PASSPHRASE |grep -wi "Alias name:"|awk -F: '{print $2}'|tr -d '[:space:]')

		keytool -importkeystore -srckeystore $KEYSTORE_FILE  -destkeystore $PEM_DIR/keystore.p12 -deststoretype PKCS12 -srcstorepass $PASSPHRASE -deststorepass $PASSPHRASE

		openssl pkcs12 -nokeys -in $PEM_DIR/keystore.p12 -out $PEM_DIR/cert.pem
		
		openssl pkcs12 -in $PEM_DIR/keystore.p12 -nocerts -nodes -out $PEM_DIR/key.pem
		
		keytool -importkeystore -srckeystore $TRUSTSTORE_FILE -destkeystore $PEM_DIR/truststore_cert.p12 -deststoretype PKCS12 -srcstorepass $PASSPHRASE -deststorepass $PASSPHRASE

		openssl pkcs12 -in $PEM_DIR/truststore_cert.p12 -nokeys -out $PEM_DIR/cacert.crt

		keytool -exportcert -alias $ALIAS_NAME -keystore $TRUSTSTORE_FILE -rfc -file $PEM_DIR/cacert.pem  --storepass $PASSPHRASE
		
		rm -f $PEM_DIR/*.p12

	elif [ "$TYPE" = "jks" ]
	then
		if [ "$KEYSTORE_FILE" != "" ] || [ "$TRUSTSTORE_FILE" != "" ] || [ "$PASSPHRASE" != "" ]
		then
			echo -e "\nDon't specify below arguments:\n\n$JKS_USAGE\n"
			exit 1
		fi
		if [ "$CERT_FILE" == "" ] || [ "$KEY_FILE" == "" ] || [ "$CA_FILE" == "" ]
		then
			echo -e "\nSpecify below arguments:\n\n$PEM_USAGE\n"
			exit 1
		fi
		check_if_installed
		make_or_remove_dir $JKS_DIR
		openssl pkcs12 -export -in $CERT_FILE -inkey $KEY_FILE -out $JKS_DIR/intermediate.p12 -name clientCert -passin pass:pkcs -passout pass:password

		keytool -importkeystore -srckeystore $JKS_DIR/intermediate.p12 -srcstoretype PKCS12 -srcstorepass password -alias clientCert -deststorepass password -destkeypass password -destkeystore $JKS_DIR/keystore.jks

		keytool -importcert -noprompt -keystore $JKS_DIR/truststore.jks -storepass password -file $CA_FILE -alias serverca
		echo -e "Files are generated. Pass phrase for keystore and truststore: \033[4mpassword\033[0m"
		rm -f $JKS_DIR/*.p12
	else
		echo -e "\n-l argument value only accepts: pem,jks"
		usage
	fi
fi