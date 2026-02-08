package ai.mailhub.authhub.adapter.in.web;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public Mono<String> testApi(){
        return Mono.just("Hello World") ;
    }
}


