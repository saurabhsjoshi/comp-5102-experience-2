package com.saurabh.cas;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactsRepository extends CassandraRepository<Facts, String> {

}
