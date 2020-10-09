package com.example.demo.controller;

import com.example.demo.model.plan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


@RestController
public class PlanServiceController {

    @Autowired
    private RedisTemplate redisTemplate;

    ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/plans", method = RequestMethod.POST)
    public ResponseEntity<Object> createPlan(@RequestBody plan plan) throws IOException {
        File schemaFile = new File("src\\main\\java\\com\\example\\demo\\controller\\JSONSchema.json");
        JSONTokener schemaData = new JSONTokener(new FileInputStream(schemaFile));
        JSONObject jsonSchema = new JSONObject(schemaData);
        Schema schemaValidator = SchemaLoader.load(jsonSchema);

        JSONObject result = new JSONObject(mapper.writeValueAsString(plan));
//        System.out.println(result.toString());
        try {
            schemaValidator.validate(result);
        } catch (ValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CREATED);
        }

        redisTemplate.opsForValue().set(plan.getObjectId(), plan);
        return new ResponseEntity<>("Plan created successfully", HttpStatus.CREATED);
    }

    @RequestMapping(value = "/plans/{id}")
    public ResponseEntity<Object> getPlan(@PathVariable("id") String id) {
        return new ResponseEntity<>(redisTemplate.opsForValue().get(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/plans/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") String id) {
        redisTemplate.delete(id);
        return new ResponseEntity<>("Plan is deleted successfully", HttpStatus.OK);
    }

    //    @RequestMapping(value = "/products/{id}", method = RequestMethod.PUT)
//    public ResponseEntity<Object> updateProduct(@PathVariable("id") String id, @RequestBody Plan plan) {
//        productRepo.remove(id);
//        plan.setId(id);
//        productRepo.put(id, plan);
//        return new ResponseEntity<>("Product is updated successfully", HttpStatus.OK);
//    }
}
