package com.example.product_api.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.product_api.model.Product;
import com.example.product_api.repository.ProductRepository;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    // GET all products
    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }

    // GET product by ID
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé"));
    }

    // CREATE product
    @PostMapping
    public Product create(@RequestBody Product product) {
        return repository.save(product);
    }

    // UPDATE product
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        Product existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé"));
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        return repository.save(existing);
    }

    // DELETE product
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // DUPLICATE product
    @PostMapping("/{id}/duplicate")
    public Product duplicate(@PathVariable Long id) {
        Product original = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé"));

        Product copy = new Product();
        copy.setName(original.getName());
        copy.setPrice(original.getPrice());

        return repository.save(copy);
    }

    // CREATE BUNDLE
    @PostMapping("/bundle")
    public ResponseEntity<?> createBundle(@RequestBody BundleRequest request) {
        // Récupération des produits sources
        List<Product> sources = repository.findAllById(request.sourceIds);

        if (sources.size() != request.sourceIds.size()) {
            return ResponseEntity.badRequest().body("Un ou plusieurs produits sources non trouvés");
        }

        // Vérifier l'absence de cycle
        if (hasCycle(sources, new HashSet<>())) {
            return ResponseEntity.badRequest().body("Cycle détecté dans la composition du bundle");
        }

        // Créer le produit bundle
        Product bundle = new Product();
        bundle.setName(request.name);
        bundle.setPrice(request.price);
        bundle.setSources(sources);

        repository.save(bundle);
        return ResponseEntity.ok(bundle);
    }

    // ----------- Méthodes internes -----------

    // DTO pour la requête de création de bundle
    public static class BundleRequest {
        public String name;
        public double price;
        public List<Long> sourceIds;
    }

    // Détection récursive de cycle
    private boolean hasCycle(List<Product> products, Set<Long> visited) {
        for (Product product : products) {
            if (visited.contains(product.getId())) {
                return true; // cycle détecté
            }
            visited.add(product.getId());
            if (hasCycle(product.getSources(), visited)) {
                return true;
            }
            visited.remove(product.getId());
        }
        return false;
    }
}
