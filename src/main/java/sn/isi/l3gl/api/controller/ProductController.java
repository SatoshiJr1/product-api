package sn.isi.l3gl.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("POST /api/products - création du produit: {}", product.getName());
        Product created = productService.createProduct(product);
        log.info("Produit créé avec l'id: {}", created.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> listProducts() {
        log.info("GET /api/products - liste des produits");
        List<Product> products = productService.listProducts();
        log.info("Retour de {} produit(s)", products.size());
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        log.info("PUT /api/products/{} - mise à jour quantité: {}", id, body.get("quantity"));
        return ResponseEntity.ok(productService.updateQuantity(id, body.get("quantity")));
    }

    @GetMapping("/low-stock/count")
    public ResponseEntity<Map<String, Long>> countLowStockProducts() {
        log.info("GET /api/products/low-stock/count");
        return ResponseEntity.ok(Map.of("count", productService.countLowStockProducts()));
    }
}
