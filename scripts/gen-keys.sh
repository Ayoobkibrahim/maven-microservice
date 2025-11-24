#!/usr/bin/env bash
set -e

echo "Generating RSA keys…"
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -in private.pem -pubout -out public.pem

mv private.pem ../auth-service/private.pem
mv public.pem ../auth-service/public.pem

echo "Done"
