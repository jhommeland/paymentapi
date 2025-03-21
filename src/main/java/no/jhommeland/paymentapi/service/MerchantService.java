package no.jhommeland.paymentapi.service;

import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.model.MerchantModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public List<MerchantModel> getMerchants() {
        return merchantRepository.findAllByOrderByAdyenMerchantAccount();
    }

    public String getClientKey(String merchantId) {
        MerchantModel merchant = merchantRepository.findById(merchantId).orElse(null);
        return merchant != null ? merchant.getAdyenClientKey() : "";
    }

}
