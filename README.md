# Cirrina

<div align="center">
    <img src="cirrina.svg" alt="Logo" width="400"/>
</div>

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE.md) [![Build](https://github.com/UIBK-DPS-DC/Cirrina/actions/workflows/build.yml/badge.svg?event=push)](https://github.com/UIBK-DPS-DC/Cirrina/actions/workflows/build.yml?event=push)

Cirrina, a distributed Collaborative State Machines (CSM) runtime for the Cloud-Edge-IoT continuum. Collaborative state machines is a state
machine-based programming model for the Cloud-Edge-IoT continuum inspired by David
Harel's [statecharts](https://www.sciencedirect.com/science/article/pii/0167642387900359).

For the Collaborative State Machines Language specification,
visit [CSML Specifications](https://git.uibk.ac.at/informatik/dps/dps-dc-software/cirrina/-/wikis/csml-specifications).

Collaborative State Machines and the Cirrina runtime are created and developed by the [Distributed and Parallel Systems Group of the
Universität Innsbruck](https://dps.uibk.ac.at/).

## Running

To run a Cirrina runtime without compiling or building a Docker image manually, the
[Cirrina Docker Image](https://hub.docker.com/r/marlonetheredgeuibk/cirrina) can be used as follows:

```bash
docker run \ 
  -e OTEL_EXPORTER_OTLP_ENDPOINT=http://192.168.64.84:4317/ \
  marlonetheredgeuibk/cirrina:develop \
    --nats-context-url nats://192.168.64.84:4222/ \
    --nats-event-url nats://192.168.64.84:4222/ \
    --zookeeper-connect-string 192.168.64.84:2181
```

Please refer to the help text for arguments, `cirrina -h`:

```bash
Usage: cirrina [options]
  Options:
    --delete-job, -d         Flag to delete the job after it is consumed 
                             (default: true)
    --event-handler, -e      Specifies the event handler type to use (default: 
                             Nats) (values: [Nats])
    --health-port, -z        Port number for the HTTP health check service 
                             (default: 51966)
    --help, -h               Show this help message
    --manager, -m            Run the application in manager mode (default: 
                             false) 
    --nats-bucket            Bucket name used for storing the persistent 
                             context (default: persistent)
    --nats-context-url       NATS server connection string for managing 
                             persistent context (default: 
                             nats://localhost:4222/) 
    --nats-event-url         NATS server connection string for event handling 
                             (default: nats://localhost:4222/)
    --persistent-context, -p Specifies the persistent context type to use 
                             (default: Nats) (values: [Nats])
    --zk-session-timeout     Session timeout for ZooKeeper, in milliseconds 
                             (default: 3000)
    --zk-timeout             Timeout for ZooKeeper connections, in milliseconds 
                             (default: 3000)
    --zk-url                 ZooKeeper connection string (default: 
                             localhost:2181)
```

## Documentation

The Cirrina documentation can be found [here](docs/README.md).

## Dependencies

The following components are used by this version of Cirrina:

| Name                   | Version |
|------------------------|---------|
| Cirrina-Specifications | 2.0     |
| Cirrina-UseCases       | 1.0.0   |

Attributions can be found [here](ATTRIBUTIONS.md).

## Citing

```
```
