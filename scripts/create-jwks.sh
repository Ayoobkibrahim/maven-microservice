#!/usr/bin/env bash

set -e

cd ../auth-service

PUBKEY="public.pem"
KID="auth-rs256-key-1"

# Convert SPKI public key → PKCS1 RSA public key → DER
openssl rsa -pubin -inform PEM -in $PUBKEY -RSAPublicKey_out -out public_rsa.pem 2>/dev/null
openssl rsa -pubin -inform PEM -in public_rsa.pem -outform DER -out public_rsa.der 2>/dev/null

# Extract modulus from DER key
MODULUS=$(openssl rsa -pubin -inform DER -in public_rsa.der -text -noout 2>/dev/null \
          | awk '/Modulus/{flag=1;next}/Exponent/{flag=0}flag' \
          | tr -d ' \n:' \
          | xxd -r -p \
          | openssl base64 -A \
          | tr '+/' '-_' \
          | tr -d '=')

EXPONENT="AQAB"

cat <<EOF > ../auth-service/jwks.json
{
  "keys": [
    {
      "kty": "RSA",
      "alg": "RS256",
      "use": "sig",
      "kid": "$KID",
      "n": "$MODULUS",
      "e": "$EXPONENT"
    }
  ]
}
EOF

rm -f public_rsa.pem public_rsa.der

echo "JWKS successfully generated inside auth-service/jwks.json"
