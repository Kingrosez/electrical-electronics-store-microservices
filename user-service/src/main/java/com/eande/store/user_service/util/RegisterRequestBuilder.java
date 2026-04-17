package com.eande.store.user_service.util;

import com.eande.store.user_service.dto.request.RegisterRequest;

public class RegisterRequestBuilder {
    private String name="John Doe";
    private String email="johndoe@gmil.com";
    private String password="Password@123";
    private String phone="1234567890";

    private RegisterRequestBuilder(){}
    public static RegisterRequestBuilder builder(){
        return new RegisterRequestBuilder();
    }
    public RegisterRequestBuilder withName(String name){
        this.name = name;
        return this;
    }
    public RegisterRequestBuilder withEmail(String email){
        this.email = email;
        return this;
    }
    public RegisterRequestBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }
    public RegisterRequestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }
    public RegisterRequest build(){
        return new RegisterRequest(name,email,password,phone);
    }

}
