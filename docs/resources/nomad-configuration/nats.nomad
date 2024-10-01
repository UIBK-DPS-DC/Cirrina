job "nats" {
  region = "global"
  datacenters = ["dc-local"]
  type   = "service"

  group "nats" {
    count = 1

    network {
      port "client" {
        static = 4222
      }
      port "routing" {
        static = 6222
      }
      port "monitoring" {
        static = 8222
      }
    }

    service {
      port = "client"
      name = "nats"

      check {
        type     = "http"
        port     = "monitoring"
        path     = "/connz"
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

    task "nats" {
      driver = "docker"

      config {
        image = "nats:latest"
        ports = ["client", "monitoring", "routing"]
        args = [
          "-js",
          "-m", "8222"
        ]
      }
    }
  }
}