/**
 *
 */
package com.sddc.dbinterfaces;

/**
 * @author vasco
 *
 */

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sddc.models.datacollectors.Users;

@Repository
public interface UsersRepository extends MongoRepository<Users, String>{
	public Users findByUserName(String username);
}
