# dynamic-routing-gateway-spring-boot-starter
this is a gateway starter with dynamic routing based on spring cloud gateway and nacos
## Background

Spring Cloud Gateway and Nacos can very easily integrated with each other, and if you enable the nacos as service registry center and turn on the discovery locator （`spring.cloud.gateway.discovery.locator.enable = true`）, then the gateway already got a default routing rules. Let's assume you have a service 'service-provider', when the SP register to Nacos, spring cloud gateway will detect it , and a defult route for the service 'service-provider' will be created, the rule basically like :

```
{
    "id": "spid",
    "order": 0,
    "uri": "lb://service-provider",
    "predicates": [{
        "name":"Path",
        "args":{
          "pattern": "/service-provider/**"
        }
      }
    ],
    "filters": [{
        "name":"RewritePath",
        "args":{
          "regexp": "/service-provider/(?<segment>.*)",
          "replacement": "/$\\{segment}"
        }
      }
    ]
  }
```

when you access url `http://hostname_of_gateway:port_of_gateway/service-provider/example`  , the gateway will match the routing rule, and redirect the request to service-provider, and the url will be  /example.

The problem is sometimes we need some customized routing rule, like rules based on header and we need to take the rule configuration into effect immediately, that's what we just need, a dynamic route function.

## About Implementation

* It's based on spring cloud gateway and spring cloud nacos 
* create configuration in nacos as the routing configuration
* init the gateway routes and add a nacos config listener (when the configuration changed, update the gateway routing rule and fresh.)

## How to use it?

* install it to your repository and add it in you maven dependence of the gateway project

* create the routing configuration in nacos

* add the application configuration, example as below:

  ```
  server:
    port: <your-server-port>
  spring:
    application:
      name: <your-application-name>
    cloud:
      nacos:
        config:
          server-addr: <your-nacos-host:port>
          namespace: <your-config-namespapce>
          group: <your-group>
          file-extension: yaml #or others format
        discovery:
          server-addr: <your-nacos-host:port>
          namespace: <your-config-namespapce>
          group: dev
        httpclient:
          connect-time: 1000
          response-timeout: 5s
    config:
      import: "nacos:<your-nacos-data-id>"
  ###################### this is the core part of this project ######################
  gateway:
    routes:
      config:
        useDynamicRoutes: true
        data-id: <your-nacos-config-data-id>
        namespace: <your-config-namespapce>
        group: <your-group>
  ####################################################################################
  ```

  

