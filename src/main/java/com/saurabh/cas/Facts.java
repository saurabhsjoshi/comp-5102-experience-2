package com.saurabh.cas;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Table
@Data
public class Facts {

    /**
     * The key.
     */
    @PrimaryKey
    private String key;

    @Column
    private List<String> facts = new ArrayList<>();
}
