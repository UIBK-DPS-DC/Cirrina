job "helloworld" {
  region = "global"
  datacenters = ["dc-local"]
  type   = "service"

  group "helloworld" {
    count = 3
    network {
      port "http" {
        to = 80
      }
    }

    service {
      name = "nginx-webserver"
      tags = ["urlprefix-/"]
      port = "http"
      check {
        name     = "alive"
        type     = "http"
        path     = "/"
        interval = "10s"
        timeout  = "2s"
      }
    }

    restart {
      attempts = 2
      interval = "30m"
      delay    = "15s"
      mode     = "fail"
    }

    task "nginx" {
      driver = "docker"
      config {
        image = "nginxdemos/hello"
        ports = ["http"]
      }
    }
  }
}