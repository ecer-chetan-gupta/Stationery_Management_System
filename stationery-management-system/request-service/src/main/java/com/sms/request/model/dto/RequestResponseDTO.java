package com.sms.request.model.dto;

import com.sms.request.model.RequestItem;
import com.sms.request.model.StationeryRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO returned in API responses for stationery requests.
 */
public class RequestResponseDTO {

    private Long id;
    private Long studentId;
    private String studentEmail;
    private String status;
    private String adminComment;
    private List<ItemDTO> items;
    private LocalDateTime requestDate;
    private LocalDateTime updatedAt;

    public static RequestResponseDTO from(StationeryRequest request) {
        RequestResponseDTO dto = new RequestResponseDTO();
        dto.id = request.getId();
        dto.studentId = request.getStudentId();
        dto.studentEmail = request.getStudentEmail();
        dto.status = request.getStatus().name();
        dto.adminComment = request.getAdminComment();
        dto.requestDate = request.getRequestDate();
        dto.updatedAt = request.getUpdatedAt();
        if (request.getItems() != null) {
            dto.items = request.getItems().stream().map(item -> {
                ItemDTO i = new ItemDTO();
                i.itemId = item.getItemId();
                i.itemName = item.getItemName();
                i.quantity = item.getQuantity();
                return i;
            }).collect(Collectors.toList());
        }
        return dto;
    }

    // ─── Inner DTO ────────────────────────────────────────────────────────────
    public static class ItemDTO {
        public Long itemId;
        public String itemName;
        public Integer quantity;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }
    public Long getStudentId()                    { return studentId; }
    public void setStudentId(Long v)              { this.studentId = v; }
    public String getStudentEmail()               { return studentEmail; }
    public void setStudentEmail(String v)         { this.studentEmail = v; }
    public String getStatus()                     { return status; }
    public void setStatus(String v)               { this.status = v; }
    public String getAdminComment()               { return adminComment; }
    public void setAdminComment(String v)         { this.adminComment = v; }
    public List<ItemDTO> getItems()               { return items; }
    public void setItems(List<ItemDTO> v)         { this.items = v; }
    public LocalDateTime getRequestDate()         { return requestDate; }
    public void setRequestDate(LocalDateTime v)   { this.requestDate = v; }
    public LocalDateTime getUpdatedAt()           { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)     { this.updatedAt = v; }
}
