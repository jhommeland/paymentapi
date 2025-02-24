package no.jhommeland.paymentapi.util;

@FunctionalInterface
public interface AdyenApiCall<T> {
    T apply() throws Exception;
}
