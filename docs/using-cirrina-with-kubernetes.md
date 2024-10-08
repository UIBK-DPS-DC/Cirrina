# Using Cirrina with Kubernetes

This page will describe the usage of Cirrina with [Kubernetes](https://kubernetes.io/). Kubernetes is a powerful, open-source container
orchestration platform designed to automate the deployment, scaling, and management of containerized applications. It supports diverse
workloads, from microservices to batch processing jobs, and provides a unified approach for managing containers at scale.

Kubernetes operates across cloud, on-premise, and hybrid infrastructures, offering a highly flexible and resilient architecture. It ensures
high availability and fault tolerance with built-in features like automatic scaling, self-healing, and rolling updates. Kubernetes is
designed to manage large, complex clusters of containers, making it ideal for environments where reliability and scalability are key.

Kubernetes allows users to define workloads using declarative YAML manifests, where resources, deployments, and services are described in a
structured and consistent manner. The platform also integrates seamlessly with a wide range of tools, such as [Helm](https://helm.sh/) for
managing application packages and [Istio](https://istio.io/) for service mesh management. Kubernetes provides powerful networking
capabilities and integrates easily with secrets management tools like [HashiCorp Vault](https://www.vaultproject.io/).

While Kubernetes can handle large, complex deployments, its flexibility and broad ecosystem make it an ideal choice for orchestrating both
containerized and microservice-based applications. Kubernetes excels in multi-cloud or hybrid environments, providing a robust, scalable
solution for modern infrastructure automation.

# With Vagrant

Vagrant is an open-source tool that simplifies managing virtualized development environments. It allows developers to create and configure
lightweight, reproducible, and portable virtual machines (VMs) using a simple configuration file. Vagrant works with various virtualization
providers, like VirtualBox, enabling users to create consistent environments for development, testing, or deployment across different
systems. It helps streamline workflows by ensuring all developers work in identical environments. It reduces the "it works on my machine"
problem and supports automation by integrating configuration management tools like Ansible.

All instructions in this section relate to the configuration found in `resources/kubernetes-configuration`.

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

Kubernetes should now be running.

To destroy the VMs, you can use:

```bash
vagrant hosts list | cut -f 2 -d ' ' | xargs -L 1 vagrant destroy -f --no-tty
```

**Note:** This will destroy all hosts!