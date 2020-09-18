package app.expert.controllers;

import app.base.constants.GErrors;
import app.base.controllers.GControllerAdvice;
import app.base.exceptions.GException;
import app.base.models.GResponse;
import app.expert.models.client.ClientFilter;
import app.expert.models.client.RqClient;
import app.expert.models.client.RsClient;
import app.expert.services.ClientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@ControllerAdvice
@Api(tags = "1019. Клиенты")
@RequestMapping
@RequiredArgsConstructor
public class Clients extends GControllerAdvice {
    
    private final ClientService service;

    @PutMapping("/client")
    @ApiOperation("Добавить нового клиента")
    public RsClient add(@Validated @ModelAttribute("rqClient") RqClient rq) throws GException {
        return service.add(rq);
    }
    
    @PatchMapping("/client")
    @ApiOperation("Изменение данных клиента")
    public RsClient edit(@RequestParam Long id, @Validated @ModelAttribute("rqClient") RqClient rq) throws GException {
        return service.update(id,rq);
    }
    
    @GetMapping("/client")
    @ApiOperation("Получить клиента по id")
    public RsClient get(@RequestParam Long id) throws GException {
        return service.get(id);
    }
    
    @DeleteMapping("/client")
    @ApiOperation("Удалить клиента по id")
    public GResponse delete(@RequestParam Long id) throws GException {
        service.delete(id);
        return new GResponse(GErrors.OK);
    }
    
    @GetMapping("/client/search")
    @ApiOperation("Получить отфильтрованный список клиентов с пагинацией")
    public Page<RsClient> get(@ModelAttribute("clientFilter") ClientFilter rq) throws GException {
        return service.getFilteredPage(rq);
    }
    
    @GetMapping("/clients")
    @ApiOperation("Получить клиента по номеру телефона")
    public RsClient getByPhone(@RequestParam String phone) throws GException {
        return service.getByPhone(phone);
    }

}
