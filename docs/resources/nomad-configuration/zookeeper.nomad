job "zookeeper" {
  region = "global"
  datacenters = ["dc-local"]
  type   = "service"

  group "zookeeper" {
    count = 1

    network {
      port "client" {
        static = 2181
      }
      port "follow" {
        static = 2888
      }
      port "election" {
        static = 3888
      }
      port "http" {
        static = 8080
      }
    }

    service {
      port = "client"
      name = "zookeeper"
    }

    restart {
      attempts = 10
      interval = "5m"
      delay    = "25s"
      mode     = "delay"
    }

    task "zookeeper" {
      driver = "docker"

      config {
        image = "zookeeper:latest"
        ports = ["client", "follow", "election", "http"]
      }
    }
  }
}