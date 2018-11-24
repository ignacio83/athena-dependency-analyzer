#!/bin/bash

# Grupo Netshoes

# startup parameters
export MONGO_URI=$1			    # URI to MongoDB. ex: mongodb://localhost:27017/athena
export RABBITMQ_ADDRESSES=$2	# Addresses to RabbitMQ ex: localhost:5672
export RABBITMQ_HOST=$3     # Rabbitmq vhost
export RABBITMQ_USER=$4     #User for rabbitmq
export RABBITMQ_PASS=$5     #Pass for rabbitmq
export GITHUB_TOKEN=$6	        # GitHub Token
export GITHUB_HOST=$7       #User for rabbitmq
export GITHUB_ORGANIZATION=$8       #Pass for rabbitmq
export ADMIN_USERNAME=$9	    # Username for admin
export ADMIN_PASSWORD=${10}	    # Password for admin

echo "=============================="
echo "      startup parameters"
echo "=============================="
echo $1
echo $2
echo $3
echo $4
echo $5
echo $6
echo $7
echo $8
echo $9
echo ${10}

echo "=============================="


exec $(type -p java) \
  -cp /opt/app:opt/app/lib/* \
  -Dspring.data.mongodb.uri=${MONGO_URI} \
  -Dspring.rabbitmq.addresses=${RABBITMQ_ADDRESSES} \
  -Dspring.rabbitmq.virtual-host=${RABBITMQ_HOST} \
  -Dspring.rabbitmq.username=${RABBITMQ_USER} \
  -Dspring.rabbitmq.password=${RABBITMQ_PASS} \
  -Dapplication.github.token=${GITHUB_TOKEN} \
  -Dapplication.github.host=${GITHUB_HOST} \
  -Dapplication.github.organization=${GITHUB_ORGANIZATION} \
  -Dapplication.security.admin.username=${ADMIN_USERNAME} \
  -Dapplication.security.admin.password=${ADMIN_PASSWORD} \
  com.netshoes.athena.Application

