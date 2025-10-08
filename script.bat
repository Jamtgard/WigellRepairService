@echo off
set IMAGE=wigell-repair
set CONTAINER=wigell-repair
set NETWORK=wigell-network
set PORT=5555
set ENVFILE=prod.env

echo Stopping %CONTAINER%
docker stop %CONTAINER%
echo Deleting container %CONTAINER%
docker rm %CONTAINER%
echo Deleting image %IMAGE%
docker rmi %IMAGE%
echo Running mvn package
call mvn package
echo Creating image %IMAGE%
docker build -t %IMAGE% .
echo Creating and running container %CONTAINER%
docker run -d -p %PORT%:%PORT% --name %CONTAINER% --network %NETWORK% --env-file %ENVFILE% %IMAGE%
echo Done!
