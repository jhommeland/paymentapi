package no.jhommeland.paymentapi.model;

import com.adyen.model.checkout.AuthenticationData;
import com.adyen.model.checkout.ThreeDSRequestData;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

public enum TdsMode {
    OFF("off", () -> {
        AuthenticationData authenticationData = new AuthenticationData();
        authenticationData.setAttemptAuthentication(AuthenticationData.AttemptAuthenticationEnum.NEVER);
        return authenticationData;
    }),
    REDIRECT("redirect", () -> {
        AuthenticationData authenticationData = new AuthenticationData();
        authenticationData.setAttemptAuthentication(AuthenticationData.AttemptAuthenticationEnum.ALWAYS);
        ThreeDSRequestData threeDSRequestData = new ThreeDSRequestData();
        threeDSRequestData.setNativeThreeDS(ThreeDSRequestData.NativeThreeDSEnum.DISABLED);
        authenticationData.setThreeDSRequestData(threeDSRequestData);
        return authenticationData;
    }),
    NATIVE("native", () -> {
        AuthenticationData authenticationData = new AuthenticationData();
        authenticationData.setAttemptAuthentication(AuthenticationData.AttemptAuthenticationEnum.ALWAYS);
        ThreeDSRequestData threeDSRequestData = new ThreeDSRequestData();
        threeDSRequestData.setNativeThreeDS(ThreeDSRequestData.NativeThreeDSEnum.PREFERRED);
        authenticationData.setThreeDSRequestData(threeDSRequestData);
        return authenticationData;
    });

    private final String value;

    private final Supplier<AuthenticationData> authenticationDataSupplier;

    TdsMode(String value, Supplier<AuthenticationData> authenticationDataSupplier) {
        this.value = value;
        this.authenticationDataSupplier = authenticationDataSupplier;
    }

    public AuthenticationData getAuthenticationData() {
        return authenticationDataSupplier.get();
    }

    public static TdsMode fromValue(String value) {
        for(TdsMode v : values()) {
            if (v.value.equals(value)) {
                return v;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid TDS mode: " + value);
    }

}
