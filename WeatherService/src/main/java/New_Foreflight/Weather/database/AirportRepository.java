package New_Foreflight.Weather.database;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirportRepository extends Neo4jRepository<AirportNode, String> {
    Optional<AirportNode> findByIdent(String ident);

    Optional<AirportNode> findByIcao(String icao);
}