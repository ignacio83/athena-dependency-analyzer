[![Build Status](https://travis-ci.org/netshoes/athena-dependency-analyzer.svg?branch=master)](https://travis-ci.org/netshoes/athena-dependency-analyzer)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/38c2df0304454308aa927ab5353e612d)](https://app.codacy.com/app/ignacio83/athena-dependency-analyzer?utm_source=github.com&utm_medium=referral&utm_content=netshoes/athena-dependency-analyzer&utm_campaign=Badge_Grade_Dashboard)
[![Coverage Status](https://coveralls.io/repos/github/netshoes/athena-dependency-analyzer/badge.svg?branch=master)](https://coveralls.io/github/netshoes/athena-dependency-analyzer?branch=master)
[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Athena Dependency Analyzer

## Developing
The java application is available at app.

Inside of java app the code is available at `src/main/java` and the frontend files are in 
`src/main/frontend`.

### Running dependencies

    docker run -d --name athena_rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.7.7-management-alpine
    docker run -d --name athena_mongo -p 27017:27017 mongo:3.6.9 --smallfiles

### Running the backend
Run `Application` class from your IDE informing the following spring boot parameters: 
- `application.github.organization`: Name of organization in Github.
- `application.github.token`: Token for access the Github API.

### Running the frontend
Go to `src/main/frontend` and run `npm start`. (Run `npm install` before that if it's the first time)

Now we should work with `http://localhost:9090` (this is where we'll see our live changes reflected)
 instead of `http://localhost:8080`.
 
### Run everything from Maven

    mvn generate-resources spring-boot:run -Drun.arguments="--application.github.organization=my_organization --application.github.token=my_token"

The Maven goal `generate-resources` will execute the frontend-maven-plugin to install Node
and Npm the first time, run npm install to download all the libraries that are not 
present already and tell webpack to generate our `bundle.js`. It's the equivalent of running `npm run build` or `npm start` on a terminal.

### Packaging

    mvn package

## Docker

### Running
    export GITHUB_HOST=api.github.com
    export GITHUB_ORGANIZATION=my_organization
    export GITHUB_TOKEN=my_token
    export ADMIN_USERNAME=admin
    export ADMIN_PASSWORD={noop}admin
    
    docker-compose up 

### Building a new docker image

    docker build -t netshoes/athena-dependency-analyzer .

### Building a service on docker swarm

    docker service create \
                        --name ${PROJECT} \ ## Name off de service on Swarm
                        --publish xxxx:8080 \   ## Port bind
                        --replicas ${appEnv.maxInstances} \ ## Number off containers
                        --env MONGO_URI="mongodb://pass:user@host_mongo.yourdomain:27017/database" \    ## URI off mongodb
                        --env RABBITMQ_ADDRESSES="host_rabbitmq.yourdomain:5672" \ ## Rabbitmq host and port
                        --env RABBITMQ_HOST="vhost" \   ## Vhost
                        --env RABBITMQ_USER="user" \    ## Rabbitmq User
                        --env RABBITMQ_PASS="pass" \   ## Rabbitmq pass
                        --env GITHUB_HOST="api.github.com" \
                        --env GITHUB_ORGANIZATION="Your_OrganizationGit" \
                        --env GITHUB_TOKEN="Token_yourOrganizationGit" \
                        --env ADMIN_USERNAME=user \
                        --env ADMIN_PASSWORD={noop}pass \
                        --detach=false \
                        --mount type=bind,source=/usr/share/zoneinfo/America/Sao_Paulo,destination=/etc/timezone,readonly \ ## Config your timezone
                        netshoes/athena-dependency-analyzer:latest