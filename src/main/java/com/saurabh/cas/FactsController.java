package com.saurabh.cas;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.UUID;

@RestController
public class FactsController {


    private final CassandraTemplate cassandraTemplate;

    @Value("${facts.server:http://localhost:8080}")
    private String factsServer;

    private final FactsRepository repository;

    @Autowired
    public FactsController(CassandraTemplate cassandraTemplate, FactsRepository repository) {
        this.cassandraTemplate = cassandraTemplate;
        this.repository = repository;
    }

    /**
     * Dummy API.
     */
    @GetMapping("/fact")
    public Fact getFact() {
        return new Fact(
                "c41bdd70-5647-11ec-ad79-05ddb40c09e3",
                "Statistics indicate that animal lovers in recent years have shown a preference for printers over scanners!",
                "Cat fact"
        );
    }


    @PostMapping("/fact-collection")
    public Mono<Facts> saveFacts(@RequestParam(defaultValue = "5") int count,
                                 @RequestParam(defaultValue = "normal") String consistency) {

        var webClient = WebClient.create(factsServer + "/fact");
        return Flux.range(0, count)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(i -> webClient.get().retrieve().bodyToMono(Fact.class))
                .map(fact -> fact.fact)
                .collectSortedList(Comparator.naturalOrder())
                .map(f -> {

                    var facts = new Facts();
                    facts.setKey(UUID.randomUUID().toString());
                    facts.setFacts(f);

                    return cassandraTemplate.insert(facts, InsertOptions.builder()
                                    .consistencyLevel(getConsistencyLevel(consistency))
                                    .build())
                            .getEntity();

                });
    }

    @GetMapping("/fact-collection")
    public Flux<Facts> getFacts(@RequestParam(defaultValue = "normal") String consistency) {

        return Flux.fromStream(cassandraTemplate.stream(QueryBuilder
                        .selectFrom("Facts")
                        .all()
                        .build()
                        .setConsistencyLevel(getConsistencyLevel(consistency)),
                Facts.class));
    }

    @GetMapping("/fact-collection/{key}")
    public Mono<Facts> getFacts(@PathVariable String key, @RequestParam(defaultValue = "normal") String consistency) {
        return Mono.justOrEmpty(cassandraTemplate.select(QueryBuilder
                                .selectFrom("Facts")
                                .all()
                                .whereColumn("key")
                                .isEqualTo(QueryBuilder.literal(key))
                                .build()
                                .setConsistencyLevel(getConsistencyLevel(consistency)),
                        Facts.class)
                .stream()
                .findAny());
    }

    @DeleteMapping("/fact-collection/{key}")
    public Mono<Boolean> deleteFacts(@PathVariable String key) {
        return Mono.justOrEmpty(cassandraTemplate.deleteById(key, Facts.class));
    }

    private static ConsistencyLevel getConsistencyLevel(String consistency) {
        if (consistency.equalsIgnoreCase("weak")) {
            return ConsistencyLevel.ONE;
        } else if (consistency.equalsIgnoreCase("strong")) {
            return ConsistencyLevel.ALL;
        } else {
            return ConsistencyLevel.QUORUM;
        }
    }
}
