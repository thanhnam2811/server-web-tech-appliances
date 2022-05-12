package com.hcmute.backendtechnologicalapplianceswebsite.repository;

import com.hcmute.backendtechnologicalapplianceswebsite.model.Brand;
import com.hcmute.backendtechnologicalapplianceswebsite.model.Category;
import com.hcmute.backendtechnologicalapplianceswebsite.model.Product;
import com.hcmute.backendtechnologicalapplianceswebsite.utils.fileUtils.MyUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    Product findByProductId(String id);

    default String generateProductId() {
        String PREFIX = "P";
        int index = PREFIX.length();
        int length = 2;

        List<Product> products = findAll();

        products.sort((c1, c2) -> {
            Long id1 = Long.parseLong(c1.getProductId().substring(index));
            Long id2 = Long.parseLong(c2.getProductId().substring(index));
            return id2.compareTo(id1);
        });

        String lastProductId = products.get(0).getProductId();

        return MyUtils.generateID(PREFIX, length, lastProductId);
    }

    Iterable<Product> findAllByBrand(Brand brand);

    @Query(value = "select  * from Products,(select ProductId,sum(Quantity)as Sum from OrderDetails group by ProductId)as temp where temp.ProductId=Products.ProductId", nativeQuery = true)
    Iterable<Product> gettopseller();
    @Query(value = "select * from Products,(select ProductId,AVG(Rate) as Sum from Reviews group by ProductId)as temp where temp.ProductId=Products.ProductId", nativeQuery = true)
    Iterable<Product> gettopfeature();

}