package no.jhommeland.paymentapi.service;

import com.adyen.model.posmobile.CreateSessionRequest;
import com.adyen.model.posmobile.CreateSessionResponse;
import no.jhommeland.paymentapi.dao.AdyenPaymentsApiDao;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.model.MerchantModel;
import no.jhommeland.paymentapi.model.PosMobileModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PosMobileService {

    private final AdyenPaymentsApiDao adyenPaymentsApiDao;

    private final MerchantRepository merchantRepository;

    public PosMobileService(AdyenPaymentsApiDao adyenPaymentsApiDao, MerchantRepository merchantRepository) {
        this.adyenPaymentsApiDao = adyenPaymentsApiDao;
        this.merchantRepository = merchantRepository;
    }

    public CreateSessionResponse createSession(PosMobileModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        CreateSessionRequest request = new CreateSessionRequest();
        request.setMerchantAccount(merchantModel.getAdyenMerchantAccount());
        request.setSetupToken(requestModel.getSetupToken());

        return adyenPaymentsApiDao.callPosMobileSessionsApi(request, merchantModel);

    }
}
