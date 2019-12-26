package space.decarbnow.collector.entities;

import lombok.Data;

import javax.persistence.Entity;
import java.time.LocalDateTime;

@Data
@Entity
public class TwitterStatus {
    private LocalDateTime lastStatus;
    private String status;
}
