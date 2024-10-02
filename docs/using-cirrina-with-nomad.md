# Using Cirrina with Nomad

This page will describe the usage of Cirrina with [Hashicorp Nomad](https://www.nomadproject.io/). HashiCorp Nomad is a simple yet powerful
workload orchestrator designed to deploy and manage applications across diverse environments. It supports containers, legacy applications,
microservices, and batch jobs, providing a unified solution for a range of workloads. Nomad works across cloud, on-premise, and hybrid
infrastructures. Compared to complex orchestrators like Kubernetes, Nomad is lightweight and easier to set up. It can schedule containers
(like Docker), VMs, binaries, and more. With high availability built in, it scales from a single node to thousands, ensuring reliability
with failover and leader election. Nomad integrates seamlessly with other HashiCorp tools like Consul for networking and Vault for secrets
management. It uses a declarative job specification language, which allows users to define jobs, resources, and deployment strategies in a
flexible way.

Nomad is ideal for orchestrating mixed workloads, including both containerized and non-containerized applications. It efficiently schedules
batch processing jobs and provides a streamlined solution for managing workloads across hybrid or multi-cloud environments. Overall, Nomad
offers a flexible, scalable, and easy-to-use platform for modern infrastructure automation.

# With Vagrant

Vagrant is an open-source tool that simplifies managing virtualized development environments. It allows developers to create and configure
lightweight, reproducible, and portable virtual machines (VMs) using a simple configuration file. Vagrant works with various virtualization
providers, like VirtualBox, enabling users to create consistent environments for development, testing, or deployment across different
systems. It helps streamline workflows by ensuring all developers work in identical environments. It reduces the "it works on my machine"
problem and supports automation by integrating configuration management tools like Ansible.

All instructions in this section relate to the configuration found in `resources/nomad-configuration`.

## Prerequisites

Vagrant, VirtualBox and Python are assumed to be installed.

Also, the [vagrant-docker-compose](https://github.com/leighmcculloch/vagrant-docker-compose) plugin must be installed:

```bash
vagrant plugin install vagrant-docker-compose
```

## Run VMs

Use Vagrant to bring the configured VMs up as follows:

```bash
vagrant up --provision --parallel
```

It may be necessary to adjust the allowed IP ranges in `/etc/vbox/networks.conf`:

```
* 10.0.0.0/8 192.168.0.0/16
* 2001::/64
```

The output should reflect that the VMs are put online and configured through Ansible. The status can be checked as follows:

```bash
vagrant status
````

Which should produce something along the lines of:

```
Current machine states:

server                    running (virtualbox)
client1                   running (virtualbox)
client2                   running (virtualbox)

This environment represents multiple VMs. The VMs are all listed
above with their current state. For more information about a specific
VM, run `vagrant status NAME`.
```

The Consul and Nomad UIs should be available through:

- **Consul**: http://localhost:8500/
- **Nomad**: http://localhost:4646/

A "Hello World!" Nomad job is provided based on [nginxdemos/hello](https://hub.docker.com/r/nginxdemos/hello/), to run it, connect to the
server VM and run the job:

```bash
cd /vagrant/
nomad job run helloworld.nomad
```

A [Fabio](https://fabiolb.net/) (load balancer) job is also provided. When Fabio is running, the "Hello World!" service can be accessed from
any node through:

```bash
curl http://10.10.2.21:9999/ | w3m -T text/html
```

Assuming that [w3m](https://w3m.sourceforge.net/) is installed.

Nomad jobs can be stopped (and purged) with:

```bash
nomad job stop -purge job-name
```

To destroy the VMs, you can use:

```bash
vagrant hosts list | cut -f 2 -d ' ' | xargs -L 1 vagrant destroy -f --no-tty
```

The services required by Cirrina are provided as Nomad jobs:

- InfluxDB
- Telegraf
- ZooKeeper
- NATS

The Cirrina Nomad job is used to deploy Cirrina on the Nomad cluster, it is provided as a _system_ service, meaning that the runtime gets
deployed on all clients in the Nomad cluster. Communication with dependencies (Telegraf, ZooKeeper, and NATS) happens by means of Consul
DNS forwarding.

**Note:** This will destroy all hosts!

## Acknowledgements

Thanks to [Manoj Govindan](https://github.com/egmanoj) for making his Vagrant and Nomad setup available on GitHub, which the Vagrant + Nomad
configuration is based on: https://github.com/egmanoj/hashilab