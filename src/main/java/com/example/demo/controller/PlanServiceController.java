package com.example.demo.controller;

import com.example.demo.MyUserDetailsService;
import com.example.demo.model.AuthenticationRequest;
import com.example.demo.model.AuthenticationResponse;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@RestController
public class PlanServiceController {

    @Autowired
    private RedisTemplate redisTemplate;

    private String etagValue = "etag";

    File schemaFile = new File("src\\main\\java\\com\\example\\demo\\controller\\JSONSchema.json");
    JSONTokener schemaData;
    {
        try {
            schemaData = new JSONTokener(new FileInputStream(schemaFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    JSONObject jsonSchema = new JSONObject(schemaData);
    Schema schemaValidator = SchemaLoader.load(jsonSchema);

    @RequestMapping(value = "/plans", method = RequestMethod.POST)
    public ResponseEntity<Object> createPlan(@RequestBody String data) {
        JSONObject result = new JSONObject(data);
        try {
            schemaValidator.validate(result);
        } catch (ValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CREATED);
        }

        HashMap<String, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            //Convert JSON to Map
            map = mapper.readValue(result.toString(), HashMap.class);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        HashMap<String, String> savedMap = new HashMap<>();
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            if (entry.getValue().getClass() == HashMap.class) {
//                HashMap<String, Object> newMap = (HashMap<String, Object>) entry.getValue();
//                String index = newMap.get("objectId") + "_" + newMap.get("objectType") + "_" + entry.getKey();
//                redisTemplate.opsForValue().set(index, newMap);
////                savedMap.put("object", index + "_" + entry.getKey());
//            } else if (entry.getValue().getClass() == String.class) {
//                redisTemplate.opsForValue().set(entry.getKey(), entry.getValue());
//            } else if (entry.getValue().getClass() == ArrayList.class) {
//                ArrayList list = (ArrayList) entry.getValue();
//                for (Object maps : list) {
//                    HashMap<String, Object> listMap = (HashMap<String, Object>) maps;
//                    for (Map.Entry<String, Object> listEntry : listMap.entrySet()) {
//                        if (listEntry.getValue().getClass() == HashMap.class) {
//                            HashMap<String, Object> newMap = (HashMap<String, Object>) listEntry.getValue();
//                            String index = newMap.get("objectId") + "_" + newMap.get("objectType") + "_" + listEntry.getKey() + "_" + entry.getKey();
//                            redisTemplate.opsForValue().set(index, newMap);
//                        } else if (listEntry.getValue().getClass() == String.class) {
//                            redisTemplate.opsForValue().set(listEntry.getKey(), listEntry.getValue());
//                        }
//                    }
//                }
//            }
//        }

        redisTemplate.opsForValue().set(result.getString("objectId"), map);
        return new ResponseEntity<>("Plan created successfully", HttpStatus.CREATED);
//        return ResponseEntity.ok().eTag(result.getString("objectId")).body("Plan created successfully");
    }

    @RequestMapping(value = "/plans/{id}")
    public ResponseEntity<Object> getPlan(@PathVariable("id") String id, @RequestHeader("If-None-Match") String head) {
        if (!redisTemplate.hasKey(id)) {
            return new ResponseEntity<>("Plan not exist", HttpStatus.NOT_FOUND);
        }
        etagValue = (String) redisTemplate.opsForValue().get("etag");
        if (head.equals(etagValue)) {
//            return new ResponseEntity<>(redisTemplate.opsForValue().get(id), HttpStatus.OK);
            return ResponseEntity.status(304).body("No Change");
        } else {
            etagValue = String.valueOf(redisTemplate.opsForValue().get(id).hashCode());
            redisTemplate.opsForValue().set("etag", etagValue);
            return ResponseEntity.ok().eTag(etagValue).body(redisTemplate.opsForValue().get(id));
        }
    }

    @RequestMapping(value = "/plans/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") String id) {
        if (redisTemplate.hasKey(id)) {
            redisTemplate.delete(id);
            return new ResponseEntity<>("Plan is deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Plan not exist", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/plans/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Object> updatePlan(@PathVariable("id") String id, @RequestBody String data, @RequestHeader("If-Match") String head) {
        if (!redisTemplate.hasKey(id)) {
            return new ResponseEntity<>("Plan not exist", HttpStatus.NOT_FOUND);
        }
        etagValue = (String) redisTemplate.opsForValue().get("etag");
        if (!head.equals(etagValue)) {
            return ResponseEntity.status(412).body("Etag Not Match");
        } else {
            redisTemplate.delete(id);

            JSONObject result = new JSONObject(data);
            try {
                schemaValidator.validate(result);
            } catch (ValidationException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.CREATED);
            }
            HashMap<String, Object> map = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            try {
                //Convert Map to JSON
                map = mapper.readValue(result.toString(), HashMap.class);
                System.out.println(map);
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            redisTemplate.opsForValue().set(result.getString("objectId"), map);
            return new ResponseEntity<>("Plan is updated successfully", HttpStatus.OK);

        }
    }

    @RequestMapping(value = "/plans/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<Object> updatePlanPartial(@PathVariable("id") String id, @RequestBody String data, @RequestHeader("If-Match") String head) {
        if (!redisTemplate.hasKey(id)) {
            return new ResponseEntity<>("Plan not exist", HttpStatus.NOT_FOUND);
        }
        etagValue = (String) redisTemplate.opsForValue().get("etag");
        if (!head.equals(etagValue)) {
            return ResponseEntity.status(412).body("Etag Not Match");
        } else {
            HashMap<String, Object> plan, partialUpdate = new HashMap<>();
            plan = (HashMap<String, Object>) redisTemplate.opsForValue().get(id);
            ObjectMapper mapper = new ObjectMapper();
            try {
                partialUpdate = mapper.readValue(new JSONObject(data).toString(), HashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Iterator<Map.Entry<String, Object>> partialEntries = partialUpdate.entrySet().iterator();
            while(partialEntries.hasNext()) {
                Map.Entry<String, Object> partialEntry = partialEntries.next();
                for (Map.Entry<String, Object> entry : plan.entrySet()) {
                    if(partialEntry.getKey().equals(entry.getKey())) {
                        plan.put(partialEntry.getKey(), partialEntry.getValue());
                    }
                }
            }
            redisTemplate.opsForValue().set(id, plan);
            return new ResponseEntity<>("Plan is updated successfully", HttpStatus.OK);
        }
    }

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private MyUserDetailsService userDetailsService;

    @RequestMapping(value = "token", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken() throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken("foo", "foo")
            );
        }
        catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }


        final UserDetails userDetails = userDetailsService
                .loadUserByUsername("foo");

        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

}
