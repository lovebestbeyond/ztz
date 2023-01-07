package io.github.ztianz.uid.snowflake.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdWorkerNode implements Serializable {
    private static final long serialVersionUID = 2248324163212982640L;

    private Long id;

    private String applicationName;

    private Long workId;

    private String hostName;

    private Integer serverPort;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

    private Integer cycleMinutes;

    private Boolean heartBeat;
}
