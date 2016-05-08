package com.sddc.dbinterfaces;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sddc.models.GraphTemplate;

/**
 * @author vasco
 *
 */
@Repository
public interface TemplateRepository extends MongoRepository<GraphTemplate, String>{
	public List<GraphTemplate> findAllByUserName(String userName);
	public GraphTemplate findByGraphId(String graphId);
}
