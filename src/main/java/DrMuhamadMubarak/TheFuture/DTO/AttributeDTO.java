package DrMuhamadMubarak.TheFuture.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeDTO {
    private String attributeName;
    private DataType dataType;
    private String dataSize;
    private String defaultValue;
    private String referenceEntity;
    private boolean isPrimaryKey;
    private boolean isNullable;
    private boolean displayInList;
    private RelationshipType relationshipType;

    public enum DataType {
        STRING, INTEGER, LONG, BOOLEAN, DATE, DOUBLE, FLOAT
    }

    public enum RelationshipType {
        NONE, ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY
    }
}
