job "fabio" {
  region = "global"
  datacenters = ["dc-local"]
  type   = "system"

  group "fabio" {
    network {
      port "lb" {
        static = 9999
      }
      port "ui" {
        static = 9998
      }
    }

    task "fabio" {
      driver = "docker"
      config {
        image        = "fabiolb/fabio"
        network_mode = "host"
        ports = ["lb", "ui"]
      }
    }
  }
}