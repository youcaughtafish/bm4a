#!/bin/bash
keytool \
	-import \
	-v \
	-trustcacerts \
	-alias $1 \
	-file <(openssl x509 -in $2) \
	-keystore $3 \
	-storetype BKS \
	-provider org.bouncycastle.jce.provider.BouncyCastleProvider \
	-providerpath $4 \
	-storepass $5 
