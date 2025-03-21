package no.jhommeland.paymentapi.service;

import no.jhommeland.paymentapi.dao.ShopperRepository;
import no.jhommeland.paymentapi.model.ShopperModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopperService {

    private final ShopperRepository shopperRepository;

    public ShopperService(ShopperRepository shopperRepository) {
        this.shopperRepository = shopperRepository;
    }

    public List<ShopperModel> getShoppers() {
        return shopperRepository.findAll();
    }

}
