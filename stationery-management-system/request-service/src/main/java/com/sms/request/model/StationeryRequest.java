package com.sms.request.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * StationeryRequest entity — maps to `stationery_requests` table in request_db.
 */
@Entity
@Table(name = "stationery_requests")
@EntityListeners(AuditingEntityListener.class)
public class StationeryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private String studentEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminComment;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RequestItem> items = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestDate;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Status { PENDING, APPROVED, REJECTED, FULFILLED }

    public StationeryRequest() {}

    // --- Getters & Setters ----------------------------------------------------
    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public Long getStudentId()                       { return studentId; }
    public void setStudentId(Long studentId)         { this.studentId = studentId; }
    public String getStudentEmail()                  { return studentEmail; }
    public void setStudentEmail(String email)        { this.studentEmail = email; }
    public Status getStatus()                        { return status; }
    public void setStatus(Status status)             { this.status = status; }
    public String getAdminComment()                  { return adminComment; }
    public void setAdminComment(String adminComment) { this.adminComment = adminComment; }
    public List<RequestItem> getItems()              { return items; }
    public void setItems(List<RequestItem> items)    { this.items = items; }
    public LocalDateTime getRequestDate()            { return requestDate; }
    public void setRequestDate(LocalDateTime v)      { this.requestDate = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }
}
