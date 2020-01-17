package space.decarbnow.collector.pojos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TwitterStatus {
    private LocalDateTime lastStatusDateTime;
    private String status;
}
