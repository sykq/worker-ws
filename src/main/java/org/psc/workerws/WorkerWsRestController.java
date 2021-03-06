package org.psc.workerws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.psc.workerws.calculators.DefaultCalculator;
import org.psc.workerws.calculators.domain.SimpleCalculatorSpecification;
import org.psc.workerws.documents.Person;
import org.psc.workerws.documents.PersonRepository;
import org.psc.workerws.files.FilesLogic;
import org.psc.workerws.generators.UuidGenerator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = {"/service", "/"})
public class WorkerWsRestController {

    private final UuidGenerator uuidGenerator;

    private final DefaultCalculator defaultCalculator;

    private final FilesLogic filesLogic;

    private final PersonRepository personRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("{\"status\":\"OK\"}", HttpStatus.OK);
    }

    @GetMapping("/token")
    public ResponseEntity<String> getToken() {
        return new ResponseEntity<>("{\"token\":\"in header\"}", HttpStatus.OK);
    }

    @GetMapping("/randomNumber")
    public Double getRandomNumber() {
        return new SecureRandom().nextDouble();
    }

    @GetMapping("/functional/mono/randomNumber")
    public Mono<Double> getRandomNumberFunctionallyAsMono() {
        return Mono.fromSupplier(() -> new SecureRandom().nextDouble());
    }

    @GetMapping(value = "/functional/flux/randomNumber", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Double> getRandomNumberFunctionallyAsFlux() {
        return Flux.fromStream(Stream.generate(() -> new SecureRandom().nextDouble()))
                .delayElements(Duration.ofMillis(100));
    }

    @GetMapping("/flux/randomNumber")
    public ResponseEntity<Flux<Double>> getRandomNumberAsFlux() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Flux.fromStream(Stream.generate(() -> new SecureRandom().nextDouble()).limit(500)));
    }

    @GetMapping(value = "/functional/flux/time", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<LocalDateTime> getTimeFunctionally() {
        return Flux.fromStream(Stream.generate(LocalDateTime::now)).delayElements(Duration.ofMillis(250));
    }

    @GetMapping(value = "/uuid", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, UUID> getUuid() {
        var first = uuidGenerator.getUuid();
        var second = uuidGenerator.getUuid();
        return Map.of("first", first, "second", second);
    }

    @PostMapping(value = "/defaultCalculation", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, BigDecimal> uselessCalculate(
            @RequestBody SimpleCalculatorSpecification specification) throws NoSuchAlgorithmException {
        var id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-hh:mm:ss.nnnnnnnnn"));
        var result = defaultCalculator.calculate(id, specification.getStartValue(), specification.getModifierValue());
        return Map.of(id, result);
    }

    @GetMapping(value = "/randomZips", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public DeferredResult<ResponseEntity<Resource>> getRandomZippedFile() {
        DeferredResult<ResponseEntity<Resource>> result = new DeferredResult<>();
        var zipFile = filesLogic.createRandomZipFile();
        var zipResource = new FileSystemResource(zipFile);

        var responseHeaders = new HttpHeaders();
        responseHeaders.setContentDisposition(
                ContentDisposition.builder("attachment").filename("randomEntries.zip", StandardCharsets.UTF_8).build());

        result.onCompletion(() -> Try.run(() -> Files.deleteIfExists(zipFile)));
        result.setResult(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(responseHeaders)
                .body(zipResource));
        return result;
    }

    @GetMapping(value = "/persons", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Person> getAllPersons() {
        return personRepository.getAllPersons().collect(Collectors.toList());
    }

    @PostMapping(value = "/dataList", consumes = MediaType.APPLICATION_JSON_VALUE)
    public <T> List<T> postDataList(@RequestBody List<T> dataList) throws JsonProcessingException {
        kafkaTemplate.send("test", objectMapper.writeValueAsString(dataList));
        return dataList;
    }


}