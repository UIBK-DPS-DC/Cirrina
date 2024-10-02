job "influxdb" {
  datacenters = ["*"]
  type = "service"

  group "influxdb" {
    count = 1

    network {
      port "http" {
        static = 8086
      }
    }

    service {
      port = "http"
      name = "influxdb"

      check {
        type     = "http"
        port     = "http"
        path     = "/health"
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

    task "influxdb" {
      driver = "docker"
      env = {
        DOCKER_INFLUXDB_INIT_MODE        = "setup"
        DOCKER_INFLUXDB_INIT_USERNAME    = "admin"
        DOCKER_INFLUXDB_INIT_PASSWORD    = "adminadmin"
        DOCKER_INFLUXDB_INIT_ORG         = "org"
        DOCKER_INFLUXDB_INIT_BUCKET      = "bucket"
        DOCKER_INFLUXDB_INIT_ADMIN_TOKEN = "bzO10KmR8x"
      }

      config {
        image = "influxdb:2"
        ports = ["http"]
      }
    }
  }
}