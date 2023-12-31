#!/bin/bash

kubectl create ns healthai

cd springboot-backend
./build_and_push.sh
./create_secret_from_wallet.sh
./deploy.sh

cd ../flutter-frontend
./build_and_push.sh
./deploy.sh

cd ../

