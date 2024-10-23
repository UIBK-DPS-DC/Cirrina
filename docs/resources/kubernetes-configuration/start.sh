#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <number_of_instances>"
  exit 1
fi

n=$1

for ((i=1; i<=n; i++)) do
  instance_name="talos-$i"

  incus init $instance_name --empty --vm -c limits.cpu=1 -c limits.memory=4GiB -d root,size=100GiB
  incus config device add $instance_name talos-iso disk pool=default source=talos-iso boot.priority=10
  incus start $instance_name

  echo "$instance_name ✓"
done