package com.hcmute.backendtechnologicalapplianceswebsite.repository;

import com.hcmute.backendtechnologicalapplianceswebsite.model.Delivery;
import com.hcmute.backendtechnologicalapplianceswebsite.model.Order;
import com.hcmute.backendtechnologicalapplianceswebsite.model.OrderDetailId;
import com.hcmute.backendtechnologicalapplianceswebsite.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Order findByOrderId(String id);
    default String generateOrderId() {

        List<Order> orders = findAll();

        orders.sort((c1, c2) -> c2.getOrderId().compareTo(c1.getOrderId()));

        String lastOrderId = orders.get(0).getOrderId();

        return "OD" + String.format("%05d", Integer.parseInt(lastOrderId.substring(2)) + 1);
    }

    Iterable<Order> findAllByUser(User user);
}