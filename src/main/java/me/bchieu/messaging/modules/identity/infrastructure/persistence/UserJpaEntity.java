package me.bchieu.messaging.modules.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.UserStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "app_user")
/** JPA entity for persisted identity users. */
public class UserJpaEntity implements Persistable<UUID> {
  @Id private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(name = "avatar_url", length = 512)
  private String avatarUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private UserStatus status;

  @Version
  @Column(nullable = false)
  private Long version;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserJpaEntity() {}

  /** Creates a persisted identity user entity. */
  public UserJpaEntity(
      UUID id,
      String username,
      String displayName,
      String avatarUrl,
      UserStatus status,
      Long version,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.username = username;
    this.displayName = displayName;
    this.avatarUrl = avatarUrl;
    this.status = status;
    this.version = version;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** Returns the entity identifier for Spring Data. */
  @Override
  public UUID getId() {
    return id;
  }

  /** Returns the entity id. */
  public UUID id() {
    return id;
  }

  /** Indicates whether the entity should be inserted. */
  @Override
  public boolean isNew() {
    return version == null;
  }

  /** Returns the username. */
  public String username() {
    return username;
  }

  /** Returns the display name. */
  public String displayName() {
    return displayName;
  }

  /** Returns the avatar URL. */
  public String avatarUrl() {
    return avatarUrl;
  }

  /** Returns the user status. */
  public UserStatus status() {
    return status;
  }

  /** Returns the entity version. */
  public Long version() {
    return version;
  }

  /** Returns the creation timestamp. */
  public Instant createdAt() {
    return createdAt;
  }

  /** Returns the last update timestamp. */
  public Instant updatedAt() {
    return updatedAt;
  }
}
