package com.example.product_api;


import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.product_api.controller.ProductController;
import com.example.product_api.model.Product;
import com.example.product_api.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product productA;
    private Product productB;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        productA = new Product();
        productA.setName("Produit A");
        productA.setPrice(19.99);
        productA = repository.save(productA);

        productB = new Product();
        productB.setName("Produit B");
        productB.setPrice(29.99);
        productB = repository.save(productB);
    }

    @Test
    void testGetProductById() throws Exception {
        mockMvc.perform(get("/products/" + productA.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Produit A"))
            .andExpect(jsonPath("$.price").value(19.99));
    }

    @Test
    void testCreateBundle() throws Exception {
        // Préparation de la requête bundle
        var bundleRequest = new ProductController.BundleRequest();
        bundleRequest.name = "Pack A+B";
        bundleRequest.price = 39.99;
        bundleRequest.sourceIds = Arrays.asList(productA.getId(), productB.getId());

        mockMvc.perform(post("/products/bundle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bundleRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Pack A+B"))
            .andExpect(jsonPath("$.sources.length()").value(2))
            .andExpect(jsonPath("$.sources[0].name").value("Produit A"));
    }
    @Test
void testGetAllProducts() throws Exception {
    mockMvc.perform(get("/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2)) // car setup() crée 2 produits
        .andExpect(jsonPath("$[0].name").value("Produit A"))
        .andExpect(jsonPath("$[1].name").value("Produit B"));
}
@Test
void testCreateSimpleProduct() throws Exception {
    Product newProduct = new Product();
    newProduct.setName("Produit C");
    newProduct.setPrice(49.99);

    mockMvc.perform(post("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newProduct)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Produit C"))
        .andExpect(jsonPath("$.price").value(49.99));
}

}
