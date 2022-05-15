package com.hcmute.backendtechnologicalapplianceswebsite.controller;


import com.hcmute.backendtechnologicalapplianceswebsite.model.Brand;
import com.hcmute.backendtechnologicalapplianceswebsite.model.Category;
import com.hcmute.backendtechnologicalapplianceswebsite.model.Product;
import com.hcmute.backendtechnologicalapplianceswebsite.repository.BrandRepository;
import com.hcmute.backendtechnologicalapplianceswebsite.repository.CategoryRepository;
import com.hcmute.backendtechnologicalapplianceswebsite.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.hcmute.backendtechnologicalapplianceswebsite.exception.ResourceNotFoundException;
import com.hcmute.backendtechnologicalapplianceswebsite.model.dashboad.Chart;
import com.hcmute.backendtechnologicalapplianceswebsite.model.dashboad.TopCustomer;
import com.hcmute.backendtechnologicalapplianceswebsite.model.dashboad.TopProduct;
import com.hcmute.backendtechnologicalapplianceswebsite.model.dashboad.TotalSales;
import com.hcmute.backendtechnologicalapplianceswebsite.repository.dashboard.ChartRepository;
import com.hcmute.backendtechnologicalapplianceswebsite.repository.dashboard.TopCustomerRepository;
import com.hcmute.backendtechnologicalapplianceswebsite.repository.dashboard.TopProductRepository;
import com.hcmute.backendtechnologicalapplianceswebsite.repository.dashboard.TotalSalesRepository;
import com.hcmute.backendtechnologicalapplianceswebsite.utils.fileUtils.upload.FileUploadUtil;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"})
@RestController
@RequestMapping("/api/technological_appliances/")
public class ProductController {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ChartRepository chartRepository;
    private final TopProductRepository topProductRepository;
    private final TopCustomerRepository topCustomerRepository;
    private final TotalSalesRepository totalSalesRepository;

    public ProductController(ProductRepository productRepository, BrandRepository brandRepository, CategoryRepository categoryRepository,ChartRepository chartRepository,TopProductRepository topProductRepository,TopCustomerRepository topCustomerRepository,TotalSalesRepository totalSalesRepository) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.chartRepository=chartRepository;
        this.topProductRepository=topProductRepository;
        this.topCustomerRepository=topCustomerRepository;
        this.totalSalesRepository=totalSalesRepository;
    }

    // Get All Products
    @GetMapping(value = "/products")
    public Iterable<Product> getAllProducts() {
        return productRepository.findAll();
    }

    private String GetFileNameImg(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            return file.getOriginalFilename();
        }
        return null;
    }

    // Get all product by brand
    @GetMapping(value = "/products/brand/{brandId}")
    public Iterable<Product> getAllProductsByBrand(@PathVariable String brandId) {
        log.info("Get all product by brand: " + brandId);
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with id: " + brandId));
        return productRepository.findAllByBrand(brand);
    }

    //    Create product
    @PostMapping(value = "/products", consumes = {"multipart/form-data"})
    public Product createProduct(Product product, @RequestParam(value = "files", required = false) MultipartFile[] uploadedFiles) {
        //  Default value for productId
        product.setProductId(productRepository.generateProductId());

        if (uploadedFiles != null && uploadedFiles.length > 0) {
            for (MultipartFile file : uploadedFiles) {
                String fileName = GetFileNameImg(file);
                product.setImage(fileName);
            }
        }

        Brand brand = brandRepository.findById(product.getBrand().getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with id: " + product.getBrand().getBrandId()));
        product.setBrand(brand);

        Category category = categoryRepository.findById(product.getCategory().getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + product.getCategory().getCategoryId()));

        product.setCategory(category);

        if (product.getSaleDate() == null) {
            product.setSaleDate(new Date());
        }

        log.info("Create product: " + product);
        return productRepository.save(product);
    }

    // Get top products newest
    @GetMapping(value = "/products/newest/{quantity}")
    public List<Product> getTopProductsNewest(@PathVariable Integer quantity) {
        log.info("Get top " + quantity + " products newest");
        return productRepository.findAll(Sort.by("saleDate", "ProductId").descending()).subList(0, quantity);
    }

    @GetMapping(value = "/products/topseller")
    public Iterable<Product> getTopSeller() {
        log.info("Get top seller");
        return productRepository.gettopseller();
    }

    @GetMapping(value = "/products/topfeature")
    public Iterable<Product> getTopFeature() {
        log.info("Get top feature");
        return productRepository.gettopfeature();
    }

    @GetMapping(value = "/products/hottrend")
    public Iterable<Product> getHotTrend() {
        log.info("Get hot trend");
        return productRepository.gethottrend();
    }

    // Get min price
    @GetMapping(value = "/products/price/min")
    public Double getMinPrice() {
        List<Product> products = productRepository.findAll();
        Double minPrice = products.get(0).getPrice();
        for (Product product : products) {
            if (product.getPrice() < minPrice)
                minPrice = product.getPrice();
        }

        log.info("Get min price: " + minPrice);
        return minPrice;
    }

    // Get max price
    @GetMapping(value = "/products/price/max")
    public Double getMaxPrice() {
        List<Product> products = productRepository.findAll();
        Double maxPrice = products.get(0).getPrice();
        for (Product product : products) {
            if (product.getPrice() > maxPrice)
                maxPrice = product.getPrice();
        }

        log.info("Get max price: " + maxPrice);
        return maxPrice;
    }

    //    Get product by id
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = productRepository.findByProductId(id);
        if (product == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);

        log.info("Get product by id: " + id);
        return ResponseEntity.ok(product);
    }

    //    Update product
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product, @RequestParam(value = "files", required = false) MultipartFile[] files) {
        // Check if product exist
        Product _product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id));
        product.setProductId(_product.getProductId());

        // Update image if not null
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String fileName = GetFileNameImg(file);
                product.setImage(fileName);
            }
        } else {
            log.info("No file in request, use old file");
        }

        // Update brand and category
        Brand brand = brandRepository.findById(product.getBrand().getBrandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with id: " + product.getBrand().getBrandId()));
        product.setBrand(brand);

        Category category = categoryRepository.findById(product.getCategory().getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + product.getCategory().getCategoryId()));
        product.setCategory(category);

        // Update sale date if null
        if (product.getSaleDate() == null) {
            product.setSaleDate(new Date());
        }

        log.info("Update product: " + product);
        return ResponseEntity.ok(productRepository.save(product));
    }

    //    Delete product
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id));
        productRepository.delete(product);

        log.info("Delete product: " + product);
        return ResponseEntity.ok(product);
    }
    @GetMapping("/chartdata")
    public Iterable<Chart>MonthToTal(){
        return chartRepository.getTotalByMonth();
    }
    @GetMapping("/topproduct")
    public Iterable<TopProduct>TopProducts(){return topProductRepository.getTopProduct();}
    @GetMapping("/topcustomer")
    public Iterable<TopCustomer>TopCustomer(){return topCustomerRepository.getTopCustomer();}
    @GetMapping("/totallaptop")
    public Iterable<TotalSales>TotalLaptop(){
        try{
            return totalSalesRepository.totalLaptop();
        }catch (Exception e)
        {
            TotalSales a=new TotalSales(1,0);
            List<TotalSales>c=new ArrayList<>();
            c.add(a);
            Iterable<TotalSales>b=c;
            return b;
        }
    }
    @GetMapping("/totalmobile")
    public Iterable<TotalSales>TotalMobile(){
        try{
            return totalSalesRepository.totalMobile();
        }catch (Exception e)
        {
            TotalSales a=new TotalSales(2,0);
            List<TotalSales>c=new ArrayList<>();
            c.add(a);
            Iterable<TotalSales>b=c;
            return b;
        }
    }
    @GetMapping("/totalall")
    public Iterable<TotalSales>TotalAll(){
        try{
        return totalSalesRepository.totalAll();
    }
        catch (Exception e)
    {
        TotalSales a=new TotalSales(3,0);
        List<TotalSales>c=new ArrayList<>();
        c.add(a);
        Iterable<TotalSales>b=c;
        return b;
    }}

}
