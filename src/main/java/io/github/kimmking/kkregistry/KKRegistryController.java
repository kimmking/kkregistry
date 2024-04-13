package io.github.kimmking.kkregistry;

import io.github.kimmking.kkregistry.cluster.Cluster;
import io.github.kimmking.kkregistry.cluster.Server;
import io.github.kimmking.kkregistry.cluster.Snapshot;
import io.github.kimmking.kkregistry.model.InstanceMeta;
import io.github.kimmking.kkregistry.service.KKRegistryService;
import io.github.kimmking.kkregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Rest controller for registry service.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 19:49
 */

@RestController
@Slf4j
public class KKRegistryController {

    @Autowired RegistryService registryService;
    @Autowired Cluster cluster;

    @RequestMapping("/reg")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> register {} @ {}", service, instance);
        checkLeader();
        return registryService.register(service, instance);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> unregister {} @ {}", service, instance);
        checkLeader();
        return registryService.unregister(service, instance);
    }


    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam String service) {
        log.info(" ===> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("/renew")
    public long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {}", service, instance);
        checkLeader();
        return registryService.renew(instance, service);
    }

    @RequestMapping("/renews")
    public long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {}", services, instance);
        checkLeader();
        return registryService.renew(instance, services.split(","));
    }

    @RequestMapping("/version")
    public long version(@RequestParam String service) {
        log.info(" ===> version {}", service);
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String services) {
        log.info(" ===> versions {}", services);
        return registryService.versions(services.split(","));
    }

    @RequestMapping("/snapshot")
    public Snapshot snapshot() {
        log.info(" ===> snapshot");
        return KKRegistryService.snapshot();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ===> cluster");
        return cluster.getServers();
    }

    @RequestMapping("/info")
    public Server info() {
        return myself();
    }

    @RequestMapping("/myself")
    public Server myself() {
        return cluster.myself();
    }

    @RequestMapping("/sm")
    public Server setMaster() {
        cluster.myself().setLeader(!cluster.isLeader());
        return cluster.myself();
    }

    @RequestMapping("/")
    public List<Server> root() {
        return cluster();
    }

    void checkLeader() {
        if (!cluster.isLeader()) {
            log.error("this server {} is readonly slave, leader {} is writable.",
                    myself().getUrl(), cluster.getLeader().getUrl());
            throw new RuntimeException("this server[" + myself().getUrl()
                    +"] is a slave, can't be written.");
        }
    }

}
