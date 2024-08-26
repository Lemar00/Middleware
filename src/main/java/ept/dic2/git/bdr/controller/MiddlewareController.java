package ept.dic2.git.bdr.controller;

import ept.dic2.git.bdr.model.QueryRequest;
import ept.dic2.git.bdr.service.MiddlewareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MiddlewareController {

    private final MiddlewareService middlewareService;

    @Autowired
    public MiddlewareController(MiddlewareService middlewareService) {
        this.middlewareService = middlewareService;
    }

    @PostMapping("/query")
    public List<Map<String, Object>> query(@RequestBody QueryRequest queryRequest) {
        return middlewareService.executeQuery(queryRequest.getQuery());
    }
}
