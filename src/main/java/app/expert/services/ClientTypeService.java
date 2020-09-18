package app.expert.services;

import app.base.constants.GErrors;
import app.base.exceptions.GBadRequest;
import app.base.exceptions.GException;
import app.base.utils.ObjectUtils;
import app.expert.db.statics.clientType.ClientType;
import app.expert.db.statics.clientType.ClientTypeCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ClientTypeService {
    
    private final ClientTypeCache cache;

    public ClientType get(String code) throws GException {
        return cache.find(code);
    }
    
    public List<ClientType> getAll() {
        return cache.getAll().stream()
                .filter(type -> {return !type.isDisabled();})
                .collect(Collectors.toList());
    }

    public ClientType add(String code, String name, String description) throws GException {
        checkInput(code, name);
        cache.checkIfNotExists(code);
        return cache.save(ClientType.builder()
                .code(code)
                .name(name)
                .description(description)
                .build());
    }

    public ClientType edit(String code, String name, String description) throws GException {
        checkInput(code, name);
        ClientType type = cache.find(code);
        type.setName(name);
        type.setDescription(description);
        return cache.save(type);
    }
    
    public void delete(String code) throws GException {
        ClientType type = cache.find(code);
        type.setDisabledOn(new Date());
        cache.save(type);
    }
    
    private void checkInput(String code, String name)  throws GException {
        if (ObjectUtils.isBlank(code)) throw new GBadRequest(GErrors.BAD_REQUEST);
        if (ObjectUtils.isBlank(name)) throw new GBadRequest(GErrors.BAD_REQUEST);
    }
}
