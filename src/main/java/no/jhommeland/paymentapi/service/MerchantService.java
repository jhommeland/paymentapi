package no.jhommeland.paymentapi.service;

import com.adyen.util.MaskUtil;
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
        List<MerchantModel> merchants = merchantRepository.findAllByOrderByAdyenMerchantAccount();
        merchants.forEach(merchantModel -> {
            merchantModel.setLivePrefix(MaskUtil.mask(merchantModel.getLivePrefix()));
        });
        return merchants;
    }

    public String getClientKey(String merchantId) {
        MerchantModel merchant = merchantRepository.findById(merchantId).orElse(null);
        return merchant != null ? merchant.getAdyenClientKey() : "";
    }

}
