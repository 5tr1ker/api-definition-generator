package com.ideatec.definition_generator.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SchemaRepository {

    private final EntityManager em;

    public List<String> getSchema(String prefix) {
        return null;
    }

}
