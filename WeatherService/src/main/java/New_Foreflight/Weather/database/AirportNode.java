package New_Foreflight.Weather.database;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Node("Airport")
public class AirportNode {

    @Id
    @Property("ident")
    private String ident;

    @Property("icao")
    private String icao;

    @Property("name")
    private String name;

    @Property("latitude")
    private double latitude;

    @Property("longitude")
    private double longitude;

    @Property("iapExists")
    private int iapExists;

    @Property("windsAloftAirport")
    private boolean windsAloftAirport;

    @Override
    public String toString() {
        return "Airport{" + "ident='" + ident + '\'' + ", icao='" + icao + '\'' + ", name='" + name + '\''
                + ", latitude=" + latitude + ", longitude=" + longitude + ", iapExists=" + iapExists
                + ", windsAloftAirport=" + windsAloftAirport + '}';
    }
}
