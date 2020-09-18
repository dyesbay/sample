package app.expert.services;

import app.base.exceptions.GException;
import app.base.models.GFilter;
import app.expert.constants.ExpertErrors;
import app.expert.db.client.Client;
import app.expert.db.client.ClientCache;
import app.expert.db.client.ClientRepository;
import app.expert.db.statics.clientType.ClientTypeCache;
import app.expert.db.statics.region.RegionsCache;
import app.expert.models.client.ClientFilter;
import app.expert.models.client.RqClient;
import app.expert.models.client.RsClient;
import app.expert.validation.GPhoneParser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientCache cache;
    private final ClientRepository repo;
    private final RegionsCache regCache;
    private final ClientTypeCache typeCache;

    private void checkBindedEntity(RqClient rq) throws GException {
        regCache.find(rq.getRegion());
        typeCache.find(rq.getClientType());
    }

    private void checkEmail(String email) throws GException {
        if (!repo.findByEmail(email).isEmpty()) throw new GException(ExpertErrors.DUPLICATE_CLIENT_EMAIL);
    }

    private void checkPhone(String phone) throws GException {
        ClientFilter phoneFilter = new ClientFilter();
        phoneFilter.setPhone(GPhoneParser.parsePhone(phone));
        if (!getFilteredPage(phoneFilter).getContent().isEmpty())
            throw new GException(ExpertErrors.DUPLICATE_CLIENT_PHONE);
    }

    public RsClient get(Long id) throws GException {
        return RsClient.get(cache.find(id), regCache, typeCache);
    }

    public RsClient add(RqClient rq) throws GException {
        checkBindedEntity(rq);
        checkEmail(rq.getEmail());
        checkPhone(rq.getPhone());
        Client client = rq.getClient();
        client.setRegion(rq.getRegion());
        client.setPhone(GPhoneParser.parsePhone(rq.getPhone()));
        client.setClientType(rq.getClientType());
        return RsClient.get(cache.save(client), regCache, typeCache);
    }

    public RsClient update(Long id, RqClient rq) throws GException {
        Client client = cache.find(id);
        checkBindedEntity(rq);
        checkEmail(rq.getEmail());
        checkPhone(rq.getPhone());
        Client.builder()
            .id(id)
            .firstName(rq.getFirstName())
            .lastName(rq.getLastName())
            .middleName(rq.getMiddleName())
            .clientType(rq.getClientType())
            .commentary(rq.getCompany())
            .contact(rq.getContact())
            .company(rq.getCompany())
            .communicationType(rq.getCommunicationType())
            .email(rq.getEmail())
            .phone(rq.getPhone())
            .region(rq.getRegion())
            .build();
        return RsClient.get(cache.save(client), regCache, typeCache);
    }

    public Page<RsClient> getFilteredPage(GFilter<Long, Client> rq) {
        Page<RsClient> result = cache.getRepository().findAll(rq, new PageRequest(rq.getPage(), rq.getSize()))
                .map(new Converter<Client, RsClient>()
                {
                    @Override
                    public RsClient convert(Client source) {
                        try {
                            return RsClient.get(source, regCache, typeCache);
                        } catch (GException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        return result;
    }

    public void delete(Long id) throws GException {
        Client client = cache.find(id);
        client.setDisabledOn(new Date());
        cache.save(client);
    }

    public RsClient getByPhone(String phone) throws GException {
        Client client = repo.getByPhone(GPhoneParser.parsePhone(phone));
        if (client == null || client.isDisabled()) throw new GException(cache.getNotFoundError());
        return RsClient.get(client, regCache, typeCache);
    }

}
