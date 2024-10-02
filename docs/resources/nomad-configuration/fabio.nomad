job "fabio" {
  datacenters = ["*"]
  type = "system"

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
        ports = ["lb", "ui"]
        network_mode = "host"
      }
    }
  }
}