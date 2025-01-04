package com.vinfern.functions;

import java.util.*;
import java.util.regex.Pattern;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;


public class CpfValidator {

    @FunctionName("CpfValidator")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        String cpf = request.getQueryParameters().get("cpf");

        if (cpf == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a cpf on the query string").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(new ValidationResult(isValidCpf(cpf))).build();
        }
    }

    private boolean isValidCpf(String cpf){

        String sanitizedCPF = cpf.replaceAll("\\D", "");

        if (sanitizedCPF.length() != 11) {
            return false;
        }

        if (Pattern.matches("(\\d)\\1{10}", sanitizedCPF)) {
            return false;
        }

        int[] digits = new int[11];
        for (int i = 0; i < 11; i++) {
            digits[i] = Character.getNumericValue(sanitizedCPF.charAt(i));
        }

        if (calculateCheckDigit(digits, 9) != digits[9]) {
            return false;
        }

        return calculateCheckDigit(digits, 10) == digits[10];
    }


    private int calculateCheckDigit(int[] digits, int length) {
        int sum = 0;
        int weight = length + 1;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * weight--;
        }
        int modulus = sum % 11;
        return modulus < 2 ? 0 : 11 - modulus;
    }

    private record ValidationResult(boolean isValidCpf){}
}
