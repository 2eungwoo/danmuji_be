package com.back2basics.adapter.persistence.history;

import com.back2basics.history.model.DomainType;
import com.back2basics.history.model.HistoryType;
import com.back2basics.user.model.Role;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Document("history_documents")
public class HistoryDocument {

    @Id
    private ObjectId id;

    @Field("history_type")
    private HistoryType historyType;

    @Field("domain_type")
    private DomainType domainType; // user, project, step, post ...

    @Field("domain_id")
    private String domainId;

    @Field("changed_at")
    private LocalDateTime changedAt;

    @Field("changer_id")
    private String changerId;

    @Field("changer_name")
    private String changerName;

    @Field("changer_username")
    private String changerUsername;

    @Field("changer_role")
    private Role changerRole;

    @Field("before")
    private Map<String, Object> before;

    @Field("after")
    private Map<String, Object> after;

    @CreatedDate
    @Field("history_created_at")
    private LocalDateTime createdAt;

    @Field("message")
    private String message;

    @Indexed(expireAfterSeconds = 0)
    @Field("expire_at")
    private Date expireAt;

    public HistoryDocument(ObjectId id,
        HistoryType historyType,
        DomainType domainType,
        String domainId,
        LocalDateTime changedAt,
        String changerId,
        String changerName,
        String changerUsername,
        Role changerRole,
        Map<String, Object> before,
        Map<String, Object> after, LocalDateTime createdAt, String message, Date expireAt) {
        this.id = id;
        this.historyType = historyType;
        this.domainType = domainType;
        this.domainId = domainId;
        this.changedAt = changedAt;
        this.changerId = changerId;
        this.changerName = changerName;
        this.changerUsername = changerUsername;
        this.changerRole = changerRole;
        this.before = before;
        this.after = after;
        this.createdAt = createdAt;
        this.message = message;
        this.expireAt = expireAt;
    }

    public static HistoryDocument of(HistoryType historyType,
        DomainType domainType,
        Long domainId,
        LocalDateTime changedAt,
        String changerId,
        String changerName,
        String changerUsername,
        Role changerRole,
        Map<String, Object> before,
        Map<String, Object> after, String message) {
        return new HistoryDocument(
            new ObjectId(),
            historyType,
            domainType,
            String.valueOf(domainId),
            changedAt,
            changerId,
            changerName,
            changerUsername,
            changerRole,
            before,
            after,
            null,
            message,
            null
        );
    }
}
