package com.ufrn.ppgti.servio.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "coupon_usages", uniqueConstraints = @UniqueConstraint(name = "uk_coupon_client", columnNames = {
        "coupon_id", "client_id" }))
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private LocalDateTime usedAt;

    public CouponUsage() {
    }

    public CouponUsage(Coupon coupon, User client, Order order) {
        this.coupon = coupon;
        this.client = client;
        this.order = order;
    }

    @PrePersist
    public void prePersist() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}
