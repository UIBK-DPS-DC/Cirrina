job "cirrina" {
  datacenters = ["*"]
  type = "system"

  group "cirrina" {
    network {
      port "monitoring" {
        static = 51966
      }
    }

    service {
      name = "cirrina"

      check {
        type     = "http"
        port     = "monitoring"
        path     = "/"
        interval = "5s"
        timeout  = "2s"
      }
    }

    restart {
      attempts = 10
      interval = "5m"
      delay    = "25s"
      mode     = "delay"
    }

    task "cirrina" {
      driver = "docker"

      config {
        image = "marlonetheredgeuibk/cirrina:latest"
        ports = ["monitoring"]
        args = [
          "--nats-persistent-context-url", "nats.service.consul",
          "--nats-event-handler-url", "nats.service.consul",
          "--zookeeper-connect-string", "zookeeper.service.consul"
        ]
      }
    }
  }
}
