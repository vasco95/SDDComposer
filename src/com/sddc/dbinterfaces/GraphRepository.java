package com.sddc.dbinterfaces;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sddc.models.Graph;

@Repository
public interface GraphRepository extends MongoRepository<Graph, String> {
	public List<Graph> findByUserName(String username);
	public Graph findByGraphId(String graphId);
}
