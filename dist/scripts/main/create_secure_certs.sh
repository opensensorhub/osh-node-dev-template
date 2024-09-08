#### CUT HERE 



TESTCA="./testRootCA.p12"
TESTCACERT="./config/trusted_certs/testRootCACert.pem"
TESTCAALIAS="test root ca"
TESTCADN="CN=Test Root CA"
KEYSTORE="osh-keystore.p12"
KEY_ALIAS="jetty"
export KEYSTORE_PASS="atakatak"
KSPASS_PARAM1="-storepass:env"
KSPASS_PARAM2="KEYSTORE_PASS"
HOSTNAME="ogc-demo"
# Uncomment IP when address is known.
 IP="34.67.197.57"


if [ ! -f "$TESTCA" ]; then
	echo "Generating testing CA at $TESTCA"
	keytool -genkeypair -v -keyalg RSA -keysize 4096 -dname "$TESTCADN" -alias "$TESTCAALIAS" \
		-validity 3650 -keystore "$TESTCA" -storetype PKCS12 "$KSPASS_PARAM1" "$KSPASS_PARAM2" \
		-ext BC:critical=ca:true -ext KU:critical=digitalSignature,keyCertSign,cRLSign

	echo "Exporting testing CA certificate to $TESTCACERT"
	keytool -exportcert -rfc -alias "$TESTCAALIAS" -file "$TESTCACERT" \
   		-keystore "$TESTCA" "$KSPASS_PARAM1" "$KSPASS_PARAM2"
fi


echo "Deleting previous cert in osh-keystore"
keytool -delete -alias "$KEY_ALIAS" -keystore "$KEYSTORE" -storetype PKCS12 "$KSPASS_PARAM1" "$KSPASS_PARAM2"
keytool -delete -alias "$TESTCAALIAS" -keystore "$KEYSTORE" -storetype PKCS12 "$KSPASS_PARAM1" "$KSPASS_PARAM2"

echo "Generating initial self-signed certificate in osh-keystore"
keytool -genkeypair -v -keyalg RSA -keysize 4096 -validity 1825 -alias "$KEY_ALIAS" -dname "CN=$HOSTNAME" \
    -keystore "$KEYSTORE" -storetype PKCS12 "$KSPASS_PARAM1" "$KSPASS_PARAM2"

echo "Importing CA cert to keystore"
keytool -importcert -keystore "$KEYSTORE" -alias "$TESTCAALIAS" -file "$TESTCACERT" \
    -noprompt -storepass "atakatak"

echo "Signing with test CA"
if [ -z "$IP" ]; then
	SAN="SAN=DNS:$HOSTNAME"
else
    SAN="SAN=DNS:$HOSTNAME,IP:$IP"
fi

keytool -certreq -alias "$KEY_ALIAS" -keystore "$KEYSTORE" "$KSPASS_PARAM1" "$KSPASS_PARAM2" | \
keytool -gencert -rfc -ext "$SAN" -alias "$TESTCAALIAS" -validity 1825 -keystore "$TESTCA" "$KSPASS_PARAM1" "$KSPASS_PARAM2" | \
keytool -importcert -alias "$KEY_ALIAS" -keystore "$KEYSTORE" "$KSPASS_PARAM1" "$KSPASS_PARAM2"

echo ""