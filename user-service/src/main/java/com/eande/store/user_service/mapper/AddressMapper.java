package com.eande.store.user_service.mapper;

import com.eande.store.user_service.dto.request.AddressRequest;
import com.eande.store.user_service.dto.request.UpdateAddressRequest;
import com.eande.store.user_service.dto.response.AddressResponse;
import com.eande.store.user_service.entity.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toEntity(AddressRequest request);
    AddressResponse toResponse(Address address);

    List<AddressResponse> toResponseList(List<Address> addresses);
    List<Address> toEntityList(List<AddressRequest> requests);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromDto(UpdateAddressRequest request, @MappingTarget Address address);
}
