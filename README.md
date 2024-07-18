# Cirrina

<div align="center">
    <img src="cirrina.svg" alt="Logo" width="400"/>
</div>

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE.md) [![Build](https://github.com/UIBK-DPS-DC/Cirrina/actions/workflows/build.yml/badge.svg)](https://github.com/UIBK-DPS-DC/Cirrina/actions/workflows/build.yml) [![Test Report](https://github.com/UIBK-DPS-DC/Cirrina/actions/workflows/report.yml/badge.svg)](https://github.com/UIBK-DPS-DC/Cirrina/actions/workflows/report.yml)

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
    --nats-persistent-context-url nats://192.168.64.84:4222/ \
    --nats-event-handler-url nats://192.168.64.84:4222/ \
    --zookeeper-connect-string 192.168.64.84:2181
```

The following arguments are expected to be provided (otherwise it is assumed that the dependent services are running on the local host).

- `--nats-persistent-context-url` The NATS server URL where the persistent context resides.
- `--nats-event-handler-url` The NATS server URL where the event bus resides.
- `--zookeeper-connect-string` The ZooKeeper server connection string.

## Citing

```
```
