package sn.isi.l3gl.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.isi.l3gl.core.entity.Product;
import sn.isi.l3gl.core.service.ProductService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return new ResponseEntity<>(productService.createProduct(product), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> listProducts() {
        return ResponseEntity.ok(productService.listProducts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(productService.updateQuantity(id, body.get("quantity")));
    }

    @GetMapping("/low-stock/count")
    public ResponseEntity<Map<String, Long>> countLowStockProducts() {
        return ResponseEntity.ok(Map.of("count", productService.countLowStockProducts()));
    }
}
