job "telegraf" {
  region = "global"
  datacenters = ["dc-local"]
  type   = "service"

  group "telegraf" {
    count = 1

    network {
      port "oltp" {
        static = 4317
      }
      port "http" {
        static = 8080
      }
    }

    service {
      port = "oltp"
      name = "telegraf"

      check {
        type     = "http"
        port     = "http"
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

    task "telegraf" {
      driver = "docker"

      template {
        change_mode = "noop"
        destination = "telegraf/telegraf.conf"
        data        = <<EOH
[[inputs.opentelemetry]]
[[outputs.influxdb_v2]]
  ## The URLs of the InfluxDB cluster nodes.
  ##
  ## Multiple URLs can be specified for a single cluster, only ONE of the
  ## urls will be written to each interval.
  ##   ex: urls = ["https://us-west-2-1.aws.cloud2.influxdata.com"]
  urls = ["http://influxdb.service.consul:8086"]

  ## Token for authentication.
  token = "bzO10KmR8x"

  ## Organization is the name of the organization you wish to write to.
  organization = "org"

  ## Destination bucket to write into.
  bucket = "bucket"

[[outputs.health]]
EOH
      }

      config {
        image = "telegraf:latest"
        ports = ["oltp", "http"]
        volumes = ["telegraf/telegraf.conf:/etc/telegraf/telegraf.conf"]
        cap_add = ["net_raw"]
      }
    }
  }
}