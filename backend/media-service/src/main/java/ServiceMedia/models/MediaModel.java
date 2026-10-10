package ServiceMedia.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Getter
@Setter
@Document(collection = "medias")
public class MediaModel {

    @Id
    private String id;

    private String imagePath;

    @Indexed
    private String ownerId;

    @Indexed
    private String productId;

    private String mediaType;
}