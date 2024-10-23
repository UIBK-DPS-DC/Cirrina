# Using Cirrina with Kubernetes

The following guide will help with setting up a local Kubernetes cluster using [Incus](https://linuxcontainers.org/incus/)
and [Talos](https://www.talos.dev/). Incus is a versatile tool for managing containers with LXC and virtual machines (VMs) using QEMU. It
offers a fast and efficient way to create VMs. Forked from LXD, Incus provides a powerful alternative to traditional virtualization
platforms like VirtualBox. Using KVM for hardware-accelerated virtualization via QEMU, Incus enables rapid VM deployment, which is ideal for
scenarios requiring multiple instances. Talos is a Linux distribution centered around Kubernetes that allows for easy and lightweight
Kubernetes deployment.

## Prerequisites

- Incus: A container and VM management tool.
- firewalld: The firewall management tool.
- KVM: Ensure your system supports hardware-accelerated virtualization via KVM.

## Download the Talos image

Refer to the Talos [documentation](https://www.talos.dev/v1.8/talos-guides/install/bare-metal-platforms/iso/) for acquiring the Talos bare
metal image. For example, the version 1.8 bare metal image can be acquired through:

```bash
https://factory.talos.dev/image/376567988ad370138ad8b2698212367b8edcb69b5fd68c80be1f2ec7d603b4ba/v1.8.0/metal-amd64.iso
```

## Create a storage device for the Talos image

Create a storage device which represents a CD-ROM drive to boot the Talos image:

```bash
incus storage volume import default metal-amd64.iso talos-iso --type=iso
incus config device add talos-1 talos-iso disk pool=default source=talos-iso boot.priority=10
```

This assumes that a pool called _default_ exists.

## Start multiple VMs

Using the _start.sh_ script provided in the _resources/kubernetes-configuration_ directory, start multiple VMs:

```bash
./start 5
```

Note that the above command will start 5 VMs each with 100 GB of disk space and 4 GB of memory. Make sure that you host machine is capable
of providing these resources.

## Install the control plane

Generate the configuration as follows:

```bash
$ export CONTROL_PLANE_ID="10.68.37.60"
$ talosctl gen config cluster https://CONTROL_PLANE_IP:6443 --install-disk /dev/sda
```

Make sure to provide the correct _CONTROL_PLANE_ID_.

Apply the generated config as follows:

```bash
talosctl -n $CONTROL_PLANE_IP apply-config --insecure --file controlplane.yaml
```

Next, configure the control plane endpoint:

```bash
export TALOSCONFIG=$(realpath ./talosconfig)
talosctl config endpoint $CONTROL_PLANE_IP
```

And bootstrap:

```bash
talosctl -n $CONTROL_PLANE_IP bootstrap
```

## Join worker nodes

Worker nodes can be added as follows:

```bash
talosctl -n $NODE_IP apply-config --insecure --file worker.yaml
```

## Managing using kubectl

Once the cluster is set up, download the kubeconfig file and start interacting with Kubernetes using kubectl:

```bash
talosctl -n $CONTROL_PLANE_IP kubeconfig ./kubeconfig
kubectl --kubeconfig ./kubeconfig get node -owide
```

## Documentation

For more detailed documentation, refer to:

- [https://linuxcontainers.org/incus/docs/main/](https://linuxcontainers.org/incus/docs/main/)
- [https://www.talos.dev/v1.8/](https://www.talos.dev/v1.8/)